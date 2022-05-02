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

package com.example.cloudsql.functions;

import com.example.cloudsql.ConnectorConnectionPoolFactory;
import com.example.cloudsql.TcpConnectionPoolFactory;
import com.example.cloudsql.TemplateData;
import com.example.cloudsql.Utils;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class Main implements HttpFunction {

  private Logger logger = Logger.getLogger(Main.class.getName());
  private static final Gson gson = new Gson();

  // Declared at cold-start, but only initialized if/when the function executes
  // Uses the "initialization-on-demand holder" idiom
  // More information: https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
  private static class PoolHolder {

    // Making the default constructor private prohibits instantiation of this class
    private PoolHolder() {
    }

    // This value is initialized only if (and when) the getInstance() function below is called
    private static final DataSource INSTANCE = setupPool();

    private static DataSource setupPool() {
      DataSource pool;
      if (System.getenv("INSTANCE_HOST") != null) {
        pool = TcpConnectionPoolFactory.createConnectionPool();
      } else {
        pool = ConnectorConnectionPoolFactory.createConnectionPool();
      }
      try {
        Utils.createTable(pool);
      } catch (SQLException ex) {
        throw new RuntimeException(
            "Unable to verify table schema. Please double check the steps"
                + "in the README and try again.",
            ex);
      }
      return pool;
    }

    private static DataSource getInstance() {
      return PoolHolder.INSTANCE;
    }
  }

  private void returnVoteCounts(HttpRequest req, HttpResponse resp)
      throws SQLException, IOException {
    DataSource pool = PoolHolder.getInstance();
    TemplateData templateData = TemplateData.getTemplateData(pool);
    JsonObject respContent = new JsonObject();

    // Return JSON Data
    respContent.addProperty("tabCount", templateData.tabCount);
    respContent.addProperty("spaceCount", templateData.spaceCount);
    respContent.addProperty("recentVotes", gson.toJson(templateData.recentVotes));
    resp.getWriter().write(respContent.toString());
    resp.setStatusCode(HttpURLConnection.HTTP_OK);
  }

  private void submitVote(HttpRequest req, HttpResponse resp) throws IOException {
    DataSource pool = PoolHolder.getInstance();
    Timestamp now = new Timestamp(new Date().getTime());
    JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
    String team = Utils.validateTeam(body.get("team").getAsString());
    if (team == null) {
      resp.setStatusCode(400);
      resp.getWriter().append("Invalid team specified.");
      return;
    }
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
      logger.log(Level.WARNING, "Error while attempting to submit vote.", ex);
      resp.setStatusCode(500);
      resp.getWriter()
          .write(
              "Unable to successfully cast vote! Please check the application "
                  + "logs for more details.");
    }
  }

  @Override
  public void service(HttpRequest req, HttpResponse resp) throws IOException, SQLException {

    String method = req.getMethod();
    switch (method) {
      case "GET":
        returnVoteCounts(req, resp);
        break;
      case "POST":
        submitVote(req, resp);
        break;
      default:
        resp.setStatusCode(HttpURLConnection.HTTP_BAD_METHOD);
        resp.getWriter().write(String.format("HTTP Method %s is not supported", method));
        break;
    }
  }
}
