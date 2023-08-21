/*
 * Copyright 2023 Google LLC
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

package com.example.dataflow;

// [START dataflow_bigquery_stream_exactly_once]
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.testing.TestStream;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.TimestampedValue;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.Duration;
import org.joda.time.Instant;

public class BigQueryStreamExactlyOnce {
  // Create a PTransform that sends simulated streaming data. In a real application, the data
  // source would be an external source, such as Pub/Sub.
  private static TestStream<String> createEventSource() {
    Instant startTime = new Instant(0);
    return TestStream.create(StringUtf8Coder.of())
        .advanceWatermarkTo(startTime)
        .addElements(
            TimestampedValue.of("Alice,20", startTime),
            TimestampedValue.of("Bob,30",
                startTime.plus(Duration.standardSeconds(1))),
            TimestampedValue.of("Charles,40",
                startTime.plus(Duration.standardSeconds(2))),
            TimestampedValue.of("Dylan,Invalid value",
                startTime.plus(Duration.standardSeconds(2))))
        .advanceWatermarkToInfinity();
  }

  public static PipelineResult main(String[] args) {
    // Parse the pipeline options passed into the application. Example:
    //   --projectId=$PROJECT_ID --datasetName=$DATASET_NAME --tableName=$TABLE_NAME
    // For more information, see https://beam.apache.org/documentation/programming-guide/#configuring-pipeline-options
    PipelineOptionsFactory.register(ExamplePipelineOptions.class);
    ExamplePipelineOptions options = PipelineOptionsFactory.fromArgs(args)
        .withValidation()
        .as(ExamplePipelineOptions.class);
    options.setStreaming(true);

    // Create a pipeline and apply transforms.
    Pipeline pipeline = Pipeline.create(options);
    pipeline
        // Add a streaming data source.
        .apply(createEventSource())
        // Map the event data into TableRow objects.
        .apply(MapElements
            .into(TypeDescriptor.of(TableRow.class))
            .via((String x) -> {
              String[] columns = x.split(",");
              return new TableRow().set("user_name", columns[0]).set("age", columns[1]);
            }))
        // Write the rows to BigQuery
        .apply(BigQueryIO.writeTableRows()
            .to(String.format("%s:%s.%s",
                options.getProjectId(),
                options.getDatasetName(),
                options.getTableName()))
            .withCreateDisposition(CreateDisposition.CREATE_NEVER)
            .withWriteDisposition(WriteDisposition.WRITE_APPEND)
            .withMethod(Write.Method.STORAGE_WRITE_API)
            // For exactly-once processing, set the triggering frequency.
            .withTriggeringFrequency(Duration.standardSeconds(5)))
        // Get the collection of write errors.
        .getFailedStorageApiInserts()
        .apply(MapElements.into(TypeDescriptors.strings())
            // Process each error. In production systems, it's useful to write the errors to
            // another destination, such as a dead-letter table or queue.
            .via(
                x -> {
                  System.out.println("Failed insert: " + x.getErrorMessage());
                  System.out.println("Row: " + x.getRow());
                  return "";
                }));
    return pipeline.run();
  }
}
// [END dataflow_bigquery_stream_exactly_once]
