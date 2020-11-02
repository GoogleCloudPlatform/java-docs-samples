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

package example;

import com.google.api.services.bigquery.model.TableRow;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigtable.beam.CloudBigtableIO;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import example.BqCommitData.CommitData;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PDone;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

public class BigqueryImport {

  /*
  Setup with
  cbt createtable github-commits families="cf"
   */

  public static void main(String[] args) {
    BigtableOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(BigtableOptions.class);
    Pipeline p = Pipeline.create(options);
    byte[] COLUMN_FAMILY = Bytes.toBytes("cf");

    CloudBigtableTableConfiguration bigtableTableConfig =
        new CloudBigtableTableConfiguration.Builder()
            .withProjectId(options.getBigtableProjectId())
            .withInstanceId(options.getBigtableInstanceId())
            .withTableId(options.getBigtableTableId())
            .build();

    p.apply(
        "Read from BigQuery query",
        BigQueryIO.readTableRows()
            .fromQuery("SELECT * FROM `bigquery-public-data.github_repos.commits`")
            .usingStandardSql())
        .apply(MapElements.into(TypeDescriptor.of(CommitData.class)).via(CommitData::fromTableRow))
        .apply(
            ParDo.of(
                new DoFn<CommitData, Mutation>() {
                  @ProcessElement
                  public void processElement(@Element CommitData commit, OutputReceiver<Mutation>
                      out) {
                    long timestamp = System.currentTimeMillis();
                    Put row = new Put(Bytes.toBytes(commit.getRowkey()))
                        .addColumn(
                            COLUMN_FAMILY,
                            Bytes.toBytes("authorName"),
                            timestamp,
                            Bytes.toBytes(commit.authorName))
                        .addColumn(
                            COLUMN_FAMILY,
                            Bytes.toBytes("repo"),
                            timestamp,
                            Bytes.toBytes(commit.repo))
                        .addColumn(
                            COLUMN_FAMILY,
                            Bytes.toBytes("commit"),
                            timestamp,
                            Bytes.toBytes(commit.commit))
                        .addColumn(
                            COLUMN_FAMILY,
                            Bytes.toBytes("authorEmail"),
                            timestamp,
                            Bytes.toBytes(commit.authorEmail))
                        .addColumn(
                            COLUMN_FAMILY,
                            Bytes.toBytes("committerName"),
                            timestamp,
                            Bytes.toBytes(commit.committerName))
                        .addColumn(
                            COLUMN_FAMILY,
                            Bytes.toBytes("message"),
                            timestamp,
                            Bytes.toBytes(commit.message))
                        .addColumn(
                            COLUMN_FAMILY,
                            Bytes.toBytes("subject"),
                            timestamp,
                            Bytes.toBytes(commit.subject));

                    if (commit.commitDateSeconds != null) {
                      row.addColumn(
                          COLUMN_FAMILY,
                          Bytes.toBytes("commitDateSeconds"),
                          timestamp,
                          Bytes.toBytes(commit.commitDateSeconds));
                    }

                    out.output(row);
                  }
                }))
        .apply(CloudBigtableIO.writeToTable(bigtableTableConfig));

    //     .apply(ParDo.of(
    //     new DoFn<CommitData, Void>() {
    //       @ProcessElement
    //       public void processElement(@Element CommitData row) {
    //
    //         System.out.println("row.authorName = " + row.authorName);
    //         System.out.println("row.repo = " + row.repo);
    //         System.out.println("row.commit = " + row.commit);
    //         System.out.println("row.authorEmail = " + row.authorEmail);
    //         System.out.println("row.committerName = " + row.committerName);
    //         System.out.println("row.message = " + row.message);
    //         System.out.println("row.subject = " + row.subject);
    //         System.out.println("row.commitDateSeconds = " + row.commitDateSeconds);
    //       }
    //     }
    // ));

    // (FieldValue row) -> {
    //   return  System.out.println("row = " + row);
    // }

    // p.apply(Create.of("phone#4c410523#20190501", "phone#4c410523#20190502"))
    //     .apply(
    //         ParDo.of(
    //             new DoFn<String, Mutation>() {
    //               @ProcessElement
    //               public void processElement(@Element String rowkey, OutputReceiver<Mutation>
    //              out) {
    //                 long timestamp = System.currentTimeMillis();
    //                 Put row = new Put(Bytes.toBytes(rowkey));
    //
    //                 row.addColumn(
    //                     Bytes.toBytes("stats_summary"),
    //                     Bytes.toBytes("os_build"),
    //                     timestamp,
    //                     Bytes.toBytes("android"));
    //                 out.output(row);
    //               }
    //             }))
    //     .apply(CloudBigtableIO.writeToTable(bigtableTableConfig));

    p.run();
    System.exit(0);
    // p.run().waitUntilFinish();
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
