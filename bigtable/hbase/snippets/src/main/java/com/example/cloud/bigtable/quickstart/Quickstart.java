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

// [START bigtable_quickstart_hbase]

package com.example.cloud.bigtable.quickstart;

import com.google.cloud.bigtable.hbase.BigtableConfiguration;

import java.io.IOException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * A quickstart application that shows connecting to a Cloud Bigtable instance
 * using the native HBase API to read a row from a table.
 */
public class Quickstart {

  public static void main(String... args) {

    String projectId = args[0];  // my-gcp-project-id
    String instanceId = args[1]; // my-bigtable-instance-id
    String tableId = args[2];    // my-bigtable-table-id

    // Create a connection to the Cloud Bigtable instance.
    // Use try-with-resources to make sure the connection is closed correctly
    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {

      System.out.println("--- Connection established with Bigtable Instance ---");
      // Create a connection to the table that already exists
      // Use try-with-resources to make sure the connection to the table is closed correctly
      try (Table table = connection.getTable(TableName.valueOf(tableId))) {

        // Read a row
        String rowKey = "r1";
        System.out.printf("--- Reading for row-key: %s for provided table: %s ---\n",
            rowKey, tableId);

        // Retrieve the result
        Result result = table.get(new Get(Bytes.toBytes(rowKey)));

        // Convert row data to string
        String rowValue = Bytes.toString(result.value());

        System.out.printf("Scanned value for Row r1: %s \n", rowValue);

        System.out.println(" --- Finished reading row --- ");

      }  catch (IOException e) {
        // handle exception while connecting to a table
        throw e;
      }
    } catch (IOException e) {
      System.err.println("Exception while running quickstart: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
// [END bigtable_quickstart_hbase]
