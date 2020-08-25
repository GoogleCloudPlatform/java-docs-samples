// Copyright 2019 Google Inc.
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

package org.apache.beam.samples;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.gson.Gson;
import java.util.Arrays;
import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.transforms.WithTimestamps;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Apache Beam pipeline that reads JSON encoded messages from Kafka and
 * writes them to a BigQuery table.
 */
public class KafkaToBigQuery {
  private static final Logger LOG = LoggerFactory.getLogger(KafkaToBigQuery.class);
  private static final Gson GSON = new Gson();

  public interface Options extends StreamingOptions {
    @Description("Apache Kafka topic to read from.")
    @Validation.Required
    String getInputTopic();

    void setInputTopic(String value);

    @Description(
        "BigQuery table to write to, in the form 'project:dataset.table' or 'dataset.table'.")
    @Default.String("beam_samples.streaming_beam_sql")
    String getOutputTable();

    void setOutputTable(String value);

    @Description("Apache Kafka bootstrap servers in the form 'hostname:port'.")
    @Default.String("localhost:9092")
    String getBootstrapServer();

    void setBootstrapServer(String value);
  }

  @DefaultCoder(AvroCoder.class)
  private static class PageRating {
    Instant processingTime;
    @Nullable String url;
    @Nullable String rating;
  }

  public static void main(final String[] args) {
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    options.setStreaming(true);

    var pipeline = Pipeline.create(options);
    pipeline
        .apply("Read messages from Kafka",
            KafkaIO.<String, String>read()
                .withBootstrapServers(options.getBootstrapServer())
                .withTopic(options.getInputTopic())
                .withKeyDeserializer(StringDeserializer.class)
                .withValueDeserializer(StringDeserializer.class)
                .withoutMetadata())
        .apply("Get message contents", Values.<String>create())
        .apply("Log messages", MapElements.into(TypeDescriptor.of(String.class))
            .via(message -> {
              LOG.info("Received: {}", message);
              return message;
            }))
        .apply("Parse JSON", MapElements.into(TypeDescriptor.of(PageRating.class))
            .via(message -> GSON.fromJson(message, PageRating.class)))

        .apply("Add processing time", WithTimestamps
            .of((pageRating) -> new Instant(pageRating.processingTime)))
        .apply("Fixed-size windows", Window.into(FixedWindows.of(Duration.standardMinutes(1))))

        .apply("Convert to BigQuery TableRow", MapElements.into(TypeDescriptor.of(TableRow.class))
            .via(pageRating -> new TableRow()
                .set("processing_time", pageRating.processingTime.toString())
                .set("url", pageRating.url)
                .set("rating", pageRating.rating)))
        .apply("Write to BigQuery", BigQueryIO.writeTableRows()
            .to(options.getOutputTable())
            .withSchema(new TableSchema().setFields(Arrays.asList(
                new TableFieldSchema().setName("processing_time").setType("TIMESTAMP"),
                new TableFieldSchema().setName("url").setType("STRING"),
                new TableFieldSchema().setName("rating").setType("STRING"))))
            .withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
            .withWriteDisposition(WriteDisposition.WRITE_APPEND));

    // For a Dataflow Flex Template, do NOT waitUntilFinish().
    pipeline.run();
  }
}
