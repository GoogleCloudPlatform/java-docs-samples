/*
 * Copyright 2024 Google LLC
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

// [START bigtable_beam_batch_write_flow_control_imports]
import com.google.bigtable.v2.Mutation;
import com.google.bigtable.v2.Mutation.SetCell;
import com.google.cloud.bigtable.beam.CloudBigtableIO;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import com.google.cloud.bigtable.hbase.BigtableOptionsFactory;
import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.io.gcp.bigtable.BigtableIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
// [END bigtable_beam_batch_write_flow_control_imports]

/*
An example pipeline to demo the batch write flow control feature.
 */
public class BatchWriteFlowControlExample {

  static long numRows;

  static final String COLUMN_FAMILY = "cf";
  static final SecureRandom random = new SecureRandom();

  public static void main(String[] args) {
    BigtablePipelineOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(BigtablePipelineOptions.class);
    run(options);
  }

  static void run(BigtablePipelineOptions options) {
    Preconditions.checkNotNull(options.getProject());
    Preconditions.checkNotNull(options.getBigtableInstanceId());
    Preconditions.checkNotNull(options.getBigtableTableId());

    numRows = options.getBigtableRows();

    System.out.println(
        "Generating "
            + options.getBigtableRows()
            + " rows, each "
            + options.getBigtableColsPerRow()
            + " columns, "
            + options.getBigtableBytesPerCol()
            + " bytes per column, "
            + options.getBigtableColsPerRow() * options.getBigtableBytesPerCol()
            + " bytes per row.");

    String generateLabel =
        String.format("Generate %d rows for table %s", numRows, options.getBigtableTableId());
    String mutationLabel =
        String.format(
            "Create mutations that write %d columns of total %d bytes to each row",
            options.getBigtableColsPerRow(),
            options.getBigtableColsPerRow() * options.getBigtableBytesPerCol());

    Pipeline p = Pipeline.create(options);

    PCollection<Long> numbers = p.apply(generateLabel, GenerateSequence.from(0).to(numRows));

    if (options.getUseCloudBigtableIo()) {
      writeWithCloudBigtableIo(numbers, mutationLabel, options);
    } else {
      writeWithBigtableIo(numbers, mutationLabel, options);
    }

    p.run().waitUntilFinish();
  }

  static void writeWithCloudBigtableIo(
      PCollection<Long> numbers, String label, BigtablePipelineOptions options) {
    System.out.println("Using CloudBigtableIO");
    PCollection<org.apache.hadoop.hbase.client.Mutation> mutations =
        numbers.apply(
            label,
            ParDo.of(
                new CreateHbaseMutationFn(
                    options.getBigtableColsPerRow(), options.getBigtableBytesPerCol())));

    // [START bigtable_beam_batch_write_flow_control_cloudbigtableio]
    mutations.apply(
        String.format("Write data to table %s via CloudBigtableIO", options.getBigtableTableId()),
        CloudBigtableIO.writeToTable(
            new CloudBigtableTableConfiguration.Builder()
                .withProjectId(options.getProject())
                .withInstanceId(options.getBigtableInstanceId())
                .withTableId(options.getBigtableTableId())
                .withConfiguration(
                    BigtableOptionsFactory.BIGTABLE_ENABLE_BULK_MUTATION_FLOW_CONTROL, "true")
                .build()));
    // [END bigtable_beam_batch_write_flow_control_cloudbigtableio]
  }

  static void writeWithBigtableIo(
      PCollection<Long> numbers, String label, BigtablePipelineOptions options) {
    System.out.println("Using BigtableIO");
    PCollection<KV<ByteString, Iterable<Mutation>>> mutations =
        numbers.apply(
            label,
            ParDo.of(
                new CreateMutationFn(
                    options.getBigtableColsPerRow(), options.getBigtableBytesPerCol())));

    // [START bigtable_beam_batch_write_flow_control_bigtableio]
    mutations.apply(
        String.format("Write data to table %s via BigtableIO", options.getBigtableTableId()),
        BigtableIO.write()
            .withProjectId(options.getProject())
            .withInstanceId(options.getBigtableInstanceId())
            .withTableId(options.getBigtableTableId())
            .withFlowControl(true) // This enables batch write flow control
    );
    // [END bigtable_beam_batch_write_flow_control_bigtableio]
  }

