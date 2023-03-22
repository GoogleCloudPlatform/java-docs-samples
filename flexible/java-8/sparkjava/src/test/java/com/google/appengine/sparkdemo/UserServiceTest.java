/*
 * Copyright 2015 Google Inc.
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

package com.google.appengine.sparkdemo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.google.common.collect.Iterators;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.threeten.bp.Duration;

public class UserServiceTest {

  private static final LocalDatastoreHelper HELPER = LocalDatastoreHelper.create(1.0);
  private static final DatastoreOptions DATASTORE_OPTIONS = HELPER.getOptions();
  private static final Datastore DATASTORE = DATASTORE_OPTIONS.getService();
  private static final String KIND = "DemoUser";
  private static final UserService USER_SERVICE = new UserService(DATASTORE, KIND);
  private static final String USER_ID = "myId";
  private static final String USER_NAME = "myName";
  private static final String USER_EMAIL = "my@email.com";
  private static final User USER = new User(USER_ID, USER_NAME, USER_EMAIL);
  private static final Key USER_KEY =
      Key.newBuilder(DATASTORE_OPTIONS.getProjectId(), KIND, USER_ID).build();
  private static final Entity USER_RECORD = Entity.newBuilder(USER_KEY)
      .set("id", USER_ID)
      .set("name", USER_NAME)
      .set("email", USER_EMAIL)
      .build();

  @BeforeClass
  public static void beforeClass() throws IOException, InterruptedException {
    HELPER.start();
  }

  @Before
  public void setUp() {
    StructuredQuery<Key> query = Query.newKeyQueryBuilder().build();
    QueryResults<Key> result = DATASTORE.run(query);
    DATASTORE.delete(Iterators.toArray(result, Key.class));
    DATASTORE.add(USER_RECORD);
  }

  @AfterClass
  public static void afterClass() throws IOException, InterruptedException, TimeoutException {
    HELPER.stop(Duration.ofMinutes(1));
  }

  @Test
  public void testGetAllUsers() {
    List<User> allUsers = USER_SERVICE.getAllUsers();
    assertEquals(1, allUsers.size());
    User actualUser = allUsers.get(0);
    assertEquals(USER.getId(), actualUser.getId());
    assertEquals(USER.getName(), actualUser.getName());
    assertEquals(USER.getEmail(), actualUser.getEmail());
  }

  @Test
  public void testCreateUser() {
    String name = "myNewName";
    String email = "mynew@email.com";
    User actualUser = USER_SERVICE.createUser(name, email);
    assertEquals(name, actualUser.getName());
    assertEquals(email, actualUser.getEmail());
    assertNotNull(actualUser.getId());
    try {
      USER_SERVICE.createUser(null, email);
      fail("Expected to fail because name is null.");
    } catch (IllegalArgumentException e) {
      assertEquals("Parameter 'name' cannot be empty", e.getMessage());
    }
    try {
      USER_SERVICE.createUser(name, null);
      fail("Expected to fail because email is null.");
    } catch (IllegalArgumentException e) {
      assertEquals("Parameter 'email' cannot be empty", e.getMessage());
    }
  }

  @Test
  public void testDeleteUser() {
    String result = USER_SERVICE.deleteUser(USER_ID);
    assertEquals("ok", result);
    assertNull(DATASTORE.get(USER_KEY));
  }

  @Test
  public void testUpdateUser() {
    String newName = "myNewName";
    String newEmail = "mynew@email.com";
    User updatedUser = USER_SERVICE.updateUser(USER_ID, newName, newEmail);
    assertEquals(USER_ID, updatedUser.getId());
    assertEquals(newName, updatedUser.getName());
    assertEquals(newEmail, updatedUser.getEmail());
    try {
      USER_SERVICE.updateUser(USER_ID, null, USER_EMAIL);
      fail("Expected to fail because name is null.");
    } catch (IllegalArgumentException e) {
      assertEquals("Parameter 'name' cannot be empty", e.getMessage());
    }
    try {
      USER_SERVICE.updateUser(USER_ID, USER_NAME, null);
      fail("Expected to fail because email is null.");
    } catch (IllegalArgumentException e) {
      assertEquals("Parameter 'email' cannot be empty", e.getMessage());
    }
  }
}
