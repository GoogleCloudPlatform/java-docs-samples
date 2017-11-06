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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "FormServlet", description = "List the latest news posts", value = "/blogpost")
public class PostForm extends HttpServlet {

  Connection conn;

  final String getUserId = "SELECT user_id, user_fullname FROM users";
  Map<Integer, String> users = new HashMap<Integer, String>();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // Display users' full name for the blog creation form
    // Send a Map of user IDs and full names to the HTML form
    // Find the user ID from the full name

    try (ResultSet rs = conn.prepareStatement(getUserId).executeQuery()) {
      while (rs.next()) {
        users.put(rs.getInt("user_id"), rs.getString("user_fullname"));
      }

      conn.close(); // close the database connection

      req.setAttribute("users", users);
      req.getRequestDispatcher("/form.jsp")
          .forward(req, resp); // Send the map of tuples to the update page

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
