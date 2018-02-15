/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appengine;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests to demonstrate App Engine Datastore transactions.
 */
@RunWith(JUnit4.class)
public class TransactionsTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Use High Rep job policy to allow cross group transactions in tests.
          new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy());

  private DatastoreService datastore;

  @Before
  public void setUp() {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    // Clean up any dangling transactions.
    Transaction txn = datastore.getCurrentTransaction(null);
    if (txn != null && txn.isActive()) {
      txn.rollback();
    }
    helper.tearDown();
  }

  @Test
  public void usingTransactions() throws Exception {
    Entity joe = new Entity("Employee", "Joe");
    datastore.put(joe);

    // [START using_transactions]
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Transaction txn = datastore.beginTransaction();
    try {
      Key employeeKey = KeyFactory.createKey("Employee", "Joe");
      Entity employee = datastore.get(employeeKey);
      employee.setProperty("vacationDays", 10);

      datastore.put(txn, employee);

      txn.commit();
    } finally {
      if (txn.isActive()) {
        txn.rollback();
      }
    }
    // [END using_transactions]
  }

  @Test
  public void entityGroups() throws Exception {
    try {
      // [START entity_groups]
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      Entity person = new Entity("Person", "tom");
      datastore.put(person);

      // Transactions on root entities
      Transaction txn = datastore.beginTransaction();

      Entity tom = datastore.get(person.getKey());
      tom.setProperty("age", 40);
      datastore.put(txn, tom);
      txn.commit();

      // Transactions on child entities
      txn = datastore.beginTransaction();
      tom = datastore.get(person.getKey());
      Entity photo = new Entity("Photo", tom.getKey());

      // Create a Photo that is a child of the Person entity named "tom"
      photo.setProperty("photoUrl", "http://domain.com/path/to/photo.jpg");
      datastore.put(txn, photo);
      txn.commit();

      // Transactions on entities in different entity groups
      txn = datastore.beginTransaction();
      tom = datastore.get(person.getKey());
      Entity photoNotAChild = new Entity("Photo");
      photoNotAChild.setProperty("photoUrl", "http://domain.com/path/to/photo.jpg");
      datastore.put(txn, photoNotAChild);

      // Throws IllegalArgumentException because the Person entity
      // and the Photo entity belong to different entity groups.
      txn.commit();
      // [END entity_groups]
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // We expect to get an exception that complains that we don't have a XG-transaction.
    }
  }

  @Test
  public void creatingAnEntityInASpecificEntityGroup() throws Exception {
    String boardName = "my-message-board";

    //CHECKSTYLE.OFF: VariableDeclarationUsageDistance - Increased clarity in sample
    // [START creating_an_entity_in_a_specific_entity_group]
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    String messageTitle = "Some Title";
    String messageText = "Some message.";
    Date postDate = new Date();

    Transaction txn = datastore.beginTransaction();

    Key messageBoardKey = KeyFactory.createKey("MessageBoard", boardName);

    Entity message = new Entity("Message", messageBoardKey);
    message.setProperty("message_title", messageTitle);
    message.setProperty("message_text", messageText);
    message.setProperty("post_date", postDate);
    datastore.put(txn, message);

    txn.commit();
    // [END creating_an_entity_in_a_specific_entity_group]
    //CHECKSTYLE.ON: VariableDeclarationUsageDistance
  }

  @Test
  public void crossGroupTransactions() throws Exception {
    // [START cross-group_XG_transactions_using_the_Java_low-level_API]
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    TransactionOptions options = TransactionOptions.Builder.withXG(true);
    Transaction txn = datastore.beginTransaction(options);

    Entity a = new Entity("A");
    a.setProperty("a", 22);
    datastore.put(txn, a);

    Entity b = new Entity("B");
    b.setProperty("b", 11);
    datastore.put(txn, b);

    txn.commit();
    // [END cross-group_XG_transactions_using_the_Java_low-level_API]
  }

  @Test
  public void usesForTransactions_relativeUpdates() throws Exception {
    String boardName = "my-message-board";
    Entity b = new Entity("MessageBoard", boardName);
    b.setProperty("count", 41);
    datastore.put(b);

    // [START uses_for_transactions_1]
    int retries = 3;
    while (true) {
      Transaction txn = datastore.beginTransaction();
      try {
        Key boardKey = KeyFactory.createKey("MessageBoard", boardName);
        Entity messageBoard = datastore.get(boardKey);

        long count = (Long) messageBoard.getProperty("count");
        ++count;
        messageBoard.setProperty("count", count);
        datastore.put(txn, messageBoard);

        txn.commit();
        break;
      } catch (ConcurrentModificationException e) {
        if (retries == 0) {
          throw e;
        }
        // Allow retry to occur
        --retries;
      } finally {
        if (txn.isActive()) {
          txn.rollback();
        }
      }
    }
    // [END uses_for_transactions_1]

    b = datastore.get(KeyFactory.createKey("MessageBoard", boardName));
    assertThat((long) b.getProperty("count")).named("board.count").isEqualTo(42L);
  }

  private Entity fetchOrCreate(String boardName) {
    // [START uses_for_transactions_2]
    Transaction txn = datastore.beginTransaction();
    Entity messageBoard;
    Key boardKey;
    try {
      boardKey = KeyFactory.createKey("MessageBoard", boardName);
      messageBoard = datastore.get(boardKey);
    } catch (EntityNotFoundException e) {
      messageBoard = new Entity("MessageBoard", boardName);
      messageBoard.setProperty("count", 0L);
      boardKey = datastore.put(txn, messageBoard);
    }
    txn.commit();
    // [END uses_for_transactions_2]

    return messageBoard;
  }

  @Test
  public void usesForTransactions_fetchOrCreate_fetchesExisting() throws Exception {
    Entity b = new Entity("MessageBoard", "my-message-board");
    b.setProperty("count", 7);
    datastore.put(b);

    Entity board = fetchOrCreate("my-message-board");

    assertThat((long) board.getProperty("count")).named("board.count").isEqualTo(7L);
  }

  @Test
  public void usesForTransactions_fetchOrCreate_createsNew() throws Exception {
    Entity board = fetchOrCreate("my-message-board");
    assertThat((long) board.getProperty("count")).named("board.count").isEqualTo(0L);
  }

  @Test
  public void usesForTransactions_readSnapshot() throws Exception {
    String boardName = "my-message-board";
    Entity b = new Entity("MessageBoard", boardName);
    b.setProperty("count", 13);
    datastore.put(b);

    // [START uses_for_transactions_3]
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    // Display information about a message board and its first 10 messages.
    Key boardKey = KeyFactory.createKey("MessageBoard", boardName);

    Transaction txn = datastore.beginTransaction();

    Entity messageBoard = datastore.get(boardKey);
    long count = (Long) messageBoard.getProperty("count");

    Query q = new Query("Message", boardKey);

    // This is an ancestor query.
    PreparedQuery pq = datastore.prepare(txn, q);
    List<Entity> messages = pq.asList(FetchOptions.Builder.withLimit(10));

    txn.commit();
    // [END uses_for_transactions_3]

    assertThat(count).named("board.count").isEqualTo(13L);
  }

  @Test
  public void transactionalTaskEnqueuing() throws Exception {
    // [START transactional_task_enqueuing]
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Queue queue = QueueFactory.getDefaultQueue();
    Transaction txn = datastore.beginTransaction();
    // ...

    queue.add(TaskOptions.Builder.withUrl("/path/to/handler"));

    // ...

    txn.commit();
    // [END transactional_task_enqueuing]
  }
}
