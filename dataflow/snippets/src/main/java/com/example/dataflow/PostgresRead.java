/*
 * Copyright 2026 Google LLC
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

// [START dataflow_postgres_read]
import com.google.common.collect.ImmutableMap;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.managed.Managed;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.TypeDescriptors;

public class PostgresRead {

  public interface Options extends PipelineOptions {
    @Description("The JDBC URL of the PostgreSQL database to read from.")
    String getJdbcUrl();

    void setJdbcUrl(String value);

    @Description("The PostgresSQL table to read from.")
    String getTable();

    void setTable(String value);

    @Description("The username for the PostgreSQL database.")
    String getUsername();

    void setUsername(String value);

    @Description("The password for the PostgreSQL database.")
    String getPassword();

    void setPassword(String value);

    @Description("The path to write the output file. Can be a local file path, "
      + "a GCS path, or a path to any other supported file systems.")
    String getOutputPath();

    void setOutputPath(String value);
  }

  public static PipelineResult.State main(String[] args) {
    // Parse the pipeline options passed into the application. Example:
    //   --runner=DirectRunner --jdbcUrl=$JDBC_URL --table=$TABLE
    //   --username=$USERNAME --password=$PASSWORD --outputPath=$OUTPUT_FILE
    // For more information, see
    // https://beam.apache.org/documentation/programming-guide/#configuring-pipeline-options
    var options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    Pipeline pipeline = createPipeline(options);
    return pipeline.run().waitUntilFinish();
  }

  public static Pipeline createPipeline(Options options) {

    // Create configuration parameters for the Managed I/O transform.
    ImmutableMap<String, Object> config =
        ImmutableMap.<String, Object>builder()
            .put("jdbc_url", options.getJdbcUrl())
            .put("location", options.getTable())
            .put("username", options.getUsername())
            .put("password", options.getPassword())
            .build();

    // Build the pipeline.
    var pipeline = Pipeline.create(options);
    pipeline
        // Read data from a Postgres database using Managed I/O.
        .apply(Managed.read(Managed.POSTGRES).withConfig(config))
        .getSinglePCollection()
        // Convert each row to a string.
        .apply(
            MapElements.into(TypeDescriptors.strings())
                .via((row -> String.format("%d,%s", row.getInt32("id"), row.getString("name")))))
        // Write strings to a text file.
        .apply(TextIO.write().to(options.getOutputPath()).withSuffix(".txt").withNumShards(1));
    return pipeline;
  }
}
// [END dataflow_postgres_read]
