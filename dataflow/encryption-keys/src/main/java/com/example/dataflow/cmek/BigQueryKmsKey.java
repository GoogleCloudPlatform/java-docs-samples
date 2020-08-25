// Copyright 2018 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.dataflow.cmek;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import java.util.Arrays;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;

public class BigQueryKmsKey {
  // Extend PipelineOptions for more command line arguments.
  public interface BigQueryKmsKeyOptions extends PipelineOptions {
    @Description("Cloud Key Management Service key name")
    String getKmsKey();

    void setKmsKey(String value);

    @Description("Output BigQuery table spec in the format 'PROJECT:DATASET.TABLE'")
    String getOutputBigQueryTable();

    void setOutputBigQueryTable(String value);
  }

  public static void main(String[] args) {
    // [START dataflow_cmek]
    // Query from the NASA wildfires public dataset:
    // https://console.cloud.google.com/bigquery?p=bigquery-public-data&d=nasa_wildfire&t=past_week&page=table
    String query =
        "SELECT latitude,longitude,acq_date,acq_time,bright_ti4,confidence "
        + "FROM `bigquery-public-data.nasa_wildfire.past_week` "
        + "LIMIT 10";

    // Schema for the output BigQuery table.
    final TableSchema outputSchema = new TableSchema().setFields(Arrays.asList(
        new TableFieldSchema().setName("latitude").setType("FLOAT"),
        new TableFieldSchema().setName("longitude").setType("FLOAT"),
        new TableFieldSchema().setName("acq_date").setType("DATE"),
        new TableFieldSchema().setName("acq_time").setType("TIME"),
        new TableFieldSchema().setName("bright_ti4").setType("FLOAT"),
        new TableFieldSchema().setName("confidence").setType("STRING")));

    // Create the BigQuery options from the command line arguments.
    BigQueryKmsKeyOptions options = PipelineOptionsFactory.fromArgs(args)
        .withValidation().as(BigQueryKmsKeyOptions.class);

    // String outputBigQueryTable = "<project>:<dataset>.<table>";
    String outputBigQueryTable = options.getOutputBigQueryTable();

    // String kmsKey =
    //    "projects/<project>/locations/<kms-location>/keyRings/<kms-keyring>/cryptoKeys/<kms-key>";
    String kmsKey = options.getKmsKey();

    // Create and run an Apache Beam pipeline.
    Pipeline pipeline = Pipeline.create(options);
    pipeline
        .apply("Read from BigQuery with KMS key",
            BigQueryIO.readTableRows()
                .fromQuery(query)
                .usingStandardSql()
                .withKmsKey(kmsKey))
        .apply("Write to BigQuery with KMS key",
            BigQueryIO.writeTableRows()
                .to(outputBigQueryTable)
                .withSchema(outputSchema)
                .withWriteDisposition(WriteDisposition.WRITE_TRUNCATE)
                .withKmsKey(kmsKey));
    pipeline.run().waitUntilFinish();
    // [END dataflow_cmek]
  }
}
