/**
 * Copyright 2017 Google Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.appengine.cloudsql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

@SuppressWarnings("serial")
@WebServlet(name = "create", description = "Write post content to SQL DB", urlPatterns = "/create")
public class CreateRecord extends HttpServlet {
  // Creates a record from data sent in a HTML form and stores it in Cloud SQL

  Connection conn;

  // Table creation queries
  final String createContentTableSql =
      "CREATE TABLE IF NOT EXISTS posts ( post_id INT NOT NULL "
          + "AUTO_INCREMENT, author_id INT NOT NULL, timestamp DATETIME NOT NULL, "
          + "title VARCHAR(256) NOT NULL, "
          + "body VARCHAR(1337) NOT NULL, PRIMARY KEY (post_id) )";

  final String createUserTableSql =
      "CREATE TABLE IF NOT EXISTS users ( user_id INT NOT NULL "
          + "AUTO_INCREMENT, user_fullname VARCHAR(64) NOT NULL, "
          + "PRIMARY KEY (user_id) )";

  // Post creation query
  final String createPostSql =
      "INSERT INTO posts (author_id, timestamp, title, body) VALUES (?, ?, ?, ?)";

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // Take the values submitted in an HTML form, clean them through jSoup, store
    // them in a Cloud SQL database and then send the user to a JSP that has a
    // personalised confirmation message.

    // Create a map of the httpParameters that we want and run it through jSoup
    Map<String, String> blogContent =
        req.getParameterMap()
            .entrySet()
            .stream()
            .filter(a -> a.getKey().startsWith("blogContent_"))
            .collect(
                Collectors.toMap(
                    p -> p.getKey(), p -> Jsoup.clean(p.getValue()[0], Whitelist.basic())));

    // Build the SQL command to insert the blog post into the database
    try (PreparedStatement statementCreatePost = conn.prepareStatement(createPostSql)) {
      statementCreatePost.setInt(
          1,
          Integer.parseInt(
              blogContent.get(
                  "blogContent_id"))); // set the author to the user ID from the user table
      statementCreatePost.setTimestamp(2, new Timestamp(new Date().getTime()));
      statementCreatePost.setString(3, blogContent.get("blogContent_title"));
      statementCreatePost.setString(4, blogContent.get("blogContent_description"));
      statementCreatePost.executeUpdate();

      conn.close(); // close the connection to the MySQL server

      // Send the user to the confirmation page with personalised confirmation text
      String confirmation = "Post with title " + blogContent.get("blogContent_title") + " created.";

      req.setAttribute("confirmation", confirmation);
      req.getRequestDispatcher("/confirm.jsp").forward(req, resp);

    } catch (SQLException e) {
      throw new ServletException("SQL error when creating post", e);
    }
  }

  @Override
  public void init() throws ServletException {
    try {
      String url = System.getProperty("cloudsql");

      try {
        conn = DriverManager.getConnection(url); // Connect to the database

        // Create the tables for first use
        conn.createStatement().executeUpdate(createContentTableSql); // Create content table
        conn.createStatement().executeUpdate(createUserTableSql); // Create user table
      } catch (SQLException e) {
        throw new ServletException("Unable to connect to Cloud SQL", e);
      }

    } finally {
      // Nothing really to do here.
    }
  }
}
