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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link PostForm}. */
@RunWith(JUnit4.class)
public final class PostFormTest {

  Connection conn;

  // MySQL server connection URL - use localhost for testing
  // Format: jdbc:mysql://localhost/DATABASE_NAME?useSSL=false&user=USERNAME&password=PASSWORD
  final String serverUrl =
      "jdbc:mysql://localhost/DATABASE?useSSL=false&user=USERNAME&password=PASSWORD";

  // Table creation SQL command
  final String createUserTableSql =
      "CREATE TABLE IF NOT EXISTS users ( user_id INT NOT NULL "
          + "AUTO_INCREMENT, user_fullname VARCHAR(64) NOT NULL, "
          + "PRIMARY KEY (user_id) )";

  // User creation SQL command
  final String createUserSql = "INSERT INTO users (user_id, user_fullname) VALUES (?, ?)";

  // User selection SQL command
  final String selectUserSql = "SELECT user_id, user_fullname FROM users";

  final String dropUserTableSql = "DROP TABLE users";

  @Before
  public void setUp() throws Exception {
    // Connect to the MySQL server for testing and create the databases

    try {
      conn = DriverManager.getConnection(serverUrl);
    } catch (Exception e) {
      throw new Exception("Unable to connect to Cloud SQL", e);
    }

    // Create table
    conn.createStatement().executeUpdate(createUserTableSql); // create user table
  }

  @After
  public void tearDown() throws Exception {
    // Drop created tables and close the connection

    try {
      PreparedStatement dropUserStatement = conn.prepareStatement(dropUserTableSql);
      dropUserStatement.executeUpdate();

      conn.close(); // close the database connection
    } catch (Exception e) {
      throw new Exception("Unable to drop tables and close MySQL connection", e);
    }
  }

  @Test
  public void listTest() throws Exception {
    // Retrieve user's names from Cloud SQL

    // Setup phase

    // Create a user
    PreparedStatement userInsert = conn.prepareStatement(createUserSql);

    userInsert.setInt(1, 1);
    userInsert.setString(2, "Test User");

    // Excute phase

    userInsert.executeUpdate(); // store the new user

    ResultSet rs =
        conn.prepareStatement(selectUserSql)
            .executeQuery(); // retrieve the user and store them in a ResultSet for iteration

    // Check phase

    while (rs.next()) {
      assertThat((String) rs.getString("user_id")).isEqualTo("1");
      assertThat((String) rs.getString("user_fullname")).isEqualTo("Test User");
    }
  }
}
