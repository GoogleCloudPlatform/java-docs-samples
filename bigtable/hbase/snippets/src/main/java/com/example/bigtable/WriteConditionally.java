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

// [START bigtable_writes_conditional_hbase]

import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.RowMutations;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.util.Bytes;

public class WriteConditionally {

  private static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes("stats_summary");

  public static void writeConditionally(String projectId, String instanceId, String tableId) {
    // String projectId = "my-project-id";
    // String instanceId = "my-instance-id";
    // String tableId = "mobile-time-series";

    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      Table table = connection.getTable(TableName.valueOf(Bytes.toBytes(tableId)));
      long timestamp = System.currentTimeMillis();

      String rowKey = "phone#4c410523#20190501";
      RowMutations mutations = new RowMutations(Bytes.toBytes(rowKey));

      Put put = new Put(Bytes.toBytes(rowKey));
      put.addColumn(
          COLUMN_FAMILY_NAME, Bytes.toBytes("os_name"), timestamp, Bytes.toBytes("android"));
      mutations.add(put);

      table.checkAndMutate(
          Bytes.toBytes(rowKey),
          COLUMN_FAMILY_NAME,
          Bytes.toBytes("os_build"),
          CompareOp.GREATER_OR_EQUAL,
          Bytes.toBytes("PQ2A.190405"),
          mutations);

      System.out.print("Successfully updated row's os_name");

    } catch (Exception e) {
      System.out.println("Error during WriteConditionally: \n" + e.toString());
      e.printStackTrace();
    }
  }
}

// [END bigtable_writes_conditional_hbase]
