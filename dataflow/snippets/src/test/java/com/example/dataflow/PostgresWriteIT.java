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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.beam.sdk.PipelineResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresWriteIT {

  private static final String TABLE_NAME = "test_write_table";
  private static final PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15-alpine");

  @Before
  public void setUp() throws Exception {
    postgres.start();

    // Create the table so the Managed I/O can find it.
    try (Connection conn =
        DriverManager.getConnection(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
      Statement stmt = conn.createStatement();
      stmt.execute(
          String.format("CREATE TABLE %s (id INT PRIMARY KEY, name VARCHAR(255))", TABLE_NAME));
    }
  }

  @After
  public void tearDown() {
    if (postgres != null) {
      postgres.stop();
    }
  }

  @Test
  public void testPostgresWrite() throws Exception {
    // Execute the Beam pipeline.
    PipelineResult.State state =
        PostgresWrite.main(
            new String[] {
              "--runner=DirectRunner",
              "--jdbcUrl=" + postgres.getJdbcUrl(),
              "--table=" + TABLE_NAME,
              "--username=" + postgres.getUsername(),
              "--password=" + postgres.getPassword()
            });

    assertEquals(PipelineResult.State.DONE, state);
    verifyDatabaseContent();
  }

  private void verifyDatabaseContent() throws Exception {
    try (Connection conn =
        DriverManager.getConnection(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
      Statement stmt = conn.createStatement();
      ResultSet rs =
          stmt.executeQuery(String.format("SELECT id, name FROM %s ORDER BY id", TABLE_NAME));

      List<String> results = new ArrayList<>();
      while (rs.next()) {
        results.add(rs.getInt("id") + "," + rs.getString("name"));
      }

      assertEquals("Should have 2 rows", 2, results.size());
      assertTrue("Should contain John Doe", results.contains("1,John Doe"));
      assertTrue("Should contain Jane Smith", results.contains("2,Jane Smith"));
    }
  }
}
