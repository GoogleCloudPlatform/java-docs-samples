/*
 * Copyright 2024 Google LLC
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

// [START dataflow_apache_iceberg_read]
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.managed.Managed;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.PCollectionRowTuple;
import org.apache.beam.sdk.values.TypeDescriptors;

public class ApacheIcebergRead {

  static final String CATALOG_TYPE = "hadoop";

  public interface Options extends PipelineOptions {
    @Description("The URI of the Apache Iceberg warehouse location")
    String getWarehouseLocation();

    void setWarehouseLocation(String value);

    @Description("Path to write the output file")
    String getOutputPath();

    void setOutputPath(String value);

    @Description("The name of the Apache Iceberg catalog")
    String getCatalogName();

    void setCatalogName(String value);

    @Description("The name of the table to write to")
    String getTableName();

    void setTableName(String value);
  }

  public static void main(String[] args) {

    // Parse the pipeline options passed into the application. Example:
    //   --runner=DirectRunner --warehouseLocation=$LOCATION --catalogName=$CATALOG \
    //   --tableName= $TABLE_NAME --outputPath=$OUTPUT_FILE
    // For more information, see https://beam.apache.org/documentation/programming-guide/#configuring-pipeline-options
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    Pipeline pipeline = Pipeline.create(options);

    // Configure the Iceberg source I/O
    Map catalogConfig = ImmutableMap.<String, Object>builder()
        .put("warehouse", options.getWarehouseLocation())
        .put("type", CATALOG_TYPE)
        .build();

    ImmutableMap<String, Object> config = ImmutableMap.<String, Object>builder()
        .put("table", options.getTableName())
        .put("catalog_name", options.getCatalogName())
        .put("catalog_properties", catalogConfig)
        .build();

    // Build the pipeline.
    pipeline.apply(Managed.read(Managed.ICEBERG).withConfig(config))
        .getSinglePCollection()
        // Format each record as a string with the format 'id:name'.
        .apply(MapElements
            .into(TypeDescriptors.strings())
            .via((row -> {
              return String.format("%d:%s",
                  row.getInt64("id"),
                  row.getString("name"));
            })))
        // Write to a text file.
        .apply(
            TextIO.write()
                .to(options.getOutputPath())
                .withNumShards(1)
                .withSuffix(".txt"));

    pipeline.run().waitUntilFinish();
  }
}
// [END dataflow_apache_iceberg_read]
