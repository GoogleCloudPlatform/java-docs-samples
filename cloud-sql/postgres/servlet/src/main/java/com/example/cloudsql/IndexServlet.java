/*
 * Copyright 2018 Google LLC
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

package com.example.cloudsql;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@SuppressFBWarnings(
    value = {"SE_NO_SERIALVERSIONID", "WEM_WEAK_EXCEPTION_MESSAGING"},
    justification = "Not needed for IndexServlet, Exception adds context")
@WebServlet(name = "Index", value = "")
public class IndexServlet extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(IndexServlet.class.getName());

  public TemplateData getTemplateData(DataSource pool) throws ServletException {
    try {
      return TemplateData.getTemplateData(pool);
    } catch (SQLException ex) {
      throw new ServletException(ex);
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    // Extract the pool from the Servlet Context, reusing the one that was created
    // in the ContextListener when the application was started
    DataSource pool = (DataSource) req.getServletContext().getAttribute("my-pool");

    TemplateData templateData = getTemplateData(pool);

    // Add variables and render the page
    req.setAttribute("tabCount", templateData.tabCount);
    req.setAttribute("spaceCount", templateData.spaceCount);
    req.setAttribute("recentVotes", templateData.recentVotes);
    req.getRequestDispatcher("/index.jsp").forward(req, resp);
  }

  // Used to validate user input. All user provided data should be validated and sanitized before
  // being used something like a SQL query. Returns null if invalid.
  @Nullable
  private String validateTeam(String input) {
    if (input != null) {
      input = input.toUpperCase(Locale.ENGLISH);
      // Must be either "TABS" or "SPACES"
      if (!"TABS".equals(input) && !"SPACES".equals(input)) {
        return null;
      }
    }
    return input;
  }

  @SuppressFBWarnings(
      value = {"SERVLET_PARAMETER", "XSS_SERVLET"},
      justification = "Input is validated and sanitized.")
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Get the team from the request and record the time of the vote.
    String team = validateTeam(req.getParameter("team"));
    Timestamp now = new Timestamp(new Date().getTime());
    if (team == null) {
      resp.setStatus(400);
      resp.getWriter().append("Invalid team specified.");
      return;
    }

    // Reuse the pool that was created in the ContextListener when the Servlet started.
    DataSource pool = (DataSource) req.getServletContext().getAttribute("my-pool");
    // [START cloud_sql_postgres_servlet_connection]
    // Using a try-with-resources statement ensures that the connection is always released back
    // into the pool at the end of the statement (even if an error occurs)
    try (Connection conn = pool.getConnection()) {

      // PreparedStatements can be more efficient and project against injections.
      String stmt = "INSERT INTO votes (time_cast, candidate) VALUES (?, ?);";
      try (PreparedStatement voteStmt = conn.prepareStatement(stmt);) {
        voteStmt.setTimestamp(1, now);
        voteStmt.setString(2, team);

        // Finally, execute the statement. If it fails, an error will be thrown.
        voteStmt.execute();
      }
    } catch (SQLException ex) {
      // If something goes wrong, handle the error in this section. This might involve retrying or
      // adjusting parameters depending on the situation.
      // [START_EXCLUDE]
      LOGGER.log(Level.WARNING, "Error while attempting to submit vote.", ex);
      resp.setStatus(500);
      resp.getWriter()
          .write(
              "Unable to successfully cast vote! Please check the application "
                  + "logs for more details.");
      // [END_EXCLUDE]
    }
    // [END cloud_sql_postgres_servlet_connection]

    resp.setStatus(200);
    resp.getWriter().printf("Vote successfully cast for '%s' at time %s!%n", team, now);
  }
}
