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

import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ReadsTest {

  private static final String INSTANCE_ENV = "BIGTABLE_TESTING_INSTANCE";
  private static final String TABLE_ID =
      "mobile-time-series-" + UUID.randomUUID().toString().substring(0, 20);
  private static final String COLUMN_FAMILY_NAME = "stats_summary";
  private static final long TIMESTAMP = System.currentTimeMillis();

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

    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId);
        Admin admin = connection.getAdmin()) {

      admin.createTable(
          new HTableDescriptor(TableName.valueOf(TABLE_ID))
              .addFamily(new HColumnDescriptor(COLUMN_FAMILY_NAME)));

      try (BufferedMutator batcher = connection.getBufferedMutator(TableName.valueOf(TABLE_ID))) {
        batcher.mutate(
            new Put(Bytes.toBytes("phone#4c410523#20190501"))
                .addColumn(
                    COLUMN_FAMILY_NAME.getBytes(),
                    Bytes.toBytes("connected_cell"),
                    TIMESTAMP,
                    Bytes.toBytes(1L))
                .addColumn(
                    COLUMN_FAMILY_NAME.getBytes(),
                    Bytes.toBytes("connected_wifi"),
                    TIMESTAMP,
                    Bytes.toBytes(1L))
                .addColumn(
                    COLUMN_FAMILY_NAME.getBytes(),
                    Bytes.toBytes("os_build"),
                    TIMESTAMP,
                    Bytes.toBytes("PQ2A.190405.003")));

        batcher.mutate(
            new Put(Bytes.toBytes("phone#4c410523#20190502"))
                .addColumn(
                    COLUMN_FAMILY_NAME.getBytes(),
                    Bytes.toBytes("connected_cell"),
                    TIMESTAMP,
                    Bytes.toBytes(1L))
                .addColumn(
                    COLUMN_FAMILY_NAME.getBytes(),
                    Bytes.toBytes("connected_wifi"),
                    TIMESTAMP,
                    Bytes.toBytes(1L))
                .addColumn(
                    COLUMN_FAMILY_NAME.getBytes(),
                    Bytes.toBytes("os_build"),
                    TIMESTAMP,
                    Bytes.toBytes("PQ2A.190405.004")));

        batcher.mutate(
            new Put(Bytes.toBytes("phone#4c410523#20190505"))
                .addColumn(
                    COLUMN_FAMILY_NAME.getBytes(),
                    Bytes.toBytes("connected_cell"),
                    TIMESTAMP,
                    Bytes.toBytes(0L))
                .addColumn(
                    COLUMN_FAMILY_NAME.getBytes(),
                    Bytes.toBytes("connected_wifi"),
                    TIMESTAMP,
                    Bytes.toBytes(1L))
                .addColumn(
                    COLUMN_FAMILY_NAME.getBytes(),
                    Bytes.toBytes("os_build"),
                    TIMESTAMP,
                    Bytes.toBytes("PQ2A.190406.000")));

        batcher.mutate(
            new Put(Bytes.toBytes("phone#5c10102#20190501"))
                .addColumn(
                    COLUMN_FAMILY_NAME.getBytes(),
                    Bytes.toBytes("connected_cell"),
                    TIMESTAMP,
                    Bytes.toBytes(1L))
                .addColumn(
                    COLUMN_FAMILY_NAME.getBytes(),
                    Bytes.toBytes("connected_wifi"),
                    TIMESTAMP,
                    Bytes.toBytes(1L))
                .addColumn(
                    COLUMN_FAMILY_NAME.getBytes(),
                    Bytes.toBytes("os_build"),
                    TIMESTAMP,
                    Bytes.toBytes("PQ2A.190401.002")));

        batcher.mutate(
            new Put(Bytes.toBytes("phone#5c10102#20190502"))
                .addColumn(
                    COLUMN_FAMILY_NAME.getBytes(),
                    Bytes.toBytes("connected_cell"),
                    TIMESTAMP,
                    Bytes.toBytes(1L))
                .addColumn(
                    COLUMN_FAMILY_NAME.getBytes(),
                    Bytes.toBytes("connected_wifi"),
                    TIMESTAMP,
                    Bytes.toBytes(0L))
                .addColumn(
                    COLUMN_FAMILY_NAME.getBytes(),
                    Bytes.toBytes("os_build"),
                    TIMESTAMP,
                    Bytes.toBytes("PQ2A.190406.000")));
      }
    } catch (Exception e) {
      System.out.println("Error during beforeClass: \n" + e);
      throw e;
    }
  }

  @Before
  public void setupStream() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @AfterClass
  public static void afterClass() throws IOException {
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId);
        Admin admin = connection.getAdmin()) {
      admin.deleteTable(TableName.valueOf(TABLE_ID));
    } catch (Exception e) {
      System.out.println("Error during afterClass: \n" + e);
      throw (e);
    }
  }

  @Test
  public void testReadRow() {
    Reads.readRow(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output)
        .contains(
            String.format(
                "Reading data for phone#4c410523#20190501\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190405.003 @%1$s",
                TIMESTAMP));
  }

  @Test
  public void testReadRowPartial() {
    Reads.readRowPartial(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output)
        .contains(
            String.format(
                "Reading data for phone#4c410523#20190501\n"
                    + "Column Family stats_summary\n"
                    + "\tos_build: PQ2A.190405.003 @%1$s",
                TIMESTAMP));
  }

  @Test
  public void testReadRows() {
    Reads.readRows(projectId, instanceId, TABLE_ID);

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
                    + "\tos_build: PQ2A.190405.004 @%1$s",
                TIMESTAMP));
  }

  @Test
  public void testReadRowRange() {
    Reads.readRowRange(projectId, instanceId, TABLE_ID);

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
                    + "\tos_build: PQ2A.190406.000 @%1$s",
                TIMESTAMP));
  }

  @Test
  public void testReadRowRanges() {
    Reads.readRowRanges(projectId, instanceId, TABLE_ID);

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
  public void testReadPrefix() {
    Reads.readPrefix(projectId, instanceId, TABLE_ID);

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
  public void testReadRowsReversed() {
    Reads.readRowsReversed(projectId, instanceId, TABLE_ID);
    String output = bout.toString();

    assertThat(output)
        .contains(
            String.format(
                    "Reading data for phone#4c410523#20190505\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190406.000 @%1$s\n\n"
                    + "Reading data for phone#4c410523#20190502\n"
                    + "Column Family stats_summary\n"
                    + "\tconnected_cell: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tconnected_wifi: \u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001 @%1$s\n"
                    + "\tos_build: PQ2A.190405.004 @%1$s\n\n",
                TIMESTAMP));
  }

  @Test
  public void testReadFilter() {
    Reads.readFilter(projectId, instanceId, TABLE_ID);

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
}
