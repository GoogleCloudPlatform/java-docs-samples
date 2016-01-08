/*
 * Copyright (c) 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.google.appengine.sparkdemo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.google.common.collect.Iterators;
import com.google.gcloud.datastore.Datastore;
import com.google.gcloud.datastore.DatastoreOptions;
import com.google.gcloud.datastore.Entity;
import com.google.gcloud.datastore.Key;
import com.google.gcloud.datastore.Query;
import com.google.gcloud.datastore.QueryResults;
import com.google.gcloud.datastore.StructuredQuery;
import com.google.gcloud.datastore.testing.LocalGcdHelper;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class UserServiceTest {

  private static final int PORT = LocalGcdHelper.findAvailablePort(LocalGcdHelper.DEFAULT_PORT);
  private static final String PROJECT_ID = LocalGcdHelper.DEFAULT_PROJECT_ID;
  private static final String USER_ID = "myId";
  private static final String USER_NAME = "myName";
  private static final String USER_EMAIL = "my@email.com";
  private static final User USER = new User(USER_ID, USER_NAME, USER_EMAIL);
  private static final Key USER_KEY = Key.builder(PROJECT_ID, "DEMO_USER", USER_ID).build();
  private static final Entity USER_RECORD = Entity.builder(USER_KEY)
      .set("id", USER_ID)
      .set("name", USER_NAME)
      .set("email", USER_EMAIL)
      .build();
  private static LocalGcdHelper gcdHelper;
  private static Datastore datastore;
  private static UserService userService;

  @BeforeClass
  public static void beforeClass() throws IOException, InterruptedException {
    if (!LocalGcdHelper.isActive(PROJECT_ID, PORT)) {
      gcdHelper = LocalGcdHelper.start(PROJECT_ID, PORT);
    }
    datastore = DatastoreOptions.builder()
        .projectId(PROJECT_ID)
        .host("http://localhost:" + PORT)
        .build()
        .service();
    userService = new UserService(datastore);
  }

  @Before
  public void setUp() {
    StructuredQuery<Key> query = Query.keyQueryBuilder().build();
    QueryResults<Key> result = datastore.run(query);
    datastore.delete(Iterators.toArray(result, Key.class));
    datastore.add(USER_RECORD);
  }

  @AfterClass
  public static void afterClass() throws IOException, InterruptedException {
    if (gcdHelper != null) {
      gcdHelper.stop();
    }
  }

  @Test
  public void testGetAllUsers() {
    List<User> allUsers = userService.getAllUsers();
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
    User actualUser = userService.createUser(name, email);
    assertEquals(name, actualUser.getName());
    assertEquals(email, actualUser.getEmail());
    assertNotNull(actualUser.getId());
    try {
      userService.createUser(null, email);
      fail("Expected to fail because name is null.");
    } catch (IllegalArgumentException e) {
      assertEquals("Parameter 'name' cannot be empty", e.getMessage());
    }
    try {
      userService.createUser(name, null);
      fail("Expected to fail because email is null.");
    } catch (IllegalArgumentException e) {
      assertEquals("Parameter 'email' cannot be empty", e.getMessage());
    }
  }

  @Test
  public void testDeleteUser() {
    String result = userService.deleteUser(USER_ID);
    assertEquals("ok", result);
    assertNull(datastore.get(USER_KEY));
  }

  @Test
  public void testUpdateUser() {
    String newName = "myNewName";
    String newEmail = "mynew@email.com";
    User updatedUser = userService.updateUser(USER_ID, newName, newEmail);
    assertEquals(USER_ID, updatedUser.getId());
    assertEquals(newName, updatedUser.getName());
    assertEquals(newEmail, updatedUser.getEmail());
    try {
      userService.updateUser(USER_ID, null, USER_EMAIL);
      fail("Expected to fail because name is null.");
    } catch (IllegalArgumentException e) {
      assertEquals("Parameter 'name' cannot be empty", e.getMessage());
    }
    try {
      userService.updateUser(USER_ID, USER_NAME, null);
      fail("Expected to fail because email is null.");
    } catch (IllegalArgumentException e) {
      assertEquals("Parameter 'email' cannot be empty", e.getMessage());
    }
  }
}
