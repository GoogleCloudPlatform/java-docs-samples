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

/* Unit tests for {@link DeleteRecords}. */
@RunWith(JUnit4.class)
public final class DeleteRecordsTest {

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

  final String countSelectSql = "SELECT COUNT(*) as total FROM posts";

  // User creation SQL command
  final String createUserSql = "INSERT INTO users (user_id, user_fullname) VALUES (?, ?)";

  // Delete record SQL command
  final String deleteRecordSql = "DELETE FROM posts WHERE post_id = ?";

  // Drop table commands
  final String dropContentTableSql = "DROP TABLE posts";

  final String dropUserTableSql = "DROP TABLE users";

  @Before
  public void setUp() throws Exception {
    // Connect to the MySQL server for testing and create the databases

    try {
      conn = DriverManager.getConnection(serverUrl);

      // Create tables
      conn.createStatement().executeUpdate(createContentTableSql); // create content table
      conn.createStatement().executeUpdate(createUserTableSql); // create user table
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
  public void deleteRecordsTest() throws Exception {
    /* Delete a record stored in Cloud SQL */

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
      PreparedStatement statementDeletePost = conn.prepareStatement(deleteRecordSql);
      statementDeletePost.setString(1, decodedId); // delete post with ID = 1
      statementDeletePost.executeUpdate(); // delete the record from the database

      PreparedStatement statementCountPosts = conn.prepareStatement(countSelectSql);
      ResultSet rs =
          statementCountPosts.executeQuery(); // check to see how many records are in the table

      // Check phase

      rs.next(); // increment the cursor

      // the table should be empty
      assertThat((String) rs.getString("total")).named("query results").isEqualTo("0");
    } catch (Exception e) {
      throw new Exception("Error deleting record from database", e);
    }
  }

  @Rule public final ExpectedException exception = ExpectedException.none();

  @Test
  public void deleteInvalidRecordsTest() throws Exception {
    /*
     * Tests to see if an invalid ID will result in record deletion
     * In this test no deletion should occur
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
