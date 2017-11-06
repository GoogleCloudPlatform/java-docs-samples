/*
 * Copyright 2017 Google Inc.
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

package com.example.appengine.cloudsql;

import static com.google.common.truth.Truth.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/* Unit tests for {@link UpdateRecords}. */
@RunWith(JUnit4.class)
public final class UpdateRecordsTest {

  Connection conn;

  // MySQL server connection URL - use localhost for testing
  // Format: jdbc:mysql://localhost/DATABASE_NAME?useSSL=false&user=USERNAME&password=PASSWORD
  final String serverUrl =
      "jdbc:mysql://localhost/DATABASE?useSSL=false&user=USERNAME&password=PASSWORD";

  // Table creation SQL commands
  final String createContentTableSql =
      "CREATE TABLE IF NOT EXISTS posts ( post_id INT NOT NULL "
          + "AUTO_INCREMENT, author_id INT NOT NULL, timestamp DATETIME NOT NULL, "
          + "title VARCHAR(256) NOT NULL, "
          + "body VARCHAR(1337) NOT NULL, PRIMARY KEY (post_id) )";

  final String createUserTableSql =
      "CREATE TABLE IF NOT EXISTS users ( user_id INT NOT NULL "
          + "AUTO_INCREMENT, user_fullname VARCHAR(64) NOT NULL, "
          + "PRIMARY KEY (user_id) )";

  // Record SQL commands
  final String createPostSql =
      "INSERT INTO posts (post_id, author_id, timestamp, title, body) VALUES (?, ?, ?, ?, ?)";

  // User creation SQL command
  final String createUserSql = "INSERT INTO users (user_id, user_fullname) VALUES (?, ?)";

  // Update record SQL command
  final String updateSql = "UPDATE posts SET title = ?, body = ? WHERE post_id = ?";

  // Select record SQL command
  final String selectSql =
      "SELECT posts.post_id, posts.title, posts.body, users.user_fullname FROM posts, users WHERE (post_id = ?) AND (posts.author_id = users.user_id)  LIMIT 1";

  // Drop table commands
  final String dropContentTableSql = "DROP TABLE posts";

  final String dropUserTableSql = "DROP TABLE users";

  @Before
  public void setUp() throws Exception {
    // Connect to the MySQL server for testing and create the databases

    try {
      conn = DriverManager.getConnection(serverUrl); // Connect to the database

      // Create tables
      conn.createStatement().executeUpdate(createContentTableSql); // Create content table
      conn.createStatement().executeUpdate(createUserTableSql); // Create user table
    } catch (Exception e) {
      throw new Exception("Unable to connect to Cloud SQL", e);
    }
  }

  @After
  public void tearDown() throws Exception {
    // Drop created tables

    try {
      PreparedStatement dropContentStatement = conn.prepareStatement(dropContentTableSql);
      PreparedStatement dropUserStatement = conn.prepareStatement(dropUserTableSql);

      dropContentStatement.executeUpdate();
      dropUserStatement.executeUpdate();

      conn.close(); // close the database connection
    } catch (Exception e) {
      throw new Exception("Unable to drop tables and close MySQL connection", e);
    }
  }

  @Test
  public void updateRecordsTest() throws Exception {
    /* Update a record stored in Cloud SQL */

    // Setup phase

    // Create a test author
    PreparedStatement userInsert = conn.prepareStatement(createUserSql);

    userInsert.setInt(1, 1); // set the UID manually
    userInsert.setString(2, "Test User");

    // Create a record and store it in Cloud SQL
    PreparedStatement postStatement = conn.prepareStatement(createPostSql);

    postStatement.setInt(1, 1); // set the ID manually
    postStatement.setInt(2, 1);
    postStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
    postStatement.setString(4, "Post 1 title");
    postStatement.setString(5, "Post 1 content");

    // Excute phase

    userInsert.executeUpdate(); // store the new user
    postStatement.executeUpdate(); // store the prepared queries

    // Encode the ID (to replicate behaviour as seen in application)
    String encodedId = Base64.getUrlEncoder().encodeToString(String.valueOf("1").getBytes());

    // Decode ID in preparation to use in Cloud SQL query
    String decodedId = new String(Base64.getUrlDecoder().decode(encodedId));

    try {
      PreparedStatement statementUpdatePost = conn.prepareStatement(updateSql);
      statementUpdatePost.setString(1, "Post 1 title updated");
      statementUpdatePost.setString(2, "Post 1 content updated");
      statementUpdatePost.setString(3, decodedId); // update post with ID = 1
      statementUpdatePost.executeUpdate(); // update the record

      PreparedStatement statementPost = conn.prepareStatement(selectSql);
      statementPost.setString(1, decodedId);
      ResultSet rs = statementPost.executeQuery(); // return the updated blog post

      // Check phase

      rs.next(); // increment the cursor

      assertThat((String) rs.getString("posts.post_id")).isEqualTo("1"); // ID hasn't changed
      assertThat((String) rs.getString("users.user_fullname"))
          .isEqualTo("Test User"); // Author hasn't changed
      assertThat((String) rs.getString("posts.title"))
          .isEqualTo("Post 1 title updated"); // Title has been updated
      assertThat((String) rs.getString("posts.body"))
          .isEqualTo("Post 1 content updated"); // Content has been updated
    } catch (Exception e) {
      throw new Exception("Error updating record from database", e);
    }
  }

  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void updateInvalidRecordsTest() throws Exception {
    /*
     * Tests to see if an invalid ID will result in updating a record
     * In this test no update should occur with an exception being thrown
     */

    // Setup phase

    // Create a test author
    PreparedStatement userInsert = conn.prepareStatement(createUserSql);

    userInsert.setInt(1, 1); // set the UID manually
    userInsert.setString(2, "Test User");

    // Create a record and store it in Cloud SQL
    PreparedStatement postStatement = conn.prepareStatement(createPostSql);

    postStatement.setInt(1, 1); // set the ID manually
    postStatement.setInt(2, 1);
    postStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
    postStatement.setString(4, "Post 1 title");
    postStatement.setString(5, "Post 1 content");

    // Excute phase

    userInsert.executeUpdate(); // store the new user
    postStatement.executeUpdate(); // store the prepared queries

    // Test if the user tries to an ID (unencoded) in the URI

    // Decode ID in preparation to use in Cloud SQL query but with unencoded ID
    exception.expect(IllegalArgumentException.class);
    String decodedId = new String(Base64.getUrlDecoder().decode("1"));
  }
}
