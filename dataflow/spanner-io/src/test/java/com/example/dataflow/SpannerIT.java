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
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SpannerIT {

  private String runSample(Consumer<Void> main) throws Exception {
    PrintStream stdOut = System.out;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
    main.accept(null);
    System.setOut(stdOut);
    return bout.toString();
  }

  @Test
  public void testSpannerGroupWrite() throws Exception {
    Path tempPath = Files.createTempFile("suspicious-ids", "txt");

    String instanceId = "mairbek-deleteme";
    String databaseId  = "test2";

    SpannerOptions options = SpannerOptions.getDefaultInstance();
    Spanner spanner = options.getService();

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

    DatabaseClient dbClient = spanner.getDatabaseClient(DatabaseId.of(options.getProjectId(), instanceId, databaseId));

    List<Mutation> mutations = new ArrayList<>();
    for (int i = 0; i< 10; i++) {
      mutations.add(Mutation.newInsertBuilder("users").set("id").to(Integer.toString(i)).set("state").to("ACTIVE").build());
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
    Files.write(tempPath, content.getBytes());


    String out = runSample(v -> SpannerGroupWrite.main(new String[] { "--instanceId=" + instanceId, "--databaseId=" + databaseId,
          "--suspiciousUsersFile=" + tempPath, "--runner=DirectRunner" }));

    System.out.println(out);

    try (ReadContext context = dbClient.singleUse()) {
      ResultSet rs = context.executeQuery(
          Statement.newBuilder("SELECT COUNT(*) FROM users WHERE STATE = @state").bind("state").to("BLOCKED")
              .build());
      assertTrue(rs.next());
      assertEquals(10, rs.getLong(0));

    }
    try (ReadContext context = dbClient.singleUse()) {
      ResultSet rs = context.executeQuery(
          Statement.newBuilder("SELECT COUNT(*) FROM PendingReviews WHERE ACTION = @action").bind("action").to("REVIEW ACCOUNT").build());
      assertTrue(rs.next());
      assertEquals(10, rs.getLong(0));
    }
  }
}