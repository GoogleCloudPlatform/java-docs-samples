/*
 * Copyright 2018 Google Inc.
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

package com.example.dataflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Operation;
import com.google.cloud.spanner.ReadContext;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerException;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import com.google.spanner.admin.database.v1.CreateDatabaseMetadata;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class SpannerWriteIT {

  private String instanceId;
  private String databaseId;

  private Path singersPath;
  private Path albumsPath;
  private Spanner spanner;
  private SpannerOptions spannerOptions;

  @Before
  public void setUp() throws Exception {

    instanceId = System.getProperty("spanner.test.instance");
    databaseId = "df-spanner-write-it";

    spannerOptions = SpannerOptions.getDefaultInstance();
    spanner = spannerOptions.getService();

    DatabaseAdminClient adminClient = spanner.getDatabaseAdminClient();

    try {
      adminClient.dropDatabase(instanceId, databaseId);
    } catch (SpannerException e) {
      // Does not exist, ignore.
    }

    Operation<Database, CreateDatabaseMetadata> op = adminClient
        .createDatabase(instanceId, databaseId, Arrays.asList("CREATE TABLE Singers "
                + "(singerId INT64 NOT NULL, "
                + "firstName STRING(MAX) NOT NULL, lastName STRING(MAX) NOT NULL,) "
                + "PRIMARY KEY (singerId)",
            "CREATE TABLE Albums (singerId INT64 NOT NULL, "
                + "albumId INT64 NOT NULL, albumTitle STRING(MAX) NOT NULL,) "
                + "PRIMARY KEY (singerId, albumId)"));

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
  public void tearDown() {
    DatabaseAdminClient adminClient = spanner.getDatabaseAdminClient();
    try {
      adminClient.dropDatabase(instanceId, databaseId);
    } catch (SpannerException e) {
      // Failed to cleanup.
    }

    spanner.close();
  }

  @Test
  public void testEndToEnd() {
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