  static class CreateMutationFn extends DoFn<Long, KV<ByteString, Iterable<Mutation>>> {

    // The actual row key will be reversed to avoid rolling hotspotting
    static final String rowKeyFormat = "%015d";

    final int colsPerRow;
    final int bytesPerCol;

    public CreateMutationFn(int colsPerRow, int bytesPerCol) {
      this.colsPerRow = colsPerRow;
      this.bytesPerCol = bytesPerCol;
    }

    @ProcessElement
    public void processElement(
        @Element Long number, OutputReceiver<KV<ByteString, Iterable<Mutation>>> out) {
      String rowKey = String.format(rowKeyFormat, number);
      // Reverse the rowkey so that it's evenly writing to different TS and not rolling hotspotting
      rowKey = new StringBuilder(rowKey).reverse().toString();

      // Generate random bytes
      List<Mutation> mutations = new ArrayList<>(colsPerRow);
      for (int c = 0; c < colsPerRow; c++) {
        byte[] randomData = new byte[(int) bytesPerCol];
        random.nextBytes(randomData);

        SetCell setCell =
            SetCell.newBuilder()
                .setFamilyName(COLUMN_FAMILY)
                .setColumnQualifier(ByteString.copyFromUtf8(String.valueOf(c)))
                .setValue(ByteString.copyFrom(randomData))
                .build();
        Mutation mutation = Mutation.newBuilder().setSetCell(setCell).build();
        mutations.add(mutation);
      }

      out.output(KV.of(ByteString.copyFromUtf8(rowKey), mutations));
    }
  }

  static class CreateHbaseMutationFn extends DoFn<Long, org.apache.hadoop.hbase.client.Mutation> {

    // The actual row key will be reversed to avoid rolling hotspotting
    static final String rowKeyFormat = "%015d";

    final int colsPerRow;
    final int bytesPerCol;

    public CreateHbaseMutationFn(int colsPerRow, int bytesPerCol) {
      this.colsPerRow = colsPerRow;
      this.bytesPerCol = bytesPerCol;
    }

    @ProcessElement
    public void processElement(
        @Element Long number, OutputReceiver<org.apache.hadoop.hbase.client.Mutation> out) {

      String rowKey = String.format(rowKeyFormat, number);
      // Reverse the rowkey so that it's evenly writing to different TS and not rolling hotspotting
      rowKey = new StringBuilder(rowKey).reverse().toString();

      Put row = new Put(Bytes.toBytes(rowKey));

      // Generate random bytes
      for (int c = 0; c < colsPerRow; c++) {
        byte[] randomData = new byte[(int) bytesPerCol];
        random.nextBytes(randomData);

        row.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes(String.valueOf(c)), randomData);
      }

      out.output(row);
    }
  }

  public interface BigtablePipelineOptions extends DataflowPipelineOptions {

    @Description("The Bigtable instance ID")
    String getBigtableInstanceId();

    void setBigtableInstanceId(String bigtableInstanceId);

    @Description("The Bigtable table ID")
    String getBigtableTableId();

    void setBigtableTableId(String bigtableTableId);

    @Description("The number of bytes per column")
    @Default.Integer(1024)
    Integer getBigtableBytesPerCol();

    void setBigtableBytesPerCol(Integer bigtableBytesPerCol);

    @Description("The number of columns per row")
    @Default.Integer(1)
    Integer getBigtableColsPerRow();

    void setBigtableColsPerRow(Integer bigtableColsPerRow);

    @Description("The number of rows")
    @Default.Long(15000000)
    Long getBigtableRows();

    void setBigtableRows(Long bigtableRows);

    @Description("Use CloudBigtableIO instead of BigtableIO (default).")
    @Default.Boolean(false)
    Boolean getUseCloudBigtableIo();

    void setUseCloudBigtableIo(Boolean hbase);
  }
}
