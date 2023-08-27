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

import com.example.spanner.jdbc.SingerProto.Genre;
import com.example.spanner.jdbc.SingerProto.SingerInfo;
import com.google.cloud.spanner.jdbc.ProtoEnumType;
import com.google.cloud.spanner.jdbc.ProtoMessageType;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

class ProtoColumnsInsertDataExample {

  // Class to contain Singer sample data.
  static class Singer {
    final long singerId;
    final String firstName;
    final String lastName;
    final SingerInfo singerInfo;
    final Genre singerGenre;

    Singer(
        long singerId,
        String firstName,
        String lastName,
        SingerInfo singerInfo,
        Genre singerGenre) {
      this.singerId = singerId;
      this.firstName = firstName;
      this.lastName = lastName;
      this.singerInfo = singerInfo;
      this.singerGenre = singerGenre;
    }
  }

  static final SingerInfo singerInfo =
      SingerInfo.newBuilder()
          .setSingerId(1)
          .setNationality("Country1")
          .setGenre(Genre.ROCK)
          .build();
  static final List<Singer> SINGERS =
      Arrays.asList(
          new Singer(1, "Marc", "Richards", singerInfo, Genre.ROCK),
          new Singer(2, "Catalina", "Smith", null, null));

  static void insertProtoColumnsData() throws SQLException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";
    insertProtoColumnsData(projectId, instanceId, databaseId);
  }

  static void insertProtoColumnsData(String projectId, String instanceId, String databaseId)
      throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner://staging-wrenchworks.sandbox.googleapis.com/projects/%s/instances/%s/databases/%s",
            projectId, instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl)) {
      try (PreparedStatement ps =
          connection.prepareStatement(
              "INSERT INTO Singer"
                  + " (SingerId, FirstName, LastName, SingerInfo, SingerGenre) VALUES (?, ?, ?, ?, ?)")) {
        for (Singer singer : SINGERS) {
          ps.setLong(1, singer.singerId);
          ps.setString(2, singer.firstName);
          ps.setString(3, singer.lastName);
          // Tell the JDBC driver that we want to set a PROTO MESSAGE value
          // by specifying the ProtoMessageType SQL type.
          ps.setObject(4, singer.singerInfo, ProtoMessageType.INSTANCE);
          // Tell the JDBC driver that we want to set a PROTO ENUM value
          // by specifying the ProtoEnumType SQL type.
          ps.setObject(5, singer.singerGenre, ProtoEnumType.INSTANCE);
          ps.addBatch();
        }
        int[] updateCounts = ps.executeBatch();
        System.out.printf("Insert counts: %s%n", Arrays.toString(updateCounts));
      }
    }
  }
}
