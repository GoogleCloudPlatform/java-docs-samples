/*
 * Copyright 2023 Google LLC
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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Arrays;

class ProtoColumnsCreateTableExample {

  static void createTableWithProtoDataType() throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    createTableWithProtoDataType(projectId, instanceId, databaseId);
  }

  static void createTableWithProtoDataType(String projectId, String instanceId, String databaseId)
      throws Exception {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner://staging-wrenchworks.sandbox.googleapis.com/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    File file = new File("src/test/resources/descriptors.pb");
    InputStream in = new FileInputStream(file);
    try (Connection connection = DriverManager.getConnection(connectionUrl)) {
      CloudSpannerJdbcConnection spannerConnection =
          connection.unwrap(CloudSpannerJdbcConnection.class);
      spannerConnection.setProtoDescriptors(in);
      spannerConnection.setAutoCommit(false);
      Statement statement = spannerConnection.createStatement();
      statement.addBatch(
          "CREATE PROTO BUNDLE ("
              + "spanner.examples.music.SingerInfo,"
              + "spanner.examples.music.Genre,"
              + ")");
      statement.addBatch(
          "CREATE TABLE Singer (\n"
              + "  SingerId   INT64 NOT NULL,\n"
              + "  FirstName  STRING(1024),\n"
              + "  LastName   STRING(1024),\n"
              + "  SingerInfo spanner.examples.music.SingerInfo,\n"
              + "  SingerGenre   spanner.examples.music.Genre,\n"
              + ") PRIMARY KEY (SingerId)\n");
      int[] val = statement.executeBatch();
      System.out.printf(
          "Created table with Proto Message and Enum data type. DDL counts: %s",
          Arrays.toString(val));
    }
  }
}
