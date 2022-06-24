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
import java.io.PrintStream;
import java.util.UUID;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WritesTest {

  private static final String INSTANCE_ENV = "BIGTABLE_TESTING_INSTANCE";
  private static final String TABLE_ID =
      "mobile-time-series-" + UUID.randomUUID().toString().substring(0, 20);
  private static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes("stats_summary");
  private static String projectId;
  private static String instanceId;
  private ByteArrayOutputStream bout;

  private static String requireEnv(String varName) {
    assertNotNull(
        System.getenv(varName),
        "Environment variable '%s' is required to perform these tests.".format(varName));
    return System.getenv(varName);
  }

  @BeforeClass
  public static void beforeClass() {
    projectId = requireEnv("GOOGLE_CLOUD_PROJECT");
    instanceId = requireEnv(INSTANCE_ENV);
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      Admin admin = connection.getAdmin();
      HTableDescriptor descriptor = new HTableDescriptor(TableName.valueOf(TABLE_ID));
      descriptor.addFamily(new HColumnDescriptor(COLUMN_FAMILY_NAME));
      admin.createTable(descriptor);
    } catch (Exception e) {
      System.out.println("Error during beforeClass: \n" + e.toString());
    }
  }

  @Before
  public void setupStream() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @AfterClass
  public static void afterClass() {
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      Admin admin = connection.getAdmin();
      Table table = connection.getTable(TableName.valueOf(Bytes.toBytes(TABLE_ID)));
      admin.disableTable(table.getName());
      admin.deleteTable(table.getName());
    } catch (Exception e) {
      System.out.println("Error during afterClass: \n" + e.toString());
    }
  }

  @Test
  public void test1_WriteSimple() {
    WriteSimple.writeSimple(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output).contains("Successfully wrote row");
  }

  @Test
  public void test2_WriteBatch() {
    WriteBatch.writeBatch(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output).contains("Successfully wrote 2 rows");
  }

  @Test
  public void test3_WriteConditionally() {
    WriteConditionally.writeConditionally(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output).contains("Successfully updated row's os_name");
  }

  @Test
  public void test4_WriteIncrement() {
    WriteIncrement.writeIncrement(projectId, instanceId, TABLE_ID);

    String output = bout.toString();
    assertThat(output).contains("Successfully updated row");
  }
}
