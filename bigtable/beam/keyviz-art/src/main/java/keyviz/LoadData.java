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

package keyviz;

import com.google.cloud.bigtable.beam.CloudBigtableIO;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import java.util.Random;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * A Beam job that loads random data into Cloud Bigtable.
 */
public class LoadData {

  static final long ONE_MB = 1000 * 1000;
  static final long ONE_GB = 1000 * ONE_MB;
  static final String COLUMN_FAMILY = "cf";

  public static void main(String[] args) {

    WriteDataOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(WriteDataOptions.class);
    Pipeline p = Pipeline.create(options);
    CloudBigtableTableConfiguration bigtableTableConfig =
        new CloudBigtableTableConfiguration.Builder()
            .withProjectId(options.getBigtableProjectId())
            .withInstanceId(options.getBigtableInstanceId())
            .withTableId(options.getBigtableTableId())
            .build();

    long rowSize = options.getMegabytesPerRow() * ONE_MB;
    final long max =
        (Math.round((options.getGigabytesWritten() * ONE_GB)) / rowSize);
    // Make each number the same length by padding with 0s
    int maxLength = ("" + max).length();
    String numberFormat = "%0" + maxLength + "d";

    p.apply(GenerateSequence.from(0).to(max))
        .apply(
            ParDo.of(
                new DoFn<Long, Mutation>() {
                  @ProcessElement
                  public void processElement(@Element Long rowkey, OutputReceiver<Mutation> out) {
                    String paddedRowkey = String.format(numberFormat, rowkey);

                    // Reverse the rowkey for more efficient writing
                    String reversedRowkey = new StringBuilder(paddedRowkey).reverse().toString();
                    Put row = new Put(Bytes.toBytes(reversedRowkey));

                    // Generate random bytes
                    byte[] b = new byte[(int) rowSize];
                    new Random().nextBytes(b);

                    long timestamp = System.currentTimeMillis();
                    row.addColumn(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes("C"), timestamp, b);
                    out.output(row);
                  }
                }))
        .apply(CloudBigtableIO.writeToTable(bigtableTableConfig));

    p.run().waitUntilFinish();
  }

  public interface WriteDataOptions extends BigtableOptions {

    @Description("The number of gigabytes to write")
    @Default.Double(40)
    double getGigabytesWritten();

    void setGigabytesWritten(double gigabytesWritten);

    @Description("The number of megabytes per row to write")
    @Default.Long(5)
    long getMegabytesPerRow();

    void setMegabytesPerRow(long megabytesPerRow);
  }

  public interface BigtableOptions extends DataflowPipelineOptions {

    @Description("The Bigtable project ID, this can be different than your Dataflow project")
    @Default.String("bigtable-project")
    String getBigtableProjectId();

    void setBigtableProjectId(String bigtableProjectId);

    @Description("The Bigtable instance ID")
    @Default.String("bigtable-instance")
    String getBigtableInstanceId();

    void setBigtableInstanceId(String bigtableInstanceId);

    @Description("The Bigtable table ID in the instance.")
    @Default.String("bigtable-table")
    String getBigtableTableId();

    void setBigtableTableId(String bigtableTableId);
  }
}