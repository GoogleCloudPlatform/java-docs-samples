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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
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
@WebServlet(name = "update", description = "Update a post", urlPatterns = "/update")
public class UpdateRecords extends HttpServlet {
  // Update an existing blog post
  // doGet() - retrieves the existing post data sends it to the input form
  // doPost() - stores updated blog post in Cloud SQL

  Connection conn;

  // SQL commands
  final String selectSql =
      "SELECT posts.title, posts.body, posts.author_id FROM posts WHERE post_id = ? LIMIT 1";
  final String updateSql = "UPDATE posts SET title = ?, body = ? WHERE post_id = ?";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // Retrieve the existing post data and send it to the update form

    Map<String, String[]> userData = req.getParameterMap();
    String[] postId = userData.get("id"); // Grab the websafe ID
    String decodedId =
        new String(Base64.getUrlDecoder().decode(postId[0])); // Decode the websafe ID

    try (PreparedStatement statementSelectPost = conn.prepareStatement(selectSql)) {
      statementSelectPost.setString(1, decodedId); // Include the decoded ID in the query
      ResultSet rs = statementSelectPost.executeQuery(); // Retrieve the post
      rs.next(); // Move the cursor

      // Build out the query with user submitted data
      req.setAttribute("title", rs.getString("title"));
      req.setAttribute("body", rs.getString("body"));
      req.setAttribute("author", rs.getString("author_id"));
      req.setAttribute("id", decodedId);

      // Page formatting
      final String pageTitle = "Updating blog post";
      req.setAttribute("pagetitle", pageTitle);

      req.getRequestDispatcher("/form.jsp")
          .forward(req, resp); // Send the user to the confirmation page

    } catch (SQLException e) {
      throw new ServletException("SQL error", e);
    }
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // Store the updated post

    // Create a map of the httpParameters that we want and run it through jSoup
    // jSoup is used to santise the content
    Map<String, String> blogContent =
        req.getParameterMap()
            .entrySet()
            .stream()
            .filter(a -> a.getKey().startsWith("blogContent_"))
            .collect(
                Collectors.toMap(
                    p -> p.getKey(), p -> Jsoup.clean(p.getValue()[0], Whitelist.basic())));

    try (PreparedStatement statementUpdatePost = conn.prepareStatement(updateSql)) {
      // Build out the query with dat// Build out the query with dataa
      statementUpdatePost.setString(1, blogContent.get("blogContent_title"));
      statementUpdatePost.setString(2, blogContent.get("blogContent_description"));
      statementUpdatePost.setString(3, blogContent.get("blogContent_id"));
      statementUpdatePost.executeUpdate();

      conn.close(); // Close the connection to the MySQL server

      final String confirmation =
          "Blog post " + blogContent.get("blogContent_title") + " has been updated";

      req.setAttribute("confirmation", confirmation);
      req.getRequestDispatcher("/confirm.jsp").forward(req, resp);

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
      } catch (SQLException e) {
        throw new ServletException("Unable to connect to Cloud SQL", e);
      }

    } finally {
      // Nothing really to do here.
    }
  }
}
