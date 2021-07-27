/*
 * Copyright 2021 Google LLC
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

package bigtable;

import com.google.bigtable.admin.v2.Cluster;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminSettings;
import com.google.cloud.bigtable.admin.v2.models.CreateTableRequest;
import com.google.cloud.bigtable.beam.CloudBigtableIO;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import com.google.cloud.bigtable.grpc.BigtableClusterName;
import com.google.cloud.bigtable.grpc.BigtableClusterUtilities;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.joda.time.Duration;

public class BulkWrite {

  static final double TB_PER_TABLE = .5;
  static final long MB_PER_ROW = 1;
  static final long ONE_MB = 1000 * 1000;
  static final long ONE_GB = 1000 * ONE_MB;
  static final long ONE_TB = 1000 * ONE_GB;
  static final long MB_PER_SEC = 120;

  static final String COLUMN_FAMILY = "cf";
  public static final String TABLE_PREFIX = "data-";
  static final SecureRandom random = new SecureRandom();

  public static void main(String[] args) {
    BigtableOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(BigtableOptions.class);
    Pipeline p = Pipeline.create(options);

    BigtableTableAdminClient adminClient = null;
    try {
      BigtableTableAdminSettings adminSettings = BigtableTableAdminSettings.newBuilder()
          .setProjectId(options.getProject())
          .setInstanceId(options.getBigtableInstanceId())
          .build();

      // Creates a Bigtable table admin client.
      adminClient = BigtableTableAdminClient.create(adminSettings);
    } catch (IOException e) {
      System.out.println("Unable to connect to Bigtable instance.");
      return;
    }

    int clusterNodeCount = getClusterNodeCount(options.getProject(),
        options.getBigtableInstanceId());
    List<String> newTableIds = getNewTableIds(adminClient, options.getBigtableSize());

    if (newTableIds.isEmpty()) {
      return;
    }

    long numRows = (long) ((TB_PER_TABLE * ONE_TB) / (MB_PER_ROW * ONE_MB));
    long rate = clusterNodeCount * MB_PER_SEC / newTableIds.size();

    String generateLabel = String
        .format("Generate %d rows at %dMB per second for %d tables", numRows, rate,
            newTableIds.size());
    String mutationLabel = String
        .format("Create mutations that write %d MB to each row", MB_PER_ROW);

    System.out.println(generateLabel);
    System.out.println(mutationLabel);

    PCollection<Mutation> mutations = p
        .apply(generateLabel, GenerateSequence.from(0).to(numRows)
            .withRate(rate, Duration.standardSeconds(1)))
        .apply(mutationLabel,
            ParDo.of(new CreateMutationFn()));

    for (String tableId : newTableIds) {
      mutations.apply(String.format("Write data to table %s", tableId),
          CloudBigtableIO.writeToTable(new CloudBigtableTableConfiguration.Builder()
              .withProjectId(options.getProject())
              .withInstanceId(options.getBigtableInstanceId())
              .withTableId(tableId)
              .build()));
    }

    p.run();
  }

  private static List<String> getNewTableIds(BigtableTableAdminClient adminClient,
      double expectedSize) {
    List<String> tableIds = adminClient.listTables();
    List<String> newTableIds = new ArrayList<>();
    double currentSize = tableIds.size() * TB_PER_TABLE;

    if (currentSize >= expectedSize) {
      int numTablesToDelete = (int) ((currentSize - expectedSize) / .5);
      for (int i = 0; i < numTablesToDelete; i++) {
        adminClient.deleteTable(tableIds.get(i));
      }
      System.out.printf("Deleted %d tables%n", numTablesToDelete);
    } else {

      int numTablesToCreate = (int) ((expectedSize - currentSize) / .5);
      System.out.printf("Creating %d tables%n", numTablesToCreate);

      for (int i = 0; i < numTablesToCreate; i++) {
        String tableId = TABLE_PREFIX + UUID.randomUUID().toString().substring(0, 20);
        CreateTableRequest createTableRequest = CreateTableRequest.of(tableId)
            .addFamily(COLUMN_FAMILY);
        adminClient.createTable(createTableRequest);
        newTableIds.add(tableId);

        System.out.println(tableId);
      }
    }

    return newTableIds;
  }

  private static int getClusterNodeCount(String projectId, String instanceId) {
    try {
      BigtableClusterUtilities clusterUtility = BigtableClusterUtilities
          .forInstance(projectId, instanceId);
      Cluster cluster = clusterUtility.getSingleCluster();
      String clusterId = new BigtableClusterName(cluster.getName()).getClusterId();
      String zoneId = BigtableClusterUtilities.getZoneId(cluster);
      int clusterNodeCount = clusterUtility.getClusterNodeCount(clusterId, zoneId);
      System.out.println("Cluster size " + clusterNodeCount);
      return clusterNodeCount;
    } catch (IOException | GeneralSecurityException e) {
      System.out.println("Unable to get cluster size. Treating as single-node cluster.");
      return 1;
    }
  }

  static class CreateMutationFn extends DoFn<Long, Mutation> {

    @ProcessElement
    public void processElement(@Element Long rowkey, OutputReceiver<Mutation> out) {
      long timestamp = System.currentTimeMillis();

      // Pad and reverse the rowkey for more distributed writes
      String numberFormat = "%0" + 30 + "d";
      String paddedRowkey = String.format(numberFormat, rowkey);
      String reversedRowkey = new StringBuilder(paddedRowkey).reverse().toString();
      Put row = new Put(Bytes.toBytes(reversedRowkey));

      // Generate random bytes
      long rowSize = MB_PER_ROW * ONE_MB;
      byte[] randomData = new byte[(int) rowSize];

      random.nextBytes(randomData);
      row.addColumn(
          Bytes.toBytes(COLUMN_FAMILY),
          Bytes.toBytes("C"),
          timestamp,
          randomData);
      out.output(row);
    }
  }

  public interface BigtableOptions extends DataflowPipelineOptions {

    @Description("The Bigtable instance ID")
    @Default.String("bigtable-instance")
    String getBigtableInstanceId();

    void setBigtableInstanceId(String bigtableInstanceId);

    @Description("The number of terabytes to set your Bigtable instance to have.")
    @Default.Double(1.5)
    Double getBigtableSize();

    void setBigtableSize(Double bigtableSize);
  }
}
