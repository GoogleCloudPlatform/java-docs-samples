/**
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.example.managedvms.cloudsql;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@SuppressWarnings("serial")
@WebServlet(name = "cloudsql", value = "/*")
public class CloudSqlServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    final String createTableSql = "CREATE TABLE IF NOT EXISTS visits ( visit_id INT NOT NULL "
        + "AUTO_INCREMENT, user_ip VARCHAR(46) NOT NULL, timestamp DATETIME NOT NULL, "
        + "PRIMARY KEY (visit_id) )";
    final String createVisitSql = "INSERT INTO visits (user_ip, timestamp) VALUES (?, ?)";
    final String selectSql = "SELECT user_ip, timestamp FROM visits ORDER BY timestamp DESC "
        + "LIMIT 10";
    PrintWriter out = resp.getWriter();
    resp.setContentType("text/plain");
    String url = System.getenv("SQL_DATABASE_URL");
    try (Connection conn = DriverManager.getConnection(url);
        PreparedStatement statementCreateVisit = conn.prepareStatement(createVisitSql)) {
      conn.createStatement().executeUpdate(createTableSql);
      statementCreateVisit.setString(1, req.getRemoteAddr());
      statementCreateVisit.setTimestamp(2, new Timestamp(new Date().getTime()));
      statementCreateVisit.executeUpdate();
      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        out.print("Last 10 visits:\n");
        while (rs.next()) {
          String userIp = rs.getString("user_ip");
          String timeStamp = rs.getString("timestamp");
          out.print("Time: " + timeStamp + " Addr: " + userIp + "\n");
        }
      }
    } catch (SQLException e) {
      throw new ServletException("SQL error", e);
    }
  }
}
// [END example]
