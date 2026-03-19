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

// [START dataflow_postgres_write]
import static org.apache.beam.sdk.schemas.Schema.toSchema;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.managed.Managed;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.Row;

public class PostgresWrite {

  private static Schema INPUT_SCHEMA =
      Stream.of(
              Schema.Field.of("id", Schema.FieldType.INT32),
              Schema.Field.of("name", Schema.FieldType.STRING))
          .collect(toSchema());

  private static List<Row> ROWS =
      Arrays.asList(
          Row.withSchema(INPUT_SCHEMA)
              .withFieldValue("id", 1)
              .withFieldValue("name", "John Doe")
              .build(),
          Row.withSchema(INPUT_SCHEMA)
              .withFieldValue("id", 2)
              .withFieldValue("name", "Jane Smith")
              .build());

  public interface Options extends PipelineOptions {
    @Description("The JDBC URL of the PostgreSQL database to write to.")
    String getJdbcUrl();

    void setJdbcUrl(String value);

    @Description("The PostgresSQL table to write to.")
    String getTable();

    void setTable(String value);

    @Description("The username for the PostgreSQL database.")
    String getUsername();

    void setUsername(String value);

    @Description("The password for the PostgreSQL database.")
    String getPassword();

    void setPassword(String value);
  }

  public static PipelineResult.State main(String[] args) {
    // Parse the pipeline options passed into the application. Example:
    //   --runner=DirectRunner --jdbcUrl=$JDBC_URL --table=$TABLE
    //   --username=$USERNAME --password=$PASSWORD
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
        // Create data to write to Postgres.
        .apply(Create.of(ROWS))
        .setRowSchema(INPUT_SCHEMA)
        // Write data to a Postgres database using Managed I/O.
        .apply(Managed.write(Managed.POSTGRES).withConfig(config))
        .getSinglePCollection();
    return pipeline;
  }
}
// [END dataflow_postgres_write]
