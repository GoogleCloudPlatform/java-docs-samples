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

// [START bigtable_writes_batch_hbase]

import com.google.cloud.bigtable.hbase.BigtableConfiguration;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class WriteBatch {

  private static final byte[] COLUMN_FAMILY_NAME = Bytes.toBytes("stats_summary");

  public static void writeBatch(String projectId, String instanceId, String tableId) {
    // String projectId = "my-project-id";
    // String instanceId = "my-instance-id";
    // String tableId = "mobile-time-series";

    try (Connection connection = BigtableConfiguration.connect(projectId, instanceId)) {
      final Table table = connection.getTable(TableName.valueOf(Bytes.toBytes(tableId)));
      long timestamp = System.currentTimeMillis();
      byte[] one = new byte[]{0, 0, 0, 0, 0, 0, 0, 1};

      List<Put> puts = new ArrayList<Put>();
      puts.add(new Put(Bytes.toBytes("tablet#a0b81f74#20190501")));
      puts.add(new Put(Bytes.toBytes("tablet#a0b81f74#20190502")));

      puts.get(0).addColumn(COLUMN_FAMILY_NAME, Bytes.toBytes("connected_wifi"), timestamp, one);
      puts.get(0)
          .addColumn(
              COLUMN_FAMILY_NAME,
              Bytes.toBytes("os_build"),
              timestamp,
              Bytes.toBytes("12155.0.0-rc1"));

      puts.get(1).addColumn(COLUMN_FAMILY_NAME, Bytes.toBytes("connected_wifi"), timestamp, one);
      puts.get(1)
          .addColumn(
              COLUMN_FAMILY_NAME,
              Bytes.toBytes("os_build"),
              timestamp,
              Bytes.toBytes("12145.0.0-rc6"));

      table.put(puts);

      System.out.print("Successfully wrote 2 rows");
    } catch (Exception e) {
      System.out.println("Error during WriteBatch: \n" + e.toString());
    }
  }
}

// [END bigtable_writes_batch_hbase]
