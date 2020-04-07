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
// [START bigtable_beam_helloworld_write]
import com.google.cloud.bigtable.beam.CloudBigtableIO;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

public class HelloWorldWrite {
  public static void main(String[] args) {
    // [START bigtable_beam_helloworld_create_pipeline]
    BigtableOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(BigtableOptions.class);
    Pipeline p = Pipeline.create(options);
    // [END bigtable_beam_helloworld_create_pipeline]

    // [START bigtable_beam_helloworld_write_config]
    CloudBigtableTableConfiguration bigtableTableConfig =
        new CloudBigtableTableConfiguration.Builder()
            .withProjectId(options.getBigtableProjectId())
            .withInstanceId(options.getBigtableInstanceId())
            .withTableId(options.getBigtableTableId())
            .build();
    // [END bigtable_beam_helloworld_write_config]

    // [START bigtable_beam_helloworld_write_transforms]
    p.apply(Create.of("phone#4c410523#20190501", "phone#4c410523#20190502"))
        .apply(
            ParDo.of(
                new DoFn<String, Mutation>() {
                  @ProcessElement
                  public void processElement(@Element String rowkey, OutputReceiver<Mutation> out) {
                    long timestamp = System.currentTimeMillis();
                    Put row = new Put(Bytes.toBytes(rowkey));

                    row.addColumn(
                        Bytes.toBytes("stats_summary"),
                        Bytes.toBytes("os_build"),
                        timestamp,
                        Bytes.toBytes("android"));
                    out.output(row);
                  }
                }))
        .apply(CloudBigtableIO.writeToTable(bigtableTableConfig));
    // [END bigtable_beam_helloworld_write_transforms]

    p.run().waitUntilFinish();
  }

  // [START bigtable_beam_helloworld_options]
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
  // [END bigtable_beam_helloworld_options]
}
// [END bigtable_beam_helloworld_write]
