/*
 * Copyright 2020 Google LLC
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
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class HelloWorldTest {
  private static final String INSTANCE_ENV = "BIGTABLE_TESTING_INSTANCE";
  private static final String TABLE_ID =
      "mobile-time-series-" + UUID.randomUUID().toString().substring(0, 20);
  private static final String COLUMN_FAMILY_NAME = "stats_summary";

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
  public static void beforeClass() {
    projectId = requireEnv("GOOGLE_CLOUD_PROJECT");
    instanceId = requireEnv(INSTANCE_ENV);
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      Admin admin = connection.getAdmin();
      HTableDescriptor descriptor = new HTableDescriptor(TableName.valueOf(TABLE_ID));
      descriptor.addFamily(new HColumnDescriptor(COLUMN_FAMILY_NAME));
      admin.createTable(descriptor);

      Table table = connection.getTable(TableName.valueOf(Bytes.toBytes(TABLE_ID)));

      String rowKey = "phone#4c410523#20190401";
      Put put = new Put(Bytes.toBytes(rowKey));

      put.addColumn(
          Bytes.toBytes(COLUMN_FAMILY_NAME), Bytes.toBytes("os_name"), Bytes.toBytes("android"));
      table.put(put);

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
  public void testWrite() {
    HelloWorldWrite.main(
        new String[] {
          "--bigtableProjectId=" + projectId,
          "--bigtableInstanceId=" + instanceId,
          "--bigtableTableId=" + TABLE_ID
        });

    long count = 0;
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      Table table = connection.getTable(TableName.valueOf(TABLE_ID));
      Scan scan = new Scan();

      ResultScanner rows = table.getScanner(scan);

      for (Result row : rows) {
        count++;
      }
    } catch (IOException e) {
      System.out.println(
          "Unable to initialize service client, as a network error occurred: \n" + e.toString());
    }
    assertThat(count).isGreaterThan(0);
  }

  @Test
  public void testRead() {
    HelloWorldRead.main(
        new String[] {
          "--bigtableProjectId=" + projectId,
          "--bigtableInstanceId=" + instanceId,
          "--bigtableTableId=" + TABLE_ID
        });

    String output = bout.toString();
    assertThat(output).contains("phone#");
  }
}
