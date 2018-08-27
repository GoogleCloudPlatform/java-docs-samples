/*
 * Copyright 2016 Google Inc.
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

import java.io.IOException;
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
// [START example]

/**
 * A minimal application that connects to Cloud Bigtable using the native HBase API and performs
 * some basic operations.
 */
public class BigtableHelloWorld {

  // Refer to table metadata names by byte array in the HBase API
  private static final byte[] TABLE_NAME = Bytes.toBytes("Hello-Bigtable");
  private static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes("cf1");
  private static final byte[] COLUMN_NAME = Bytes.toBytes("greeting");

  // Write some friendly greetings to Cloud Bigtable
  private static final String[] GREETINGS = {
    "Hello World!", "Hello Cloud Bigtable!", "Hello HBase!"
  };

  /**
   * Create a table -- first time only.
   * @param connection to Bigtable
   * @return the status
   */
  public static String create(Connection connection) {
    try {
      // The admin API lets us create, manage and delete tables
      Admin admin = connection.getAdmin();

      // Create a table with a single column family
      HTableDescriptor descriptor = new HTableDescriptor(TableName.valueOf(TABLE_NAME));
      descriptor.addFamily(new HColumnDescriptor(COLUMN_FAMILY_NAME));

      admin.createTable(descriptor);
      // [END creating_a_table]
    } catch (IOException e) {
      return "Table exists.";
    }
    return "Create table " + Bytes.toString(TABLE_NAME);
  }

  /** Connects to Cloud Bigtable, runs some basic operations and prints the results. */
  public static String doHelloWorld() {

    StringBuilder result = new StringBuilder();

    // Create the Bigtable connection, use try-with-resources to make sure it gets closed
    Connection connection = BigtableHelper.getConnection();
    result.append(create(connection));
    result.append("<br><br>");
    try (Table table = connection.getTable(TableName.valueOf(TABLE_NAME))) {

      // Retrieve the table we just created so we can do some reads and writes

      // Write some rows to the table
      result.append("Write some greetings to the table<br>");
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

      // Get the first greeting by row key
      String rowKey = "greeting0";
      Result getResult = table.get(new Get(Bytes.toBytes(rowKey)));
      String greeting = Bytes.toString(getResult.getValue(COLUMN_FAMILY_NAME, COLUMN_NAME));
      result.append("Get a single greeting by row key<br>");
      // [END getting_a_row]
      result.append("     ");
      result.append(rowKey);
      result.append("= ");
      result.append(greeting);
      result.append("<br>");

      // Now scan across all rows.
      Scan scan = new Scan();

      result.append("Scan for all greetings:");
      ResultScanner scanner = table.getScanner(scan);
      for (Result row : scanner) {
        byte[] valueBytes = row.getValue(COLUMN_FAMILY_NAME, COLUMN_NAME);
        result.append("    ");
        result.append(Bytes.toString(valueBytes));
        result.append("<br>");
      }

    } catch (IOException e) {
      result.append("Exception while running HelloWorld: " + e.getMessage() + "<br>");
      result.append(e.toString());
      return result.toString();
    }

    return result.toString();
  }
}
// [END example]
