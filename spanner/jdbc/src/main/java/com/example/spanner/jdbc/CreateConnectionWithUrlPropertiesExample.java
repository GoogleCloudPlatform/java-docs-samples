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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class CreateConnectionWithUrlPropertiesExample {

  static void createConnectionWithUrlProperties() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    createConnectionWithUrlProperties(projectId, instanceId, databaseId);
  }

  @SuppressFBWarnings(
      value = "OBL_UNSATISFIED_OBLIGATION",
      justification = "https://github.com/spotbugs/spotbugs/issues/293")
  // Creates a JDBC connection to a Cloud Spanner database using URL properties.
  static void createConnectionWithUrlProperties(
      String projectId, String instanceId, String databaseId) throws SQLException {
    try (Connection connection =
        DriverManager.getConnection(
            String.format(
                "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s"
                    + "?readonly=true;autocommit=false",
                projectId, instanceId, databaseId))) {
      System.out.printf("Readonly: %b%n", connection.isReadOnly());
      System.out.printf("Autocommit: %b%n", connection.getAutoCommit());
    }
  }
}
