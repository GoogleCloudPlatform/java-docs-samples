/*
 * Copyright 2018 Google LLC
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


package com.example.cloud.bigtable.quickstart.it;

import static org.junit.Assert.assertTrue;

import com.example.cloud.bigtable.quickstart.Quickstart;
import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Cloud Bigtable Quickstart integration tests
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class QuickstartIT {

  // provide your project id as an env var
  private final String projectId = System.getProperty("bigtable.test.projectID");
  private final String instanceId = System.getProperty("bigtable.test.instanceID");

  private final String tableId = formatForTest("my-table");
  private final String columnFamilyName = "my-column-family";
  private final String columnName = "my-column";
  private final String data = "my-data";


  private String formatForTest(String name) {
    return name + "-" + UUID.randomUUID().toString().substring(0, 20);
  }

  @Before
  public void prepare() throws Exception {
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      HTableDescriptor descriptor = new HTableDescriptor(TableName.valueOf(tableId));
      descriptor.addFamily(new HColumnDescriptor(columnFamilyName));
      try (Admin admin = connection.getAdmin()) {
        admin.createTable(descriptor);
        // Retrieve the table we just created so we can do some reads and writes
        try (Table table = connection.getTable(TableName.valueOf(tableId))) {

          String rowKey = "r1";

          Put put = new Put(Bytes.toBytes(rowKey));
          put.addColumn(Bytes.toBytes(columnFamilyName), Bytes.toBytes(columnName),
              Bytes.toBytes(data));
          table.put(put);
        }
      }
    }
  }

  @Test
  public void quickStart() throws Exception {
    PrintStream stdOut = System.out;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
    Quickstart.main(projectId, instanceId, tableId);
    System.setOut(stdOut);
    assertTrue(bout.toString().contains(data));
  }

  @After
  public void cleanup() throws Exception {
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      connection.getAdmin().deleteTable(TableName.valueOf(tableId));
    }
  }
}
