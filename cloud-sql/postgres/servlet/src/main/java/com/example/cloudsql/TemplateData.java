/*
 * Copyright 2022 Google LLC
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class TemplateData {

  public int tabCount;
  public int spaceCount;
  public List<Vote> recentVotes;

  public TemplateData(int tabCount, int spaceCount, List<Vote> recentVotes) {
    this.tabCount = tabCount;
    this.spaceCount = spaceCount;
    this.recentVotes = recentVotes;
  }

  public static TemplateData getTemplateData(DataSource pool) throws SQLException {
    int tabCount = 0;
    int spaceCount = 0;
    List<Vote> recentVotes = new ArrayList<>();
    try (Connection conn = pool.getConnection()) {
      // PreparedStatements are compiled by the database immediately and executed at a later date.
      // Most databases cache previously compiled queries, which improves efficiency.
      String stmt1 = "SELECT candidate, time_cast FROM votes ORDER BY time_cast DESC LIMIT 5";
      try (PreparedStatement voteStmt = conn.prepareStatement(stmt1);) {
        // Execute the statement
        ResultSet voteResults = voteStmt.executeQuery();
        // Convert a ResultSet into Vote objects
        while (voteResults.next()) {
          String candidate = voteResults.getString(1);
          Timestamp timeCast = voteResults.getTimestamp(2);
          recentVotes.add(new Vote(candidate.trim(), timeCast));
        }
      }

      // PreparedStatements can also be executed multiple times with different arguments. This can
      // improve efficiency, and project a query from being vulnerable to an SQL injection.
      String stmt2 = "SELECT COUNT(vote_id) FROM votes WHERE candidate=?";
      try (PreparedStatement voteCountStmt = conn.prepareStatement(stmt2);) {
        voteCountStmt.setString(1, "TABS");
        ResultSet tabResult = voteCountStmt.executeQuery();
        if (tabResult.next()) { // Move to the first result
          tabCount = tabResult.getInt(1);
        }

        voteCountStmt.setString(1, "SPACES");
        ResultSet spaceResult = voteCountStmt.executeQuery();
        if (spaceResult.next()) { // Move to the first result
          spaceCount = spaceResult.getInt(1);
        }
      }
    } catch (SQLException ex) {
      // If something goes wrong, the application needs to react appropriately. This might mean
      // getting a new connection and executing the query again, or it might mean redirecting the
      // user to a different page to let them know something went wrong.
      throw new SQLException(
          "Unable to successfully connect to the database. Please check the "
              + "steps in the README and try again.",
          ex);
    }
    TemplateData templateData = new TemplateData(tabCount, spaceCount, recentVotes);

    return templateData;
  }
}
