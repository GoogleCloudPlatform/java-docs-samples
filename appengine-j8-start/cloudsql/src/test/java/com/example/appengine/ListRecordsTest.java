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
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ListRecords}. */
@RunWith(JUnit4.class)
public final class ListRecordsTest {

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

  final String selectSql =
      "SELECT posts.post_id, users.user_fullname, posts.title, posts.body FROM posts, users WHERE (posts.author_id = users.user_id) AND (posts.body != \"\") ORDER BY posts.post_id ASC";

  // User creation SQL command
  final String createUserSql = "INSERT INTO users (user_id, user_fullname) VALUES (?, ?)";

  // Drop table commands
  final String dropContentTableSql = "DROP TABLE posts";

  final String dropUserTableSql = "DROP TABLE users";

  @Before
  public void setUp() throws Exception {
    // Connect to the MySQL server for testing and create the databases

    try {
      conn = DriverManager.getConnection(serverUrl);
    } catch (Exception e) {
      throw new Exception("Unable to connect to Cloud SQL", e);
    }

    // Create tables if required
    conn.createStatement().executeUpdate(createContentTableSql); // create content table
    conn.createStatement().executeUpdate(createUserTableSql); // create user table
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
  public void listTest() throws Exception {
    /* Retrieve records from Cloud SQL */

    int counter = 1; // counter for iterating through results

    // Setup phase

    // Create a test author
    PreparedStatement userInsert = conn.prepareStatement(createUserSql);

    userInsert.setInt(1, 1);
    userInsert.setString(2, "Test User");

    // Create four records and store them in Cloud SQL

    PreparedStatement postBatch = conn.prepareStatement(createPostSql);

    postBatch.setInt(1, 1); // Manually set the ID because we care about the order
    postBatch.setInt(2, 1); // Author ID equals newly created author
    postBatch.setTimestamp(3, new Timestamp(new Date().getTime()));
    postBatch.setString(4, "Post 1 title");
    postBatch.setString(5, "Post 1 content");
    postBatch.addBatch();

    postBatch.setInt(1, 2);
    postBatch.setInt(2, 1);
    postBatch.setTimestamp(3, new Timestamp(new Date().getTime()));
    postBatch.setString(4, "Post 2 title");
    postBatch.setString(5, "Post 2 content");
    postBatch.addBatch();

    postBatch.setInt(1, 3);
    postBatch.setInt(2, 1);
    postBatch.setTimestamp(3, new Timestamp(new Date().getTime()));
    postBatch.setString(4, "Post 3 title");
    postBatch.setString(5, "Post 3 content");
    postBatch.addBatch();

    postBatch.setInt(1, 4);
    postBatch.setInt(2, 1);
    postBatch.setTimestamp(3, new Timestamp(new Date().getTime()));
    postBatch.setString(4, "Post 4 title");
    postBatch.setString(5, "Post 4 content");
    postBatch.addBatch();

    // Excute phase

    userInsert.executeUpdate(); // store the new user
    postBatch.executeBatch(); // store the prepared queries

    ResultSet rs =
        conn.prepareStatement(selectSql)
            .executeQuery(); // retrieve the posts and put them into a ResultSet for iteration

    // Check phase

    while (rs.next() && counter <= 4) {
      assertThat((String) rs.getString("users.user_fullname")).isEqualTo("Test User");
      assertThat((String) rs.getString("posts.title")).isEqualTo("Post " + counter + " title");
      assertThat((String) rs.getString("posts.body")).isEqualTo("Post " + counter + " content");
      counter++;
    }
  }

  @Test
  public void displayPostWithContentTest() throws Exception {
    /* Retrieve only records that have content */

    final String countSelectSql =
        "SELECT COUNT(*) as total FROM posts, users WHERE (posts.author_id = users.user_id) AND (posts.body != \"\")";

    // Setup phase

    // Create a test author
    PreparedStatement userInsert = conn.prepareStatement(createUserSql);

    userInsert.setInt(1, 1);
    userInsert.setString(2, "Test User");

    // Create four records and store them in Cloud SQL

    PreparedStatement postBatch = conn.prepareStatement(createPostSql);

    postBatch.setInt(1, 1); // Manually set the ID because we care about the order
    postBatch.setInt(2, 1); // Author ID equals newly created author
    postBatch.setTimestamp(3, new Timestamp(new Date().getTime()));
    postBatch.setString(4, "Post 1 title");
    postBatch.setString(5, "Post 1 content");
    postBatch.addBatch();

    postBatch.setInt(1, 2);
    postBatch.setInt(2, 1);
    postBatch.setTimestamp(3, new Timestamp(new Date().getTime()));
    postBatch.setString(4, "Post 2 title");
    postBatch.setString(5, "Post 2 content");
    postBatch.addBatch();

    postBatch.setInt(1, 3);
    postBatch.setInt(2, 1);
    postBatch.setTimestamp(3, new Timestamp(new Date().getTime()));
    postBatch.setString(4, "Post 3 title");
    postBatch.setString(5, "Post 3 content");
    postBatch.addBatch();

    postBatch.setInt(1, 4);
    postBatch.setInt(2, 1);
    postBatch.setTimestamp(3, new Timestamp(new Date().getTime()));
    postBatch.setString(4, "Post 4 title");
    postBatch.setString(5, ""); // No content in this post
    postBatch.addBatch();

    // Execute phase

    userInsert.executeUpdate();
    postBatch.executeBatch();

    ResultSet rs =
        conn.prepareStatement(countSelectSql)
            .executeQuery(); // retrieve the posts and put them into a ResultSet for iteration

    // Check phase

    rs.next(); // move the cursor

    // There should only be three posts returned by the query
    assertThat((String) rs.getString("total")).named("query results").isEqualTo("3");
  }
}
