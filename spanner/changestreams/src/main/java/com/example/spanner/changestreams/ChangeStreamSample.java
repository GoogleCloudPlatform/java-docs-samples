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

package com.example.spanner.changestreams;

// TODO(haikuo@google.com): we should remove the models and the mapper once the change stream
//  connector code that includes these models are released into Beam repo.
import com.example.spanner.changestreams.model.ChangeStreamRecord;
import com.example.spanner.changestreams.model.ChildPartition;
import com.example.spanner.changestreams.model.ChildPartitionsRecord;
import com.example.spanner.changestreams.model.DataChangeRecord;
import com.example.spanner.changestreams.model.HeartbeatRecord;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import com.google.common.collect.ImmutableList;
import com.google.spanner.admin.database.v1.UpdateDatabaseDdlMetadata;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample code for querying change stream, it:
 * 1. Creates a table with simple schema and a change stream that watches the table.
 * 2. Inserts test data into the table.
 * 3. Executes a change stream initial query to get change stream partition tokens.
 * 4. Executes a change stream partition query to get data change records of the inserted rows.
 * 5. Drops the created table and change stream.
 */
public class ChangeStreamSample {
  private static final long TIMEOUT_MINUTES = 2;

  public static void run(String instanceId, String databaseId, String prefix) {
    final String tableName = prefix + "_Singers";
    final String changeStreamName = prefix + "_ChangeStreamSingers";
    final SpannerOptions options = SpannerOptions.newBuilder().build();
    final Spanner spanner = options.getService();
    final DatabaseAdminClient dbAdminClient = spanner.getDatabaseAdminClient();
    final DatabaseClient dbClient = spanner.getDatabaseClient(
        DatabaseId.of(options.getProjectId(), instanceId, databaseId));

    try {
      createChangeStream(dbAdminClient, instanceId, databaseId, tableName, changeStreamName);
      queryChangeStream(dbClient, tableName, changeStreamName);
      dropChangeStream(dbAdminClient, instanceId, databaseId, tableName, changeStreamName);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      spanner.close();
    }
  }

  // [START spanner_change_streams_sample_create_change_streams]
  static void createChangeStream(
      DatabaseAdminClient dbAdminClient, String instanceId, String databaseId, String tableName,
      String changeStreamName) throws Exception {
    System.out.println(
        String.format("Updating database DDL to create table %s and change stream %s.",
          tableName, changeStreamName));

    OperationFuture<Void, UpdateDatabaseDdlMetadata> op = dbAdminClient
        .updateDatabaseDdl(
        instanceId,
        databaseId,
        Arrays.asList(
          String.format("CREATE TABLE %s ("
            + "  SingerId   INT64 NOT NULL,"
            + "  FirstName  STRING(1024),"
            + "  LastName   STRING(1024)"
            + ") PRIMARY KEY (SingerId)", tableName),
          String.format("CREATE CHANGE STREAM %s FOR %s", changeStreamName, tableName)),
        null
      );

    op.get(TIMEOUT_MINUTES, TimeUnit.MINUTES);
  }
  // [END spanner_change_streams_sample_create_change_streams]

  // [START spanner_change_streams_sample_query_change_streams]
  public static void queryChangeStream(DatabaseClient dbClient, String tableName,
                                       String changeStreamName) {
    // Insert test data into the table.
    System.out.println(
        String.format("Inserting rows "
          + "(1, singer_1_first_name, singer_1_last_name) and "
          + "(2, singer_2_first_name, singer_2_last_name) "
          + "into %s table.", tableName));

    insertRows(dbClient, tableName);

    final Timestamp startTimestamp = Timestamp.now();
    // end = start + 30 seconds.
    final Timestamp endTimestamp = Timestamp.ofTimeSecondsAndNanos(
        startTimestamp.getSeconds() + 30, startTimestamp.getNanos());

    final ChangeStreamRecordMapper changeStreamRecordMapper =
        new ChangeStreamRecordMapper();

    // Execute an initial query to get partition tokens.
    System.out.println("Executing change stream initial query.");
    // For initial query the partition token is null.
    List<ChangeStreamRecord> initialQueryRecords = executeChangeStreamQueryAndPrint(
        dbClient, changeStreamName, startTimestamp, endTimestamp, null,
        changeStreamRecordMapper);

    System.out.println("Executing change stream partition queries.");
    for (ChangeStreamRecord record : initialQueryRecords) {
      // Executes a partition query to print data records that we just inserted.
      if (record instanceof ChildPartitionsRecord) {
        ChildPartitionsRecord childPartitionsRecord = (ChildPartitionsRecord) record;
        for (ChildPartition childPartition : childPartitionsRecord.getChildPartitions()) {
          executeChangeStreamQueryAndPrint(
              dbClient, changeStreamName, childPartitionsRecord.getStartTimestamp(), endTimestamp,
              childPartition.getToken(), changeStreamRecordMapper);
        }
      } else if (record instanceof DataChangeRecord) {
        throw new IllegalArgumentException("Got unexpected DataChangeRecord from Change Streams "
          + "initial query");
      }
    }
  }

