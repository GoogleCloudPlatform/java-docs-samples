/*
 * Copyright 2019 Google Inc.
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

import com.google.api.gax.rpc.ServerStream;
import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.BigtableDataSettings;
import com.google.cloud.bigtable.data.v2.models.BulkMutation;
import com.google.cloud.bigtable.data.v2.models.Mutation;
import com.google.cloud.bigtable.data.v2.models.Query;
import com.google.cloud.bigtable.data.v2.models.Row;
import com.google.cloud.bigtable.data.v2.models.RowCell;
import com.google.cloud.bigtable.data.v2.models.RowMutation;
import com.google.protobuf.ByteString;

public class CassandraMigrationCodelab {

  private BigtableDataClient dataClient;
  private String tableId;
  private static final String COLUMN_FAMILY_NAME = "stats_summary";

  public CassandraMigrationCodelab(String projectId, String instanceId, String tableId) {
    this.tableId = tableId;
    BigtableDataSettings settings =
        BigtableDataSettings.newBuilder().setProjectId(projectId).setInstanceId(instanceId).build();

    try {
      dataClient = BigtableDataClient.create(settings);
    } catch (Exception e) {
      System.out.println("Error during data client connection: \n" + e.toString());
    }
  }

  public void run() {
    write();
    writeBatch();
    update();
    update2();
    get();
    scan();
    delete();
    deleteMultiple();
  }

  public void write() {
    try {
      System.currentTimeMillis();
      long timestamp = (long) 1556712000 * 1000; // Timestamp of June 1, 2019 12:00

      String rowKey = "phone#4c410523#20190501";
      ByteString one = ByteString.copyFrom(new byte[]{0, 0, 0, 0, 0, 0, 0, 1});

      RowMutation rowMutation =
          RowMutation.create(tableId, rowKey)
              .setCell(
                  COLUMN_FAMILY_NAME,
                  ByteString.copyFrom("connected_cell".getBytes()),
                  timestamp,
                  one)
              .setCell(COLUMN_FAMILY_NAME, "os_build", timestamp, "PQ2A.190405.003");

      dataClient.mutateRow(rowMutation);
    } catch (Exception e) {
      System.out.println("Error during Write: \n" + e.toString());
    }
  }

  public void writeBatch() {
    try {
      long timestamp = (long) 1556712000 * 1000; // Timestamp of June 1, 2019 12:00

      BulkMutation bulkMutation =
          BulkMutation.create(tableId)
              .add(
                  "tablet#a0b81f74#20190501",
                  Mutation.create()
                      .setCell(COLUMN_FAMILY_NAME, "os_name", timestamp, "chromeos")
                      .setCell(COLUMN_FAMILY_NAME, "os_build", timestamp, "12155.0.0-rc1"))
              .add(
                  "tablet#a0b81f74#20190502",
                  Mutation.create()
                      .setCell(COLUMN_FAMILY_NAME, "os_name", timestamp, "chromeos")
                      .setCell(COLUMN_FAMILY_NAME, "os_build", timestamp, "12155.0.0-rc6"));

      dataClient.bulkMutateRows(bulkMutation);
    } catch (Exception e) {
      System.out.println("Error during WriteBatch: \n" + e.toString());
    }
  }

  public void update() {
    try {
      long timestamp = (long) 1556713800 * 1000; // Timestamp of June 1, 2019 12:30

      String rowKey = "phone#4c410523#20190501";

      RowMutation rowMutation =
          RowMutation.create(tableId, rowKey)
              .setCell(COLUMN_FAMILY_NAME, "os_name", timestamp, "android");

      dataClient.mutateRow(rowMutation);
    } catch (Exception e) {
      System.out.println("Error during update: \n" + e.toString());
    }
  }

  public void update2() {

    try {
      long timestamp = (long) 1556713800 * 1000; // Timestamp of June 1, 2019 12:30

      String rowKey = "phone#4c410523#20190501";

      ByteString zero = ByteString.copyFrom(new byte[]{0, 0, 0, 0, 0, 0, 0, 0});

      RowMutation rowMutation =
          RowMutation.create(tableId, rowKey)
              .setCell(
                  COLUMN_FAMILY_NAME,
                  ByteString.copyFrom("connected_cell".getBytes()),
                  timestamp,
                  zero);

      dataClient.mutateRow(rowMutation);
    } catch (Exception e) {
      System.out.println("Error during update2: \n" + e.toString());
    }
  }

  public void get() {
    try {
      String rowKey = "phone#4c410523#20190501";

      Row row = dataClient.readRow(tableId, rowKey);
      for (RowCell cell : row.getCells()) {

        System.out.printf(
            "Family: %s    Qualifier: %s    Value: %s    Timestamp: %s%n",
            cell.getFamily(),
            cell.getQualifier().toStringUtf8(),
            cell.getValue().toStringUtf8(),
            cell.getTimestamp());
      }
    } catch (Exception e) {
      System.out.println("Error during lookup: \n" + e.toString());
    }
  }

  public void scan() {
    try {
      Query query = Query.create(tableId).range("tablet#a0b81f74#201905", "tablet#a0b81f74#201906");
      ServerStream<Row> rowStream = dataClient.readRows(query);
      for (Row row : rowStream) {
        System.out.println("Row Key: " + row.getKey().toStringUtf8());
        for (RowCell cell : row.getCells()) {

          System.out.printf(
              "Family: %s    Qualifier: %s    Value: %s    Timestamp: %s%n",
              cell.getFamily(),
              cell.getQualifier().toStringUtf8(),
              cell.getValue().toStringUtf8(),
              cell.getTimestamp());
        }
      }
    } catch (Exception e) {
      System.out.println("Error during scan: \n" + e.toString());
    }
  }

  public void delete() {
    try {
      String rowKey = "phone#4c410523#20190501";

      RowMutation mutation = RowMutation.create(tableId, rowKey).deleteRow();

      dataClient.mutateRow(mutation);
    } catch (Exception e) {
      System.out.println("Error during Delete: \n" + e.toString());
    }
  }

  public void deleteMultiple() {
    try {
      Query query = Query.create(tableId).prefix("tablet#a0b81f7");
      ServerStream<Row> rowStream = dataClient.readRows(query);
      BulkMutation bulkMutation = BulkMutation.create(tableId);
      for (Row row : rowStream) {
        bulkMutation.add(row.getKey(), Mutation.create().deleteRow());
      }

      dataClient.bulkMutateRows(bulkMutation);
    } catch (Exception e) {
      System.out.println("Error during DeleteMultiple: \n" + e.toString());
    }
  }
}
