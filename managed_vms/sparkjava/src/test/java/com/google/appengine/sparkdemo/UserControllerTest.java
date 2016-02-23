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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.Gson;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import spark.Spark;
import spark.utils.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class UserControllerTest {

  private static final String USER_NAME = "myName";
  private static final String USER_EMAIL = "my@email.com";
  private static String userId;

  @BeforeClass
  public static void beforeClass() {
    Main.main(new String[] {"kind=DemoUser" + UUID.randomUUID().toString().replaceAll("-", "")});
    Spark.awaitInitialization();
  }

  @Before
  public void setUp() throws IOException {
    userId = createUser(USER_NAME, USER_EMAIL).getId();
  }

  @After
  public void tearDown() throws IOException {
    deleteUser(userId);
  }

  @AfterClass
  public static void afterClass() {
    Spark.stop();
  }

  @Test
  public void testGetAllUsers() throws IOException {
    User[] users = getAllUsers();
    assertTrue(users.length <= 1);
  }

  @Test
  public void testCreateUser() throws IOException {
    User user = createUser(USER_NAME, USER_EMAIL);
    assertNotNull(user.getId());
    assertEquals(USER_NAME, user.getName());
    assertEquals(USER_EMAIL, user.getEmail());
  }

  @Test
  public void testCreateUserInvalidRequest() {
    try {
      executeRequest("POST", "/api/users?name=&email=");
      fail("Should fail due to an invalid request.");
    } catch (IOException e) {
      assertTrue(e.getMessage().startsWith("Server returned HTTP response code: 400 for URL"));
    }
  }

  @Test
  public void testDeleteUser() throws IOException {
    assertNotNull(getUser(userId));
    assertEquals("\"ok\"", deleteUser(userId));
    assertNull(getUser(userId));
  }

  @Test
  public void updateUser() throws IOException {
    String newName = "myNewName";
    String newEmail = "mynew@email.com";
    String responseStr = executeRequest(
        "PUT",
        "/api/users/" + userId + "?id=" + userId + "&name=" + newName + "&email=" + newEmail);
    User updatedUser = new Gson().fromJson(responseStr, User.class);
    assertEquals(userId, updatedUser.getId());
    assertEquals(newName, updatedUser.getName());
    assertEquals(newEmail, updatedUser.getEmail());
  }

  private static String executeRequest(String method, String path) throws IOException {
    URL url = new URL("http://localhost:8080" + path);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(method);
    connection.setDoOutput(true);
    connection.connect();
    return IOUtils.toString(connection.getInputStream());
  }

  private static User createUser(String name, String email) throws IOException {
    return new Gson().fromJson(
        executeRequest("POST", "/api/users?name=" + name + "&email=" + email), User.class);
  }

  private static String deleteUser(String id) throws IOException {
    return executeRequest("DELETE", "/api/users/" + id);
  }

  private static User getUser(String id) throws IOException {
    return new Gson().fromJson(executeRequest("GET", "/api/users/" + id), User.class);
  }

  private static User[] getAllUsers() throws IOException {
    return new Gson().fromJson(executeRequest("GET", "/api/users"), User[].class);
  }
}
