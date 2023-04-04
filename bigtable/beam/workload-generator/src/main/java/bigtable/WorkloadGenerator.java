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

import com.google.api.services.dataflow.model.Job;
import com.google.cloud.bigtable.beam.AbstractCloudBigtableTableDoFn;
import com.google.cloud.bigtable.beam.CloudBigtableConfiguration;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.beam.runners.dataflow.DataflowClient;
import org.apache.beam.runners.dataflow.DataflowPipelineJob;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.joda.time.Duration;

public class WorkloadGenerator {

  public static void main(String[] args) {
    BigtableWorkloadOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(BigtableWorkloadOptions.class);
    generateWorkload(options);
  }

  static PipelineResult generateWorkload(BigtableWorkloadOptions options) {
    CloudBigtableTableConfiguration bigtableTableConfig =
        new CloudBigtableTableConfiguration.Builder()
            .withProjectId(options.getProject())
            .withInstanceId(options.getBigtableInstanceId())
            .withTableId(options.getBigtableTableId())
            .build();

    Pipeline p = Pipeline.create(options);

    // Initiates a new pipeline every second
    p.apply(GenerateSequence.from(0).withRate(options.getWorkloadRate(), new Duration(1000)))
        .apply(ParDo.of(new ReadFromTableFn(bigtableTableConfig)));
    System.out.println("Beginning to generate read workload.");
    PipelineResult pipelineResult = p.run();


    // Cancel the workload after the scheduled time.
    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
    exec.schedule(() -> {
      try {
        System.out.println("Cancelling job.");
        cancelJob(options, (DataflowPipelineJob) pipelineResult);
      } catch (IOException e) {
        e.printStackTrace();
        System.out.println("Unable to cancel job.");
      }
    }, options.getWorkloadDurationMinutes(), TimeUnit.MINUTES);

    return pipelineResult;
  }

  private static void cancelJob(BigtableWorkloadOptions options, DataflowPipelineJob pipelineResult)
      throws IOException {
    String jobId = pipelineResult.getJobId();
    DataflowClient client = DataflowClient.create(options);
    Job job = client.getJob(jobId);

    job.setRequestedState("JOB_STATE_CANCELLED");
    client.updateJob(jobId, job);
  }

  public static class ReadFromTableFn extends AbstractCloudBigtableTableDoFn<Long, Void> {

    public ReadFromTableFn(CloudBigtableConfiguration config) {
      super(config);
      System.out.println("Connected to table.");
    }

    @ProcessElement
    public void processElement(PipelineOptions po) throws IOException {
      BigtableWorkloadOptions options = po.as(BigtableWorkloadOptions.class);
      Scan scan = new Scan();
      Table table = getConnection().getTable(TableName.valueOf(options.getBigtableTableId()));
      table.getScanner(scan);
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

    @Description("The QPS for the workload to produce.")
    @Default.Integer(1000)
    Integer getWorkloadRate();

    void setWorkloadRate(Integer workloadRate);

    @Description("The duration for the workload to run in minutes.")
    @Default.Integer(10)
    Integer getWorkloadDurationMinutes();

    void setWorkloadDurationMinutes(Integer workloadDurationMinutes);
  }
}