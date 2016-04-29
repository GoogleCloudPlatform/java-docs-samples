/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entities;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

/**
 * Unit tests to demonstrate App Engine Datastore entity group metadata.
 */
@RunWith(JUnit4.class)
public class MetadataEntityGroupTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set no eventual consistency, that way queries return all results.
          // https://cloud.google.com/appengine/docs/java/tools/localunittesting#Java_Writing_High_Replication_Datastore_tests
          new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(0),
          new LocalMemcacheServiceTestConfig());

  private DatastoreService datastore;

  @Before
  public void setUp() {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  // [START entity_group_1]
  private static long getEntityGroupVersion(DatastoreService ds, Transaction tx, Key entityKey) {
    try {
      return Entities.getVersionProperty(ds.get(tx, Entities.createEntityGroupKey(entityKey)));
    } catch (EntityNotFoundException e) {
      // No entity group information, return a value strictly smaller than any
      // possible version
      return 0;
    }
  }

  private static void printEntityGroupVersions(DatastoreService ds, PrintWriter writer) {
    Entity entity1 = new Entity("Simple");
    Key key1 = ds.put(entity1);
    Key entityGroupKey = Entities.createEntityGroupKey(key1);

    // Print entity1's entity group version
    writer.println("version " + getEntityGroupVersion(ds, null, key1));

    // Write to a different entity group
    Entity entity2 = new Entity("Simple");
    ds.put(entity2);

    // Will print the same version, as entity1's entity group has not changed
    writer.println("version " + getEntityGroupVersion(ds, null, key1));

    // Change entity1's entity group by adding a new child entity
    Entity entity3 = new Entity("Simple", entity1.getKey());
    ds.put(entity3);

    // Will print a higher version, as entity1's entity group has changed
    writer.println("version " + getEntityGroupVersion(ds, null, key1));
  }
  // [END entity_group_1]

  @Test
  public void printEntityGroupVersions_printsVersions() throws Exception {
    StringWriter responseWriter = new StringWriter();
    printEntityGroupVersions(datastore, new PrintWriter(responseWriter));
    assertThat(responseWriter.toString()).contains("version");
  }

  // [START entity_group_2]
  // A simple class for tracking consistent entity group counts.
  private static class EntityGroupCount implements Serializable {
    long version; // Version of the entity group whose count we are tracking
    int count;

    EntityGroupCount(long version, int count) {
      this.version = version;
      this.count = count;
    }

    // Display count of entities in an entity group, with consistent caching
    void showEntityGroupCount(
        DatastoreService ds, MemcacheService cache, PrintWriter writer, Key entityGroupKey) {
      EntityGroupCount egCount = (EntityGroupCount) cache.get(entityGroupKey);
      // Reuses getEntityGroupVersion method from the previous example.
      if (egCount != null && egCount.version == getEntityGroupVersion(ds, null, entityGroupKey)) {
        // Cached value matched current entity group version, use that
        writer.println(egCount.count + " entities (cached)");
      } else {
        // Need to actually count entities. Using a transaction to get a consistent count
        // and entity group version.
        Transaction tx = ds.beginTransaction();
        PreparedQuery pq = ds.prepare(tx, new Query(entityGroupKey));
        int count = pq.countEntities(FetchOptions.Builder.withLimit(5000));
        cache.put(
            entityGroupKey,
            new EntityGroupCount(getEntityGroupVersion(ds, tx, entityGroupKey), count));
        tx.rollback();
        writer.println(count + " entities");
      }
    }
  }
  // [END entity_group_2]

  @Test
  public void entityGroupCount_printsCount() throws Exception {
    StringWriter responseWriter = new StringWriter();
    MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
    Entity entity1 = new Entity("Simple");
    Key key1 = datastore.put(entity1);
    Key entityGroupKey = Entities.createEntityGroupKey(key1);

    EntityGroupCount groupCount = new EntityGroupCount(0, 0);
    groupCount.showEntityGroupCount(
        datastore, cache, new PrintWriter(responseWriter), entityGroupKey);

    assertThat(responseWriter.toString()).contains(" entities");
  }
}
