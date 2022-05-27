/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * <p>Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.bigtable;

// [START bigtable_hw_imports_hbase]
import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import java.io.IOException;
// [END bigtable_hw_imports_hbase]
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * A minimal application that connects to Cloud Bigtable using the native HBase API and performs
 * some basic operations.
 */
public class HelloWorld {

  // Refer to table metadata names by byte array in the HBase API
  private static final byte[] TABLE_NAME = Bytes.toBytes("Hello-Bigtable");
  private static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes("cf1");
  private static final byte[] COLUMN_NAME = Bytes.toBytes("greeting");

  // Write some friendly greetings to Cloud Bigtable
  private static final String[] GREETINGS = {
    "Hello World!", "Hello Cloud Bigtable!", "Hello HBase!"
  };

  /** Connects to Cloud Bigtable, runs some basic operations and prints the results. */
  protected static void doHelloWorld(String projectId, String instanceId) {

    // [START bigtable_hw_connect_hbase]
    // Create the Bigtable connection, use try-with-resources to make sure it gets closed
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {

      // The admin API lets us create, manage and delete tables
      Admin admin = connection.getAdmin();
      // [END bigtable_hw_connect_hbase]

      try {
        // [START bigtable_hw_create_table_hbase]
        // Create a table with a single column family
        HTableDescriptor descriptor = new HTableDescriptor(TableName.valueOf(TABLE_NAME));
        descriptor.addFamily(new HColumnDescriptor(COLUMN_FAMILY_NAME));

        System.out.println("HelloWorld: Create table " + descriptor.getNameAsString());
        admin.createTable(descriptor);
        // [END bigtable_hw_create_table_hbase]

        // [START bigtable_hw_write_rows_hbase]
        // Retrieve the table we just created so we can do some reads and writes
        Table table = connection.getTable(TableName.valueOf(TABLE_NAME));

        // Write some rows to the table
        System.out.println("HelloWorld: Write some greetings to the table");
        for (int i = 0; i < GREETINGS.length; i++) {
          // Each row has a unique row key.
          //
          // Note: This example uses sequential numeric IDs for simplicity, but
          // this can result in poor performance in a production application.
          // Since rows are stored in sorted order by key, sequential keys can
          // result in poor distribution of operations across nodes.
          //
          // For more information about how to design a Bigtable schema for the
          // best performance, see the documentation:
          //
          //     https://cloud.google.com/bigtable/docs/schema-design
          String rowKey = "greeting" + i;

          // Put a single row into the table. We could also pass a list of Puts to write a batch.
          Put put = new Put(Bytes.toBytes(rowKey));
          put.addColumn(COLUMN_FAMILY_NAME, COLUMN_NAME, Bytes.toBytes(GREETINGS[i]));
          table.put(put);
        }
        // [END bigtable_hw_write_rows_hbase]

        // [START bigtable_hw_get_by_key_hbase]
        // Get the first greeting by row key
        String rowKey = "greeting0";
        Result getResult = table.get(new Get(Bytes.toBytes(rowKey)));
        String greeting = Bytes.toString(getResult.getValue(COLUMN_FAMILY_NAME, COLUMN_NAME));
        System.out.println("Get a single greeting by row key");
        System.out.printf("\t%s = %s\n", rowKey, greeting);
        // [END bigtable_hw_get_by_key_hbase]

        // [START bigtable_hw_scan_all_hbase]
        // Now scan across all rows.
        Scan scan = new Scan();

        System.out.println("HelloWorld: Scan for all greetings:");
        ResultScanner scanner = table.getScanner(scan);
        for (Result row : scanner) {
          byte[] valueBytes = row.getValue(COLUMN_FAMILY_NAME, COLUMN_NAME);
          System.out.println('\t' + Bytes.toString(valueBytes));
        }
        // [END bigtable_hw_scan_all_hbase]

        // [START bigtable_hw_delete_table_hbase]
        // Clean up by disabling and then deleting the table
        System.out.println("HelloWorld: Delete the table");
        admin.disableTable(table.getName());
        admin.deleteTable(table.getName());
        // [END bigtable_hw_delete_table_hbase]
      } catch (IOException e) {
        if (admin.tableExists(TableName.valueOf(TABLE_NAME))) {
          System.out.println("HelloWorld: Cleaning up table");
          admin.disableTable(TableName.valueOf(TABLE_NAME));
          admin.deleteTable(TableName.valueOf(TABLE_NAME));
        }
        throw e;
      }
    } catch (IOException e) {
      System.err.println("Exception while running HelloWorld: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    // Consult system properties to get project/instance
    String projectId = requiredProperty("bigtable.projectID");
    String instanceId = requiredProperty("bigtable.instanceID");

    doHelloWorld(projectId, instanceId);
  }

  private static String requiredProperty(String prop) {
    String value = System.getProperty(prop);
    if (value == null) {
      throw new IllegalArgumentException("Missing required system property: " + prop);
    }
    return value;
  }
}
