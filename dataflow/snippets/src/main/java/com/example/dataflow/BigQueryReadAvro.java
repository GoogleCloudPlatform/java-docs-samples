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

// [START dataflow_bigquery_read_avro]
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.TypedRead;
import org.apache.beam.sdk.io.gcp.bigquery.SchemaAndRecord;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.TypeDescriptor;

public class BigQueryReadAvro {

  // A custom datatype to hold a record from the source table.
  @DefaultCoder(AvroCoder.class)
  public static class MyData {
    public String name;
    public Long age;

    // Function to convert Avro records to MyData instances.
    public static class FromSchemaAndRecord
            implements SerializableFunction<SchemaAndRecord, MyData> {
      @Override public MyData apply(SchemaAndRecord elem) {
        MyData data = new MyData();
        GenericRecord record = elem.getRecord();
        data.name = ((Utf8) record.get("user_name")).toString();
        data.age = (Long) record.get("age");
        return data;
      }
    }
  }

  public static void main(String[] args) {
    // Parse the pipeline options passed into the application. Example:
    //   --projectId=$PROJECT_ID --datasetName=$DATASET_NAME --tableName=$TABLE_NAME
    // For more information, see https://beam.apache.org/documentation/programming-guide/#configuring-pipeline-options
    PipelineOptionsFactory.register(ExamplePipelineOptions.class);
    ExamplePipelineOptions options = PipelineOptionsFactory.fromArgs(args)
        .withValidation()
        .as(ExamplePipelineOptions.class);

    // Create a pipeline and apply transforms.
    Pipeline pipeline = Pipeline.create(options);
    pipeline
        // Read table data into Avro records, using an application-defined parsing function.
        .apply(BigQueryIO.read(new MyData.FromSchemaAndRecord())
            .from(String.format("%s:%s.%s",
                options.getProjectId(),
                options.getDatasetName(),
                options.getTableName()))
            .withMethod(TypedRead.Method.DIRECT_READ))
        // The output from the previous step is a PCollection<MyData>.
        .apply(MapElements
            .into(TypeDescriptor.of(MyData.class))
            .via((MyData x) -> {
              System.out.printf("Name: %s, Age: %d%n", x.name, x.age);
              return x;
            }));
    pipeline.run().waitUntilFinish();
  }
}
// [END dataflow_bigquery_read_avro]
