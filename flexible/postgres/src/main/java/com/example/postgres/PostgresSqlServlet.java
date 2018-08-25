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

package com.example.postgres;

import com.google.common.base.Stopwatch;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START gae_flex_postgres_app]
@SuppressWarnings("serial")
@WebServlet(name = "postgresql", value = "")
public class PostgresSqlServlet extends HttpServlet {
  Connection conn;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {

    final String createTableSql = "CREATE TABLE IF NOT EXISTS visits ( visit_id SERIAL NOT NULL, "
        + "user_ip VARCHAR(46) NOT NULL, ts timestamp NOT NULL, "
        + "PRIMARY KEY (visit_id) );";
    final String createVisitSql = "INSERT INTO visits (user_ip, ts) VALUES (?, ?);";
    final String selectSql = "SELECT user_ip, ts FROM visits ORDER BY ts DESC "
        + "LIMIT 10;";

    String path = req.getRequestURI();
    if (path.startsWith("/favicon.ico")) {
      return; // ignore the request for favicon.ico
    }

    PrintWriter out = resp.getWriter();
    resp.setContentType("text/plain");

    // store only the first two octets of a users ip address
    String userIp = req.getRemoteAddr();
    InetAddress address = InetAddress.getByName(userIp);
    if (address instanceof Inet6Address) {
      // nest indexOf calls to find the second occurrence of a character in a string
      // an alternative is to use Apache Commons Lang: StringUtils.ordinalIndexOf()
      userIp = userIp.substring(0, userIp.indexOf(":", userIp.indexOf(":") + 1)) + ":*:*:*:*:*:*";
    } else if (address instanceof Inet4Address) {
      userIp = userIp.substring(0, userIp.indexOf(".", userIp.indexOf(".") + 1)) + ".*.*";
    }

    Stopwatch stopwatch = Stopwatch.createStarted();
    try (PreparedStatement statementCreateVisit = conn.prepareStatement(createVisitSql)) {
      conn.createStatement().executeUpdate(createTableSql);
      statementCreateVisit.setString(1, userIp);
      statementCreateVisit.setTimestamp(2, new Timestamp(new Date().getTime()));
      statementCreateVisit.executeUpdate();

      try (ResultSet rs = conn.prepareStatement(selectSql).executeQuery()) {
        stopwatch.stop();
        out.print("Last 10 visits:\n");
        while (rs.next()) {
          String savedIp = rs.getString("user_ip");
          String timeStamp = rs.getString("ts");
          out.println("Time: " + timeStamp + " Addr: " + savedIp);
        }
        out.println("Elapsed: " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
      }
    } catch (SQLException e) {
      throw new ServletException("SQL error", e);
    }
  }

  @Override
  public void init() throws ServletException {
    String url;

    Properties properties = new Properties();
    try {
      properties.load(
          getServletContext().getResourceAsStream("/WEB-INF/classes/config.properties"));
      url = properties.getProperty("sqlUrl");
    } catch (IOException e) {
      log("no property", e);  // Servlet Init should never fail.
      return;
    }

    log("connecting to: " + url);
    try {
      Class.forName("org.postgresql.Driver");
      conn = DriverManager.getConnection(url);
    } catch (ClassNotFoundException e) {
      throw new ServletException("Error loading JDBC Driver", e);
    } catch (SQLException e) {
      throw new ServletException("Unable to connect to PostGre", e);
    } finally {
      // Nothing really to do here.
    }
  }
}
// [END gae_flex_postgres_app]
