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
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SpannerWriteIT {

  final String instanceId = "mairbek-deleteme";
  final String databaseId = "test2";

  Path singersPath;
  Path albumsPath;
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
        .createDatabase(instanceId, databaseId, Arrays.asList("CREATE TABLE Singers (singerId INT64 NOT NULL, firstName STRING(MAX) NOT NULL, lastName STRING(MAX) NOT NULL,) PRIMARY KEY (singerId)",
            "CREATE TABLE Albums (singerId INT64 NOT NULL, albumId INT64 NOT NULL, albumTitle STRING(MAX) NOT NULL,) PRIMARY KEY (singerId, albumId)"));

    op.waitFor();

    String singers = Stream
        .of("1\tJohn\tLennon", "2\tPaul\tMccartney", "3\tGeorge\tHarrison", "4\tRingo\tStarr")
        .collect(Collectors.joining("\n"));
    singersPath = Files.createTempFile("singers", "txt");
    Files.write(singersPath, singers.getBytes());

    String albums = Stream
        .of("1\t1\tImagine", "2\t1\tPipes of Peace", "3\t1\tDark Horse")
        .collect(Collectors.joining("\n"));
    albumsPath = Files.createTempFile("albums", "txt");
    Files.write(albumsPath, albums.getBytes());


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
    SpannerWrite.main(new String[] { "--instanceId=" + instanceId, "--databaseId=" + databaseId,
        "--singersFilename=" + singersPath, "--albumsFilename=" + albumsPath,
        "--runner=DirectRunner" });

    DatabaseClient dbClient = getDbClient();
    try (ReadContext context = dbClient.singleUse()) {
      ResultSet rs = context.executeQuery(
          Statement.of("SELECT COUNT(*) FROM singers"));
      assertTrue(rs.next());
      assertEquals(4, rs.getLong(0));

    }
    try (ReadContext context = dbClient.singleUse()) {
      ResultSet rs = context.executeQuery(Statement.of("SELECT COUNT(*) FROM albums"));
      assertTrue(rs.next());
      assertEquals(3, rs.getLong(0));
    }
  }

  private DatabaseClient getDbClient() {
    return spanner
        .getDatabaseClient(DatabaseId.of(spannerOptions.getProjectId(), instanceId, databaseId));
  }

}