  // Insert two rows into Singers table.
  static void insertRows(DatabaseClient client, String tableName) {
    client.write(
        ImmutableList.of(
        Mutation.newInsertOrUpdateBuilder(tableName)
          .set("SingerId").to(1)
          .set("FirstName").to("singer_1_first_name")
          .set("LastName").to("singer_1_last_name")
          .build(),
        Mutation.newInsertOrUpdateBuilder(tableName)
          .set("SingerId").to(2)
          .set("FirstName").to("singer_2_first_name")
          .set("LastName").to("singer_2_last_name")
          .build()
      )
    );
  }

  // Execute a change stream query, return and print out the result records.
  // For initial query, partitionToken is expected to be null.
  public static List<ChangeStreamRecord> executeChangeStreamQueryAndPrint(
      DatabaseClient dbClient, String changeStreamName, Timestamp startTimestamp,
      Timestamp endTimestamp, String partitionToken,
      ChangeStreamRecordMapper changeStreamRecordMapper) {
    System.out.println("Executing a change stream query with: "
        + "start_timestamp => " + startTimestamp
        + ", end_timestamp => " + endTimestamp
        + ", partition_token => " + partitionToken
        + ", heartbeat_milliseconds => 5000");

    final String query =
        String.format("SELECT * FROM READ_%s ("
          + "start_timestamp => @startTimestamp,"
          + "end_timestamp => @endTimestamp,"
          + "partition_token => @partitionToken,"
          + "heartbeat_milliseconds => @heartbeatMillis"
          + ")", changeStreamName);

    final ResultSet resultSet =
        dbClient
          .singleUse()
          .executeQuery(
            Statement.newBuilder(query)
              .bind("startTimestamp").to(startTimestamp)
              .bind("endTimestamp").to(endTimestamp)
              .bind("partitionToken").to(partitionToken)
              .bind("heartbeatMillis").to(5000)
              .build());

    List<ChangeStreamRecord> result = new ArrayList<>();
    while (resultSet.next()) {
      // Parses result set into change stream result format.
      final List<ChangeStreamRecord> records =
          changeStreamRecordMapper.toChangeStreamRecords(resultSet.getCurrentRowAsStruct());

      // Prints out all the query results.
      for (final ChangeStreamRecord record : records) {
        if (record instanceof DataChangeRecord) {
          System.out.println("Received a DataChangeRecord: " + record);
        } else if (record instanceof HeartbeatRecord) {
          System.out.println("Received a HeartbeatRecord: " + record);
        } else if (record instanceof ChildPartitionsRecord) {
          System.out.println("Received a ChildPartitionsRecord: " + record);
        } else {
          // We should never reach here.
          throw new IllegalArgumentException("Unknown record type " + record.getClass());
        }
      }
      result.addAll(records);
    }

    return result;
  }
  // [END spanner_change_streams_sample_query_change_streams]

  // [START spanner_change_streams_sample_drop_change_streams]
  public static void dropChangeStream(
      DatabaseAdminClient dbAdminClient, String instanceId, String databaseId, String tableName,
      String changeStreamName) throws Exception {
    System.out.println(
        String.format("Updating database DDL to drop table %s and change stream %s.",
          tableName, changeStreamName));

    OperationFuture<Void, UpdateDatabaseDdlMetadata> op = dbAdminClient
        .updateDatabaseDdl(
        instanceId,
        databaseId,
        Arrays.asList(
          "DROP CHANGE STREAM " + changeStreamName,
          "DROP TABLE " + tableName),
        null
      );

    op.get(TIMEOUT_MINUTES, TimeUnit.MINUTES);
  }
  // [END spanner_change_streams_sample_drop_change_streams]
}
