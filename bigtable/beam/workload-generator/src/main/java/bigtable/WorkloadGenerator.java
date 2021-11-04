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

import com.google.cloud.bigtable.beam.AbstractCloudBigtableTableDoFn;
import com.google.cloud.bigtable.beam.CloudBigtableConfiguration;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.joda.time.Duration;

/*
export TEMPLATE_PATH="gs://billybillybillybucket/bigtable-workload-template.json"

gcloud dataflow flex-template run "generate-bigtable-workload-`date +%Y%m%d-%H%M%S`" \
    --template-file-gcs-location "$TEMPLATE_PATH" \
    --parameters bigtableInstanceId="testing-instance" \
    --parameters bigtableTableId="mobile-time-series" \
    --parameters workloadQPS=1000 \
    --region "$REGION"

 */
public class WorkloadGenerator {

  static final long MINUTES_TO_SECONDS = 60;

  public static void main(String[] args) {
    BigtableWorkloadOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(BigtableWorkloadOptions.class);
    generateWorkload(options);
  }

  static void generateWorkload(BigtableWorkloadOptions options) {
    CloudBigtableTableConfiguration bigtableTableConfig =
        new CloudBigtableTableConfiguration.Builder()
            .withProjectId(options.getProject())
            .withInstanceId(options.getBigtableInstanceId())
            .withTableId(options.getBigtableTableId())
            .build();

    Pipeline p = Pipeline.create(options);

    // Initiates a new pipeline every second
    p.apply(GenerateSequence.from(0)
            .withRate(options.getWorkloadQPS(), new Duration(1000))
        // .withMaxReadTime(new Duration(options.getWorkloadDuration() * MINUTES_TO_SECONDS * 1000))
    )
        .apply(ParDo.of(new ReadFromTableFn(bigtableTableConfig)));

    p.run();
  }

  public static class ReadFromTableFn extends AbstractCloudBigtableTableDoFn<Long, Void> {

    public ReadFromTableFn(CloudBigtableConfiguration config) {
      super(config);
    }

    @ProcessElement
    public void processElement(PipelineOptions po) {
      BigtableWorkloadOptions options = po.as(BigtableWorkloadOptions.class);
      try {
        Scan scan = new Scan();

        Table table = getConnection().getTable(TableName.valueOf(options.getBigtableTableId()));
        table.getScanner(scan);
      } catch (Exception e) {
        System.out.println("Error reading.");
        e.printStackTrace();
      }
    }
  }

  public interface BigtableWorkloadOptions extends DataflowPipelineOptions {

    @Description("The Bigtable instance ID")
    @Default.String("bigtable-instance")
    String getBigtableInstanceId();

    void setBigtableInstanceId(String bigtableInstanceId);

    @Description("The Bigtable table ID in the instance.")
    @Default.String("bigtable-table")
    String getBigtableTableId();

    void setBigtableTableId(String bigtableTableId);
    //
    // @Description("The number of minutes to run the workoad.")
    // @Default.Integer(1)
    // Integer getWorkloadDuration();
    //
    // void setWorkloadDuration(Integer workloadDuration);

    @Description("The number of minutes to run the workoad.")
    @Default.Integer(1000)
    Integer getWorkloadQPS();

    void setWorkloadQPS(Integer workloadQPS);
  }

}