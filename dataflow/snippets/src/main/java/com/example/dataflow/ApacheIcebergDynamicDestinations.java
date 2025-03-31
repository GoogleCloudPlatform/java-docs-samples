/*
 * Copyright 2025 Google LLC
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

// [START dataflow_apache_iceberg_dynamic_destinations]
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.managed.Managed;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.JsonToRow;

public class ApacheIcebergDynamicDestinations {

  // The schema for the table rows.
  public static final Schema SCHEMA = new Schema.Builder()
      .addInt64Field("id")
      .addStringField("name")
      .addStringField("airport")
      .build();

  // The data to write to table, formatted as JSON strings.
  static final List<String> TABLE_ROWS = List.of(
      "{\"id\":0, \"name\":\"Alice\", \"airport\": \"ORD\" }",
      "{\"id\":1, \"name\":\"Bob\", \"airport\": \"SYD\" }",
      "{\"id\":2, \"name\":\"Charles\", \"airport\": \"ORD\" }"
  );

  public interface Options extends PipelineOptions {
    @Description("The URI of the Apache Iceberg warehouse location")
    String getWarehouseLocation();

    void setWarehouseLocation(String value);

    @Description("The name of the Apache Iceberg catalog")
    String getCatalogName();

    void setCatalogName(String value);
  }

  // Write JSON data to Apache Iceberg, using dynamic destinations to determine the Iceberg table
  // where Dataflow writes each record. The JSON data contains a field named "airport". The
  // Dataflow pipeline writes to Iceberg tables with the naming pattern "flights-{airport}".
  public static void main(String[] args) {
    // Parse the pipeline options passed into the application. Example:
    //   --runner=DirectRunner --warehouseLocation=$LOCATION --catalogName=$CATALOG \
    // For more information, see https://beam.apache.org/documentation/programming-guide/#configuring-pipeline-options
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    Pipeline pipeline = Pipeline.create(options);

    // Configure the Iceberg source I/O
    Map catalogConfig = ImmutableMap.<String, Object>builder()
        .put("warehouse", options.getWarehouseLocation())
        .put("type", "hadoop")
        .build();

    ImmutableMap<String, Object> config = ImmutableMap.<String, Object>builder()
        .put("catalog_name", options.getCatalogName())
        .put("catalog_properties", catalogConfig)
        // Route the incoming records based on the value of the "airport" field.
        .put("table", "flights-{airport}")
        // Specify which fields to keep from the input data.
        .put("keep", Arrays.asList("name", "id"))
        .build();

    // Build the pipeline.
    pipeline
        // Read in-memory JSON data.
        .apply(Create.of(TABLE_ROWS))
        // Convert the JSON records to Row objects.
        .apply(JsonToRow.withSchema(SCHEMA))
        // Write each Row to Apache Iceberg.
        .apply(Managed.write(Managed.ICEBERG).withConfig(config));

    // Run the pipeline.
    pipeline.run().waitUntilFinish();
  }
}
// [END dataflow_apache_iceberg_dynamic_destinations]
