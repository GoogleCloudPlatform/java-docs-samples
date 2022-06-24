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
import com.google.cloud.spanner.Dialect;
import com.google.cloud.spanner.ReadContext;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerException;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import com.google.common.collect.ImmutableList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@SuppressWarnings("checkstyle:abbreviationaswordinname")
@RunWith(Parameterized.class)
public class SpannerWriteIT {

  @Parameter
  public Dialect dialect;

  @Parameters(name = "dialect = {0}")
  public static List<Object[]> data() {
    List<Object[]> parameters = new ArrayList<>();
    for (Dialect dialect : Dialect.values()) {
      parameters.add(new Object[] {dialect});
    }
    return parameters;
  }

  private final Random random = new Random();
  private String instanceId;
  private String databaseId;

  private Path singersPath;
  private Path albumsPath;
  private Spanner spanner;
  private SpannerOptions spannerOptions;

  @Before
  public void setUp() throws Exception {

    instanceId = System.getProperty("spanner.test.instance");
    databaseId = "df-spanner-write-it-" + random.nextInt(1000000000);

    spannerOptions = SpannerOptions.getDefaultInstance();
    spanner = spannerOptions.getService();

    DatabaseAdminClient adminClient = spanner.getDatabaseAdminClient();

    try {
      adminClient.dropDatabase(instanceId, databaseId);
    } catch (SpannerException e) {
      // Does not exist, ignore.
    }

    if (dialect == Dialect.POSTGRESQL) {
      Database database =
          adminClient
              .newDatabaseBuilder(
                  DatabaseId.of(spannerOptions.getProjectId(), instanceId, databaseId))
              .setDialect(Dialect.POSTGRESQL)
              .build();
      adminClient.createDatabase(database, ImmutableList.of()).get();
      adminClient.updateDatabaseDdl(
          instanceId,
          databaseId,
          Arrays.asList(
              "CREATE TABLE Singers "
                  + "(singerId bigint NOT NULL primary key, firstName varchar NOT NULL, "
                  + "lastName varchar NOT NULL)",
              "CREATE TABLE Albums (singerId bigint NOT NULL, albumId bigint NOT NULL, "
                  + "albumTitle varchar NOT NULL, PRIMARY KEY (singerId, albumId))"),
          null).get();
    } else {
      adminClient
          .createDatabase(
              instanceId,
              databaseId,
              Arrays.asList(
                  "CREATE TABLE Singers "
                      + "(singerId INT64 NOT NULL, firstName STRING(MAX) NOT NULL, "
                      + "lastName STRING(MAX) NOT NULL,) PRIMARY KEY (singerId)",
                  "CREATE TABLE Albums (singerId INT64 NOT NULL, albumId INT64 NOT NULL, "
                      + "albumTitle STRING(MAX) NOT NULL,) PRIMARY KEY (singerId, albumId)"))
          .get();
    }

    String singers =
        Stream.of("1\tJohn\tLennon", "2\tPaul\tMccartney", "3\tGeorge\tHarrison", "4\tRingo\tStarr")
            .collect(Collectors.joining("\n"));
    singersPath = Files.createTempFile("singers", "txt");
    Files.write(singersPath, singers.getBytes());

    String albums =
        Stream.of("1\t1\tImagine", "2\t1\tPipes of Peace", "3\t1\tDark Horse")
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
    SpannerWrite.main(
        new String[] {
          "--instanceId=" + instanceId,
          "--databaseId=" + databaseId,
          "--singersFilename=" + singersPath,
          "--albumsFilename=" + albumsPath,
          "--runner=DirectRunner",
          "--dialect=" + dialect
        });

    DatabaseClient dbClient = getDbClient();
    try (ReadContext context = dbClient.singleUse()) {
      ResultSet rs = context.executeQuery(Statement.of("SELECT COUNT(*) FROM singers"));
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
    return spanner.getDatabaseClient(
        DatabaseId.of(spannerOptions.getProjectId(), instanceId, databaseId));
  }
}
