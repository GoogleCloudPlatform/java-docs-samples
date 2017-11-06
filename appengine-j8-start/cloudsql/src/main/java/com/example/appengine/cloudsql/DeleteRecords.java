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
import java.util.Base64;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "delete", description = "List the latest news posts", urlPatterns = "/delete")
public class DeleteRecords extends HttpServlet {

  Connection conn;

  // Delete query
  final String deleteSql = "DELETE FROM posts WHERE post_id = ?";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // Delete the record for a given ID

    Map<String, String[]> userData = req.getParameterMap();

    String[] postId = userData.get("id");
    String decodedId =
        new String(Base64.getUrlDecoder().decode(postId[0])); // decode the websafe ID

    try (PreparedStatement statementDeletePost = conn.prepareStatement(deleteSql)) {
      statementDeletePost.setString(1, decodedId);
      statementDeletePost.executeUpdate();

      conn.close(); // Close the connection to the MySQL server

      final String confirmation = "Post ID " + decodedId + " has been deleted.";

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
