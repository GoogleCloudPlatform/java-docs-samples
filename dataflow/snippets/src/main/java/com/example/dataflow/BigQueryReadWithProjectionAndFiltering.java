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

// [START dataflow_bigquery_read_projection_and_filtering]
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.managed.Managed;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.Row;
import org.apache.beam.sdk.values.TypeDescriptors;

public class BigQueryReadWithProjectionAndFiltering {
  public static void main(String[] args) {
    // Parse the pipeline options passed into the application. Example:
    //   --projectId=$PROJECT_ID --datasetName=$DATASET_NAME --tableName=$TABLE_NAME
    // For more information, see https://beam.apache.org/documentation/programming-guide/#configuring-pipeline-options
    PipelineOptionsFactory.register(ExamplePipelineOptions.class);
    ExamplePipelineOptions options = PipelineOptionsFactory.fromArgs(args)
        .withValidation()
        .as(ExamplePipelineOptions.class);

    String tableSpec = String.format("%s:%s.%s",
        options.getProjectId(),
        options.getDatasetName(),
        options.getTableName());

    ImmutableMap<String, Object> config = ImmutableMap.<String, Object>builder()
        .put("table", tableSpec)
        .put("row_restriction", "age > 18")
        .put("fields", List.of("user_name", "age"))
        .build();

    // Create a pipeline and apply transforms.
    Pipeline pipeline = Pipeline.create(options);
    pipeline
        .apply(Managed.read(Managed.BIGQUERY).withConfig(config)).getSinglePCollection()
        .apply(MapElements
            .into(TypeDescriptors.strings())
            // Access individual fields in the row.
            .via((Row row) -> {
              String output = String.format("Name: %s, Age: %s%n",
                  row.getString("user_name"),
                  row.getInt64("age"));
              System.out.println(output);
              return output;
            }));
    pipeline.run().waitUntilFinish();
  }
}
// [END dataflow_bigquery_read_projection_and_filtering]
