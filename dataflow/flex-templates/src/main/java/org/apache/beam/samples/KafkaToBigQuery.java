/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.samples;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Base64;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.FlatMapElements;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Apache Beam pipeline that reads JSON encoded messages from Kafka and
 * writes them to a BigQuery table.
 */
public class KafkaToBigQuery {
  private static final Logger LOG = LoggerFactory.getLogger(KafkaToBigQuery.class);
  private static final TypeDescriptor<KV<String, Exception>> elementExceptionType =
      TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptor.of(Exception.class));

  public interface KafkaToBigQueryOptions extends PipelineOptions, StreamingOptions {
    @Description("BigQuery table spec to write to, in the form 'project:dataset.table'.")
    @Validation.Required
    String getOutputTable();
    void setOutputTable(String value);

    @Description("Apache Kafka bootstrap servers in the form 'hostname:port'.")
    @Default.String("localhost:9092")
    String getBootstrapServer();
    void setBootstrapServer(String value);

    @Description("Apache Kafka topic to read from.")
    @Default.String("messages")
    String getInputTopic();
    void setInputTopic(String value);
  }

  /**
   * Sample custom class to write into BigQuery as a STRUCT. Note that all field
   * names must match as defined in the schema, and data must be encoded correctly
   * as BigQuery types.
   */
  public static class CustomClass {
    public final String name;
    public final int value;
    public final String timestamp; // Timestamps must be encoded as ISO_INSTANT strings in BigQuery.

    CustomClass(String name, int value, Instant timestamp) {
      this.name = name;
      this.value = value;
      this.timestamp = timestamp.toString();
    }
  }

  public static void main(final String[] args) {
    // To learn more about BigQuery schemas:
    // https://cloud.google.com/bigquery/docs/schemas
    var schema = new TableSchema()
        .setFields(Arrays.asList(
            new TableFieldSchema().setName("string").setType("STRING").setMode("REQUIRED"),
            new TableFieldSchema().setName("int64").setType("INT64"), // default mode is "NULLABLE"
            new TableFieldSchema().setName("float64").setType("FLOAT64"),
            new TableFieldSchema().setName("numeric").setType("NUMERIC"),
            new TableFieldSchema().setName("bool").setType("BOOL"),
            new TableFieldSchema().setName("bytes").setType("BYTES"),

            new TableFieldSchema().setName("date").setType("DATE"),
            new TableFieldSchema().setName("datetime").setType("DATETIME"),
            new TableFieldSchema().setName("time").setType("TIME"),
            new TableFieldSchema().setName("timestamp").setType("TIMESTAMP"),

            new TableFieldSchema().setName("geography").setType("GEOGRAPHY"),

            new TableFieldSchema().setName("array").setType("INT64").setMode("REPEATED")
                .setDescription("Setting a field to REPEATED makes it an ARRAY."),

            new TableFieldSchema().setName("struct").setType("STRUCT")
                .setDescription("A STRUCT requires to define its fields.")
                .setFields(Arrays.asList(
                    new TableFieldSchema().setName("name").setType("STRING"),
                    new TableFieldSchema().setName("value").setType("INT64"),
                    new TableFieldSchema().setName("timestamp").setType("TIMESTAMP")
                ))
        ));

    var options = PipelineOptionsFactory.fromArgs(args).withValidation().as(KafkaToBigQueryOptions.class);
    options.setStreaming(true);

    var pipeline = Pipeline.create(options);

    var messages = pipeline
        .apply("Read messages from Kafka",
            KafkaIO.<String, String>read()
                .withBootstrapServers(options.getBootstrapServer())
                .withTopic(options.getInputTopic())
                .withKeyDeserializer(StringDeserializer.class)
                .withValueDeserializer(StringDeserializer.class)
                .withoutMetadata())

        .apply("Get message contents", Values.<String>create())

        .apply("Create BigQuery TableRows", MapElements.into(TypeDescriptor.of(TableRow.class))
            .via((String stringData) -> {
              // String stringData = "UTF-8 strings are supported! 🌱🌳🌍";
              byte[] bytes = stringData.getBytes(StandardCharsets.UTF_8);

              // To learn more about the supported BigQuery data types:
              // https://cloud.google.com/bigquery/docs/reference/standard-sql/data-types
              return new TableRow()
                  .set("string", stringData)
                  .set("int64", 432)
                  .set("float64", 3.141592653589793)
                  .set("numeric", new BigDecimal("1234.56"))
                  .set("bool", true)
                  .set("bytes", Base64.getEncoder().encodeToString(bytes))

                  // To learn more about date formatting:
                  // https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/format/DateTimeFormatter.html
                  .set("date", LocalDate.parse("2020-03-19").toString()) // ISO_LOCAL_DATE
                  .set("datetime", LocalDateTime.parse("2020-03-19T20:41:25.977340").toString()) // ISO_LOCAL_DATE_TIME
                  .set("time", LocalTime.parse("20:41:25.977340").toString()) // ISO_LOCAL_TIME
                  .set("timestamp", Instant.parse("2020-03-20T03:41:42.977340Z").toString()) // ISO_INSTANT

                  // To learn more about the geography Well-Known Text (WKT) format:
                  // https://en.wikipedia.org/wiki/Well-known_text_representation_of_geometry
                  .set("geography", "POINT(30 10)")

                  // An array can be of any type as long as its mode is REPEATED.
                  .set("array", Arrays.asList(1, 2, 3, 4, 5))

                  // Any class can be written as a STRUCT as long as all the fields
                  // are encoded correctly as BigQuery types.
                  .set("struct", new CustomClass("🌱🌳🌍", 432, Instant.now()));
            })
        .exceptionsInto(elementExceptionType)
        .exceptionsVia(ee -> KV.of(ee.element(), ee.exception())));

    // Handle errors.
    messages.failures()
        .apply("Log errors", FlatMapElements.into(TypeDescriptors.voids())
            .via(error -> {
              LOG.error(String.format("Error processing element: %s", error.getKey()), error.getValue());
              return Arrays.asList();
            }));

    // Write valid TableRows to BigQuery.
    messages.output()
        .apply("Write to BigQuery", BigQueryIO.writeTableRows()
            .to(options.getOutputTable())
            .withSchema(schema)
            .withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
            .withWriteDisposition(WriteDisposition.WRITE_APPEND));

    pipeline.run().waitUntilFinish();
  }
}
