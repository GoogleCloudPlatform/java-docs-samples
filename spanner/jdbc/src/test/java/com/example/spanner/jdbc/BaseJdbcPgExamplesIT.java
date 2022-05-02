/*
 * Copyright 2022 Google Inc.
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

import com.google.cloud.ServiceOptions;
import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Dialect;
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
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration tests for Cloud Spanner PostgreSQL JDBC examples. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public abstract class BaseJdbcPgExamplesIT {
  // The instance needs to exist for tests to pass.
  protected static String instanceId = System.getProperty("spanner.test.instance");
  protected static final String databaseId =
      formatForTest(System.getProperty("spanner.sample.pgdatabase", "mypgsample"));
  protected static DatabaseId dbId;
  private static DatabaseAdminClient dbClient;
  private boolean testTableCreated;

  protected interface JdbcRunnable {
    void run() throws Exception;
  }

  protected String runExample(JdbcRunnable example) {
    PrintStream stdOut = System.out;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
    try {
      example.run();
    } catch (Exception e) {
      e.printStackTrace();
    }
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
    Database database = dbClient.newDatabaseBuilder(dbId).setDialect(Dialect.POSTGRESQL).build();
    dbClient.createDatabase(database, Collections.emptyList()).get();
  }

  @AfterClass
  public static void dropTestDatabase() {
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
          new Singer(5, "David", "Lomond", new BigDecimal("311399.26")),
          new Singer(6, "Bruce", "Allison", null),
          new Singer(7, "Alice", "Bruxelles", null));

  protected boolean createTestTable() {
    return false;
  }

  @Before
  public void insertTestData() throws SQLException {
    if (createTestTable()) {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              ServiceOptions.getDefaultProjectId(), instanceId, databaseId);
      try (Connection connection = DriverManager.getConnection(connectionUrl)) {
        if (!testTableCreated) {
          connection
              .createStatement()
              .execute(
                  "CREATE TABLE Singers (\n"
                      + "  SingerId   BIGINT NOT NULL PRIMARY KEY,\n"
                      + "  FirstName  VARCHAR(1024),\n"
                      + "  LastName   VARCHAR(1024),\n"
                      + "  SingerInfo BYTEA,\n"
                      + "  Revenues   NUMERIC\n"
                      + ")\n");
          testTableCreated = true;
        }
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
  }

  @After
  public void removeTestData() throws SQLException {
    if (createTestTable()) {
      String connectionUrl =
          String.format(
              "jdbc:cloudspanner:/projects/%s/instances/%s/databases/%s",
              ServiceOptions.getDefaultProjectId(), instanceId, databaseId);
      try (Connection connection = DriverManager.getConnection(connectionUrl);
          Statement statement = connection.createStatement()) {
        statement.execute("DELETE FROM Singers WHERE 1=1");
      }
    }
  }

  static String formatForTest(String name) {
    return name + "-" + UUID.randomUUID().toString().substring(0, 18);
  }
}