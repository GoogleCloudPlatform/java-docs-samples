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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "list", description = "List the latest news posts", urlPatterns = "/")
public class ListResults extends HttpServlet {
  // Displays the stored blog posts

  Connection conn; // DB Connection object

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

  // Create a test user
  final String createTestUserSql = "INSERT INTO users (user_fullname) VALUES ('Test User')";

  // Blog post retrieval queries
  final String selectSql =
      "SELECT posts.post_id, users.user_fullname, posts.timestamp, posts.title, posts.body FROM posts, users "
          + "WHERE (posts.author_id = users.user_id) AND (posts.body != \"\") ORDER BY posts.post_id DESC";

  // Preformatted HTML
  String headers =
      "<!DOCTYPE html><meta charset=\"utf-8\"><h1>Welcome to the App Engine Blog</h1><h3><a href=\"blogpost\">Add a new post</a></h3>";
  String blogPostDisplayFormat =
      "<h2> %s </h2> Posted at: %s by %s [<a href=\"/update?id=%s\">update</a>] | [<a href=\"/delete?id=%s\">delete</a>]<br><br> %s <br><br>";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // Retrieve blog posts from Cloud SQL database and display them

    PrintWriter out = resp.getWriter();

    out.println(headers); // Print HTML headers

    try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
      Map<Integer, Map<String, String>> storedPosts = new HashMap<>();

      while (rs.next()) {
        Map<String, String> blogPostContents = new HashMap<>();

        // Store the particulars for a blog in a map
        blogPostContents.put("author", rs.getString("users.user_fullname"));
        blogPostContents.put("title", rs.getString("posts.title"));
        blogPostContents.put("body", rs.getString("posts.body"));
        blogPostContents.put("publishTime", rs.getString("posts.timestamp"));

        // Store the post in a map with key of the postId
        storedPosts.put(rs.getInt("posts.post_id"), blogPostContents);
      }

      // Iterate the map and display each record's contents on screen
      storedPosts.forEach(
          (k, v) -> {
            // Encode the ID into a websafe string
            String encodedID = Base64.getUrlEncoder().encodeToString(String.valueOf(k).getBytes());

            // Build up string with values from Cloud SQL
            String recordOutput =
                String.format(
                    blogPostDisplayFormat,
                    v.get("title"),
                    v.get("publishTime"),
                    v.get("author"),
                    encodedID,
                    encodedID,
                    v.get("body"));

            out.println(recordOutput); // print out the HTML
          });

    } catch (SQLException e) {
      throw new ServletException("SQL error", e);
    }
  }

  @Override
  public void init() throws ServletException {
    try {
      String url = System.getProperty("cloudsql");

      try {
        conn = DriverManager.getConnection(url);

        // Create the tables so that the SELECT query doesn't throw an exception
        // if the user visits the page before any posts have been added

        conn.createStatement().executeUpdate(createContentTableSql); // create content table
        conn.createStatement().executeUpdate(createUserTableSql); // create user table

        // Create a test user
        conn.createStatement().executeUpdate(createTestUserSql);
      } catch (SQLException e) {
        throw new ServletException("Unable to connect to SQL server", e);
      }

    } finally {
      // Nothing really to do here.
    }
  }
}
