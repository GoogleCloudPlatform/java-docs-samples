/*
 * Copyright 2020 Google Inc.
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

package com.example.spanner.jdbc;

import com.google.cloud.spanner.jdbc.CloudSpannerJdbcConnection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class CreateConnectionWithCustomHostExample {

  static void createConnectionWithCustomHost() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    int port = 9020;
    createConnectionWithCustomHost(projectId, instanceId, databaseId, port);
  }

  @SuppressFBWarnings(
      value = "OBL_UNSATISFIED_OBLIGATION",
      justification = "https://github.com/spotbugs/spotbugs/issues/293")
  // Creates a JDBC connection to a Cloud Spanner database on a custom host.
  static void createConnectionWithCustomHost(
      String projectId, String instanceId, String databaseId, int port) throws SQLException {
    // usePlainText=true in the connection URL will create an unsecured (i.e. no SSL) connection
    // to the specified host. This option must be specified when connecting to local mock servers
    // or emulators that do not use SSL.
    try (Connection connection =
        DriverManager.getConnection(
            String.format(
                "jdbc:cloudspanner://localhost:%d/projects/%s/instances/%s/databases/%s"
                    + "?usePlainText=true",
                port, projectId, instanceId, databaseId))) {
      System.out.printf(
          "Connected to %s%n",
          connection.unwrap(CloudSpannerJdbcConnection.class).getConnectionUrl());
    }
  }
}
