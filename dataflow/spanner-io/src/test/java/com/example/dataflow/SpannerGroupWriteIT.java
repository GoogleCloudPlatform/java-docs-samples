package com.example.dataflow;

import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Operation;
import com.google.cloud.spanner.ReadContext;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerException;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.TransactionContext;
import com.google.cloud.spanner.TransactionRunner;
import com.google.spanner.admin.database.v1.CreateDatabaseMetadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SpannerGroupWriteIT {

  private final String instanceId = "mairbek-deleteme";
  private final String databaseId = "test2";

  Path tempPath;
  Spanner spanner;
  SpannerOptions spannerOptions;

  @Before
  public void setUp() throws Exception {

    spannerOptions = SpannerOptions.getDefaultInstance();
    spanner = spannerOptions.getService();

    DatabaseAdminClient adminClient = spanner.getDatabaseAdminClient();

    try {
      adminClient.dropDatabase(instanceId, databaseId);
    } catch (SpannerException e) {
      // Does not exist, ignore.
    }

    Operation<Database, CreateDatabaseMetadata> op = adminClient
        .createDatabase(instanceId, databaseId, Arrays.asList("CREATE TABLE users ("
                + "id STRING(MAX) NOT NULL, state STRING(MAX) NOT NULL) PRIMARY KEY (id)",
            "CREATE TABLE PendingReviews (id INT64, action STRING(MAX), note STRING(MAX), userId STRING(MAX),) PRIMARY KEY (id)"));

    op.waitFor();

    DatabaseClient dbClient = getDbClient();

    List<Mutation> mutations = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      mutations.add(
          Mutation.newInsertBuilder("users").set("id").to(Integer.toString(i)).set("state")
              .to("ACTIVE").build());
    }
    TransactionRunner runner = dbClient.readWriteTransaction();
    runner.run(new TransactionRunner.TransactionCallable<Void>() {

      @Nullable
      @Override
      public Void run(TransactionContext tx) throws Exception {
        tx.buffer(mutations);
        return null;
      }
    });

    String content = IntStream.range(0, 10).mapToObj(Integer::toString)
        .collect(Collectors.joining("\n"));
    tempPath = Files.createTempFile("suspicious-ids", "txt");
    Files.write(tempPath, content.getBytes());
  }

  @After
  public void tearDown() throws Exception {
    DatabaseAdminClient adminClient = spanner.getDatabaseAdminClient();
    try {
      adminClient.dropDatabase(instanceId, databaseId);
    } catch (SpannerException e) {
      // Failed to cleanup.
    }

    spanner.close();
  }

  @Test
  public void testEndToEnd() throws Exception {
    SpannerGroupWrite.main(
        new String[] { "--instanceId=" + instanceId, "--databaseId=" + databaseId,
            "--suspiciousUsersFile=" + tempPath, "--runner=DirectRunner" });

    DatabaseClient dbClient = getDbClient();
    try (ReadContext context = dbClient.singleUse()) {
      ResultSet rs = context.executeQuery(
          Statement.newBuilder("SELECT COUNT(*) FROM users WHERE STATE = @state").bind("state")
              .to("BLOCKED").build());
      assertTrue(rs.next());
      assertEquals(10, rs.getLong(0));

    }
    try (ReadContext context = dbClient.singleUse()) {
      ResultSet rs = context.executeQuery(
          Statement.newBuilder("SELECT COUNT(*) FROM PendingReviews WHERE ACTION = @action")
              .bind("action").to("REVIEW ACCOUNT").build());
      assertTrue(rs.next());
      assertEquals(10, rs.getLong(0));
    }
  }

  private DatabaseClient getDbClient() {
    return spanner
        .getDatabaseClient(DatabaseId.of(spannerOptions.getProjectId(), instanceId, databaseId));
  }

}