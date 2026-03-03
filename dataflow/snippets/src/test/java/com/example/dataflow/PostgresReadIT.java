/*
 * Copyright 2026 Google LLC
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.apache.beam.sdk.PipelineResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresReadIT {

  private static final String TABLE_NAME = "test_table";
  private static final String OUTPUT_PATH = "test-output";
  // The TextIO connector appends this suffix to the pipeline output file.
  private static final String OUTPUT_FILE_SUFFIX = "-00000-of-00001.txt";
  private static final String OUTPUT_FILE_NAME = OUTPUT_PATH + OUTPUT_FILE_SUFFIX;

  private static final PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15-alpine");

  @Before
  public void setUp() throws Exception {
    postgres.start();

    // Initialize the database with table and data
    try (Connection conn = DriverManager.getConnection(
        postgres.getJdbcUrl(),
        postgres.getUsername(),
        postgres.getPassword())) {

      Statement stmt = conn.createStatement();
      stmt.execute(String.format("CREATE TABLE %s (id INT PRIMARY KEY, name VARCHAR(255))", TABLE_NAME));
      stmt.execute(String.format("INSERT INTO %s (id, name) VALUES (1, 'John Doe')", TABLE_NAME));
      stmt.execute(String.format("INSERT INTO test_table (id, name) VALUES (2, 'Jane Smith')", TABLE_NAME));
    }
  }

  @After
  public void tearDown() throws IOException {
    if (postgres != null) {
      postgres.stop();
    }
    Files.deleteIfExists(Paths.get(OUTPUT_FILE_NAME));
  }

  @Test
  public void testPostgresRead() throws IOException {
    // Execute the Beam pipeline
    PipelineResult.State state = PostgresRead.main(new String[] {
        "--runner=DirectRunner",
        "--jdbcUrl=" + postgres.getJdbcUrl(),
        "--table=" + TABLE_NAME,
        "--username=" + postgres.getUsername(),
        "--password=" + postgres.getPassword(),
        "--outputPath=" + OUTPUT_PATH
    });

    assertEquals(PipelineResult.State.DONE, state);
    verifyOutput();
  }

  private void verifyOutput() throws IOException {
    File outputFile = new File(OUTPUT_FILE_NAME);
    assertTrue("Output file should exist", outputFile.exists());

    String content = Files.readString(Paths.get(OUTPUT_FILE_NAME));

    assertTrue(content.contains("1,John Doe"));
    assertTrue(content.contains("2,Jane Smith"));
  }
}