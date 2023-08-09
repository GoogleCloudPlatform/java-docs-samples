/*
 * Copyright 2019 Google LLC
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

package com.example.bigtable;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient;
import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class FiltersTest {

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(5);

  private static final String INSTANCE_ENV = "BIGTABLE_TESTING_INSTANCE";
  private static final String TABLE_ID =
      "mobile-time-series-" + UUID.randomUUID().toString().substring(0, 20);
  private static final String COLUMN_FAMILY_NAME_STATS = "stats_summary";
  private static final String COLUMN_FAMILY_NAME_DATA = "cell_plan";
  private static final Instant CURRENT_TIME = Instant.now();
  private static final long TIMESTAMP = CURRENT_TIME.toEpochMilli();
  private static final long TIMESTAMP_MINUS_HR =
      CURRENT_TIME.minus(1, ChronoUnit.HOURS).toEpochMilli();

  private static String projectId;
  private static String instanceId;
  private ByteArrayOutputStream bout;

  private static String requireEnv(String varName) {
    String value = System.getenv(varName);
    assertNotNull(
        String.format("Environment variable '%s' is required to perform these tests.", varName),
        value);
    return value;
  }

  @BeforeClass
  public static void beforeClass() throws IOException {
    projectId = requireEnv("GOOGLE_CLOUD_PROJECT");
    instanceId = requireEnv(INSTANCE_ENV);

    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      try (Admin admin = connection.getAdmin()) {
        admin.createTable(
            new HTableDescriptor(TableName.valueOf(TABLE_ID))
                .addFamily(new HColumnDescriptor(COLUMN_FAMILY_NAME_STATS).setMaxVersions(
                    Integer.MAX_VALUE))
                .addFamily(new HColumnDescriptor(COLUMN_FAMILY_NAME_DATA).setMaxVersions(
                    Integer.MAX_VALUE)));

        try (BufferedMutator batcher = connection.getBufferedMutator(TableName.valueOf(TABLE_ID))) {

          batcher.mutate(
              new Put(Bytes.toBytes("phone#4c410523#20190501"))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_STATS),
                      Bytes.toBytes("connected_cell"),
                      TIMESTAMP,
                      Bytes.toBytes(1L))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_STATS),
                      Bytes.toBytes("connected_wifi"),
                      TIMESTAMP,
                      Bytes.toBytes(1L))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_STATS),
                      Bytes.toBytes("os_build"),
                      TIMESTAMP,
                      Bytes.toBytes("PQ2A.190405.003"))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_DATA),
                      Bytes.toBytes("data_plan_01gb"),
                      TIMESTAMP_MINUS_HR,
                      Bytes.toBytes("true"))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_DATA),
                      Bytes.toBytes("data_plan_01gb"),
                      TIMESTAMP,
                      Bytes.toBytes("false"))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_DATA),
                      Bytes.toBytes("data_plan_05gb"),
                      TIMESTAMP,
                      Bytes.toBytes("true")));

          batcher.mutate(
              new Put(Bytes.toBytes("phone#4c410523#20190502"))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_STATS),
                      Bytes.toBytes("connected_cell"),
                      TIMESTAMP,
                      Bytes.toBytes(1L))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_STATS),
                      Bytes.toBytes("connected_wifi"),
                      TIMESTAMP,
                      Bytes.toBytes(1L))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_STATS),
                      Bytes.toBytes("os_build"),
                      TIMESTAMP,
                      Bytes.toBytes("PQ2A.190405.004"))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_DATA),
                      Bytes.toBytes("data_plan_05gb"),
                      TIMESTAMP,
                      Bytes.toBytes("true")));
          batcher.mutate(
              new Put(Bytes.toBytes("phone#4c410523#20190505"))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_STATS),
                      Bytes.toBytes("connected_cell"),
                      TIMESTAMP,
                      Bytes.toBytes(0L))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_STATS),
                      Bytes.toBytes("connected_wifi"),
                      TIMESTAMP,
                      Bytes.toBytes(1L))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_STATS),
                      Bytes.toBytes("os_build"),
                      TIMESTAMP,
                      Bytes.toBytes("PQ2A.190406.000"))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_DATA),
                      Bytes.toBytes("data_plan_05gb"),
                      TIMESTAMP,
                      Bytes.toBytes("true")));

          batcher.mutate(
              new Put(Bytes.toBytes("phone#5c10102#20190501"))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_STATS),
                      Bytes.toBytes("connected_cell"),
                      TIMESTAMP,
                      Bytes.toBytes(1L))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_STATS),
                      Bytes.toBytes("connected_wifi"),
                      TIMESTAMP,
                      Bytes.toBytes(1L))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_STATS),
                      Bytes.toBytes("os_build"),
                      TIMESTAMP,
                      Bytes.toBytes("PQ2A.190401.002"))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_DATA),
                      Bytes.toBytes("data_plan_10gb"),
                      TIMESTAMP,
                      Bytes.toBytes("true")));

          batcher.mutate(
              new Put(Bytes.toBytes("phone#5c10102#20190502"))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_STATS),
                      Bytes.toBytes("connected_cell"),
                      TIMESTAMP,
                      Bytes.toBytes(1L))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_STATS),
                      Bytes.toBytes("connected_wifi"),
                      TIMESTAMP,
                      Bytes.toBytes(0L))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_STATS),
                      Bytes.toBytes("os_build"),
                      TIMESTAMP,
                      Bytes.toBytes("PQ2A.190406.000"))
                  .addColumn(
                      Bytes.toBytes(COLUMN_FAMILY_NAME_DATA),
                      Bytes.toBytes("data_plan_10gb"),
                      TIMESTAMP,
                      Bytes.toBytes("true")));
        }
      }
    } catch (Exception e) {
      System.out.println("Error during beforeClass: \n" + e);
      throw (e);
    }
  }

  @Before
  public void setupStream() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @AfterClass
  public static void afterClass() throws IOException {
    try (BigtableTableAdminClient adminClient =
        BigtableTableAdminClient.create(projectId, instanceId)) {
      adminClient.deleteTable(TABLE_ID);
    } catch (Exception e) {
      System.out.println("Error during afterClass: \n" + e.toString());
      throw (e);
    }
  }

  @Test
  public void testFilterRowSample() {
    Filters.filterLimitRowSample(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output).contains("Reading data for");
  }

  @Test
  public void testFilterRowRegex() {
    Filters.filterLimitRowRegex(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output)
        .contains(
            String.format(
                "Reading data for phone#4c410523#20190501\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_01gb: false @%1$s\n"
                    + "\tdata_plan_01gb: true @%2$s\n"
                    + "\tdata_plan_05gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190405.003 @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190501\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_10gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190401.002 @%1$s",
                TIMESTAMP, TIMESTAMP_MINUS_HR));
  }

  @Test
  public void testFilterCellsPerCol() {
    Filters.filterLimitCellsPerCol(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output)
        .contains(
            String.format(
                "Reading data for phone#4c410523#20190501\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_01gb: false @%1$s\n"
                    + "\tdata_plan_01gb: true @%2$s\n"
                    + "\tdata_plan_05gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190405.003 @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190502\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_05gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190405.004 @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190505\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_05gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190406.000 @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190501\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_10gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190401.002 @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190502\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_10gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000 @%1$s\n"
                    + "\tos_build: PQ2A.190406.000 @%1$s",
                TIMESTAMP, TIMESTAMP_MINUS_HR));
  }

  @Test
  public void testFilterCellsPerRow() {
    Filters.filterLimitCellsPerRow(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output)
        .contains(
            String.format(
                "Reading data for phone#4c410523#20190501\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_01gb: false @%1$s\n"
                    + "\tdata_plan_05gb: true @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190502\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_05gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190505\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_05gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000 @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190501\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_10gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190502\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_10gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n",
                TIMESTAMP));
  }

  @Test
  public void testFilterLimitCellsPerRowOffset() {
    Filters.filterLimitCellsPerRowOffset(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output)
        .contains(
            String.format(
                "Reading data for phone#4c410523#20190501\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190405.003 @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190502\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190405.004 @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190505\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190406.000 @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190501\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190401.002 @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190502\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000 @%1$s\n"
                    + "\tos_build: PQ2A.190406.000 @%1$s",
                TIMESTAMP));
  }

  @Test
  public void testFilterColFamilyRegex() {
    Filters.filterLimitColFamilyRegex(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output)
        .contains(
            String.format(
                "Reading data for phone#4c410523#20190501\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190405.003 @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190502\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190405.004 @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190505\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190406.000 @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190501\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190401.002 @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190502\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000 @%1$s\n"
                    + "\tos_build: PQ2A.190406.000 @%1$s",
                TIMESTAMP));
  }

  @Test
  public void testFilterColQualifierRegex() {
    Filters.filterLimitColQualifierRegex(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output)
        .contains(
            String.format(
                "Reading data for phone#4c410523#20190501\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190502\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190505\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190501\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190502\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000 @%1$s",
                TIMESTAMP));
  }

  @Test
  public void testFilterColRange() {
    Filters.filterLimitColRange(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output)
        .contains(
            String.format(
                "Reading data for phone#4c410523#20190501\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_01gb: false @%1$s\n"
                    + "\tdata_plan_01gb: true @%2$s\n"
                    + "\tdata_plan_05gb: true @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190502\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_05gb: true @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190505\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_05gb: true @%1$s",
                TIMESTAMP, TIMESTAMP_MINUS_HR));
  }

  @Test
  public void testFilterValueRange() {
    Filters.filterLimitValueRange(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output)
        .contains(
            String.format(
                "Reading data for phone#4c410523#20190501\n"
                    + "Column Family stats_summary\n"
                    + "\tos_build: PQ2A.190405.003 @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190502\n"
                    + "Column Family stats_summary\n"
                    + "\tos_build: PQ2A.190405.004 @%1$s",
                TIMESTAMP));
  }

  @Test
  public void testFilterValueRegex() {
    Filters.filterLimitValueRegex(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output)
        .contains(
            String.format(
                "Reading data for phone#4c410523#20190501\n"
                    + "Column Family stats_summary\n"
                    + "\tos_build: PQ2A.190405.003 @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190502\n"
                    + "Column Family stats_summary\n"
                    + "\tos_build: PQ2A.190405.004 @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190505\n"
                    + "Column Family stats_summary\n"
                    + "\tos_build: PQ2A.190406.000 @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190501\n"
                    + "Column Family stats_summary\n"
                    + "\tos_build: PQ2A.190401.002 @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190502\n"
                    + "Column Family stats_summary\n"
                    + "\tos_build: PQ2A.190406.000 @%1$s",
                TIMESTAMP));
  }

  @Test
  public void testFilterTimestampRange() {
    Filters.filterLimitTimestampRange(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output)
        .contains(
            String.format(
                "Reading data for phone#4c410523#20190501\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_01gb: true @%s\n",
                TIMESTAMP_MINUS_HR));
  }

  @Test
  public void testFilterBlockAll() {
    Filters.filterLimitBlockAll(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output).doesNotContain("Reading data for");
  }

  @Test
  public void testFilterChain() {
    Filters.filterComposingChain(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output)
        .contains(
            String.format(
                "Reading data for phone#4c410523#20190501\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_01gb: false @%1$s\n"
                    + "\tdata_plan_05gb: true @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190502\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_05gb: true @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190505\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_05gb: true @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190501\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_10gb: true @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190502\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_10gb: true @%1$s\n",
                TIMESTAMP));
  }

  @Test
  public void testFilterInterleave() {
    Filters.filterComposingInterleave(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output)
        .contains(
            String.format(
                "Reading data for phone#4c410523#20190501\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_01gb: true @%2$s\n"
                    + "\tdata_plan_05gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tos_build: PQ2A.190405.003 @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190502\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_05gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tos_build: PQ2A.190405.004 @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190505\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_05gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tos_build: PQ2A.190406.000 @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190501\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_10gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tos_build: PQ2A.190401.002 @%1$s\n\n"
                    + "Reading data for phone#5c10102#20190502\n"
                    + "Column Family cell_plan\n"
                    + "\tdata_plan_10gb: true @%1$s\n"
                    + "Column Family stats_summary\n"
                    + "\tos_build: PQ2A.190406.000 @%1$s",
                TIMESTAMP, TIMESTAMP_MINUS_HR));
  }
}