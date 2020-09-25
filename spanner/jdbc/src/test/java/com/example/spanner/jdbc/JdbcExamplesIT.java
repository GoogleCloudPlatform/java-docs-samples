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

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.ServiceOptions;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Instance;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.connection.ConnectionOptions;
import com.google.cloud.spanner.jdbc.CloudSpannerJdbcConnection;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration tests for Cloud Spanner JDBC examples. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class JdbcExamplesIT {
  // The instance needs to exist for tests to pass.
  private static String instanceId = System.getProperty("spanner.test.instance");
  private static String databaseId =
      formatForTest(System.getProperty("spanner.sample.database", "mysample"));
  private static DatabaseId dbId;
  private static DatabaseAdminClient dbClient;

  private interface JdbcRunnable {
    public void run() throws SQLException;
  }

  private String runExample(JdbcRunnable example) throws SQLException {
    PrintStream stdOut = System.out;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
    example.run();
    System.setOut(stdOut);
    return bout.toString();
  }

  @BeforeClass
  public static void createTestDatabase() throws Exception {
    SpannerOptions options = SpannerOptions.newBuilder().build();
    Spanner spanner = options.getService();
    dbClient = spanner.getDatabaseAdminClient();
    if (instanceId == null) {
      Iterator<Instance> iterator =
          spanner.getInstanceAdminClient().listInstances().iterateAll().iterator();
      if (iterator.hasNext()) {
        instanceId = iterator.next().getId().getInstance();
      }
    }
    dbId = DatabaseId.of(options.getProjectId(), instanceId, databaseId);
    dbClient.dropDatabase(dbId.getInstanceId().getInstance(), dbId.getDatabase());
    dbClient.createDatabase(instanceId, databaseId, Collections.emptyList()).get();
    CreateTableExample.createTable(options.getProjectId(), instanceId, databaseId);
  }

  @AfterClass
  public static void dropTestDatabase() throws Exception {
    ConnectionOptions.closeSpanner();
    dbClient.dropDatabase(dbId.getInstanceId().getInstance(), dbId.getDatabase());
  }

  static class Singer {
    final long singerId;
    final String firstName;
    final String lastName;
    final BigDecimal revenues;

    Singer(long singerId, String firstName, String lastName, BigDecimal revenues) {
      this.singerId = singerId;
      this.firstName = firstName;
      this.lastName = lastName;
      this.revenues = revenues;
    }
  }

  static final List<Singer> TEST_SINGERS =
      Arrays.asList(
          new Singer(1, "Marc", "Richards", new BigDecimal("104100.00")),
          new Singer(2, "Catalina", "Smith", new BigDecimal("9880.99")),
          new Singer(3, "Alice", "Trentor", new BigDecimal("300183")),
          new Singer(4, "Lea", "Martin", new BigDecimal("20118.12")),
          new Singer(5, "David", "Lomond", new BigDecimal("311399.26")));

  @Before
  public void insertTestData() throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            ServiceOptions.getDefaultProjectId(), instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl)) {
      CloudSpannerJdbcConnection spannerConnection =
          connection.unwrap(CloudSpannerJdbcConnection.class);
      spannerConnection.setAutoCommit(false);
      for (Singer singer : TEST_SINGERS) {
        spannerConnection.bufferedWrite(
            Mutation.newInsertBuilder("Singers")
                .set("SingerId")
                .to(singer.singerId)
                .set("FirstName")
                .to(singer.firstName)
                .set("LastName")
                .to(singer.lastName)
                .set("Revenues")
                .to(singer.revenues)
                .build());
      }
      connection.commit();
    }
  }

  @After
  public void removeTestData() throws SQLException {
    String connectionUrl =
        String.format(
            "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
            ServiceOptions.getDefaultProjectId(), instanceId, databaseId);
    try (Connection connection = DriverManager.getConnection(connectionUrl);
        Statement statement = connection.createStatement()) {
      statement.execute("DELETE FROM Singers WHERE 1=1");
    }
  }

  @Test
  public void createConnection_shouldConnectToSpanner() throws SQLException {
    String out =
        runExample(
            () ->
                CreateConnectionExample.createConnection(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Connected to Cloud Spanner at [");
  }

  @Test
  public void createConnectionWithCredentials_shouldConnectToSpanner() throws SQLException {
    String credentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
    String out =
        runExample(
            () ->
                CreateConnectionWithCredentialsExample.createConnectionWithCredentials(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId, credentials));
    assertThat(out).contains("Connected to Cloud Spanner at [");
  }

  @Test
  public void createConnectionWithDefaultProjectId_shouldConnectToSpanner() throws SQLException {
    String out =
        runExample(
            () ->
                CreateConnectionWithDefaultProjectIdExample.createConnectionWithDefaultProjectId(
                    instanceId, databaseId));
    assertThat(out).contains("Connected to Cloud Spanner at [");
  }

  @Test
  public void createConnectionWithUrlProperties_shouldConnectToSpanner() throws SQLException {
    String out =
        runExample(
            () ->
                CreateConnectionWithUrlPropertiesExample.createConnectionWithUrlProperties(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Readonly: true");
    assertThat(out).contains("Autocommit: false");
  }

  @Test
  public void createConnectionWithProperties_shouldConnectToSpanner() throws SQLException {
    String out =
        runExample(
            () ->
                CreateConnectionWithPropertiesExample.createConnectionWithProperties(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Readonly: true");
    assertThat(out).contains("Autocommit: false");
  }

  @Test
  public void createConnectionWithDataSource_shouldConnectToSpanner() throws SQLException {
    String out =
        runExample(
            () ->
                CreateConnectionWithDataSourceExample.createConnectionWithDataSource(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Readonly: true");
    assertThat(out).contains("Autocommit: false");
  }

  @Test
  public void abortBatch_shouldAbortBatch() throws SQLException {
    String out =
        runExample(
            () ->
                AbortBatchExample.abortBatch(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Aborted DML batch");
  }

  @Test
  public void autocommitUpdate_shouldUpdateData() throws SQLException {
    String out =
        runExample(
            () ->
                AutocommitUpdateDataExample.update(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Inserted 1 row(s)");
  }

  @Test
  public void batchDdl_shouldCreateTables() throws SQLException {
    String out =
        runExample(
            () ->
                BatchDdlExample.batchDdl(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("DDL update counts: [-2, -2]");
  }

  @Test
  public void batchDdlUsingSqlStatements_shouldCreateTables() throws SQLException {
    String out =
        runExample(
            () ->
                BatchDdlUsingSqlStatementsExample.batchDdlUsingSqlStatements(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Update count for CREATE TABLE Concerts: -2");
    assertThat(out).contains("Update count for CREATE INDEX SingersByFirstLastName: -2");
    assertThat(out).contains("Executed DDL batch");
  }

  @Test
  public void batchDml_shouldInsertData() throws SQLException {
    String out =
        runExample(
            () ->
                BatchDmlExample.batchDml(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Batch insert counts: [1, 1, 1]");
  }

  @Test
  public void batchDmlUsingSqlStatements_shouldInsertData() throws SQLException {
    String out =
        runExample(
            () ->
                BatchDmlUsingSqlStatementsExample.batchDmlUsingSqlStatements(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Batch insert counts: [1, 1, 1]");
  }

  @Test
  public void bufferedWrite_shouldInsertData() throws SQLException {
    String out =
        runExample(
            () ->
                BufferedWriteExample.bufferedWrite(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Transaction committed at [");
  }

  @Test
  public void spannerJdbcConnectionWithQueryOtions_shouldUseOptimizerVersion() throws SQLException {
    String out =
        runExample(
            () ->
                ConnectionWithQueryOptionsExample.connectionWithQueryOptions(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("1 Marc Richards");
    assertThat(out).contains("Optimizer version: 1");
  }

  @Test
  public void getCommitTimestampExample_shouldGetCommitTimestamp() throws SQLException {
    String out =
        runExample(
            () ->
                GetCommitTimestampExample.getCommitTimestamp(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Commit timestamp: [");
  }

  @Test
  public void getReadTimestampExample_shouldGetReadTimestamp() throws SQLException {
    String out =
        runExample(
            () ->
                GetReadTimestampExample.getReadTimestamp(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Read timestamp: [");
  }

  @Test
  public void insertData_shouldInsertData() throws SQLException {
    String out =
        runExample(
            () ->
                InsertDataExample.insertData(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Insert counts: [1, 1, 1, 1, 1]");
  }

  @Test
  public void partitionedDml_shouldUpdateData() throws SQLException {
    String out =
        runExample(
            () ->
                PartitionedDmlExample.partitionedDml(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Updated 5 row(s)");
  }

  @Test
  public void readOnlyTransaction_shouldReadData() throws SQLException {
    String out =
        runExample(
            () ->
                ReadOnlyTransactionExample.readOnlyTransaction(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("1 Marc Richards 104100");
    assertThat(out).contains("2 Catalina Smith 9880.99");
    assertThat(out).contains("Read-only transaction used read timestamp [");
  }

  @Test
  public void readWriteTransaction_shouldWriteData() throws SQLException {
    String out =
        runExample(
            () ->
                ReadWriteTransactionExample.readWriteTransaction(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Transaction committed with commit timestamp [");
  }

  @Test
  public void spannerJdbcSetStatementForQueryOptions_shouldUseOptimizerVersion()
      throws SQLException {
    String out =
        runExample(
            () ->
                SetQueryOptionsExample.setQueryOptions(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("1 Marc Richards");
    assertThat(out).contains("Optimizer version: 1");
  }

  @Test
  public void singleUseReadOnly_shouldReturnData() throws SQLException {
    String out =
        runExample(
            () ->
                SingleUseReadOnlyExample.singleUseReadOnly(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("1 Marc Richards 104100");
    assertThat(out).contains("2 Catalina Smith 9880.99");
  }

  @Test
  public void singleUseReadOnlyTimestampBound_shouldNotReturnData() throws SQLException {
    String out =
        runExample(
            () ->
                SingleUseReadOnlyTimestampBoundExample.singleUseReadOnlyTimestampBound(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Read timestamp used:");
  }

  @Test
  public void transactionWithRetryLoop_shouldCommit() throws SQLException {
    String out =
        runExample(
            () ->
                TransactionWithRetryLoopExample.transactionWithRetryLoop(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Transaction committed at [");
  }

  @Test
  public void transactionWithRetryLoopUsingOnlyJdbc_shouldCommit() throws SQLException {
    String out =
        runExample(
            () ->
                TransactionWithRetryLoopUsingOnlyJdbcExample.genericJdbcTransactionWithRetryLoop(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertThat(out).contains("Transaction committed at [");
  }

  static String formatForTest(String name) {
    return name + "-" + UUID.randomUUID().toString().substring(0, 20);
  }
}
