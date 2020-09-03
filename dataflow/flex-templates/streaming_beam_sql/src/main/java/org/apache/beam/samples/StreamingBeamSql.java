// Copyright 2020 Google Inc.
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
import com.google.pubsub.v1.ProjectSubscriptionName;
import java.util.Arrays;
import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.gcp.options.GcpOptions;
import org.apache.beam.sdk.extensions.sql.SqlTransform;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.WithTimestamps;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.Row;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Apache Beam streaming pipeline that reads JSON encoded messages fromPub/Sub,
 * uses Beam SQL to transform the message data, and writes the results to a BigQuery.
 */
public class StreamingBeamSql {
  private static final Logger LOG = LoggerFactory.getLogger(StreamingBeamSql.class);
  private static final Gson GSON = new Gson();

  public interface Options extends StreamingOptions {
    @Description("Pub/Sub subscription to read from.")
    @Validation.Required
    String getInputSubscription();

    void setInputSubscription(String value);

    @Description("BigQuery table to write to, in the form "
        + "'project:dataset.table' or 'dataset.table'.")
    @Default.String("beam_samples.streaming_beam_sql")
    String getOutputTable();

    void setOutputTable(String value);
  }

  @DefaultCoder(AvroCoder.class)
  private static class PageReviewMessage {
    @Nullable
    String url;
    @Nullable
    String review;
  }

  public static void main(final String[] args) {
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    options.setStreaming(true);

    var project = options.as(GcpOptions.class).getProject();
    var subscription = ProjectSubscriptionName
        .of(project, options.getInputSubscription()).toString();

    var schema = Schema.builder()
        .addStringField("url")
        .addDoubleField("page_score")
        .addDateTimeField("processing_time")
        .build();

    var pipeline = Pipeline.create(options);
    pipeline
        // Read, parse, and validate messages from Pub/Sub.
        .apply("Read messages from Pub/Sub", PubsubIO.readStrings().fromSubscription(subscription))
        .apply("Parse JSON into SQL rows", MapElements.into(TypeDescriptor.of(Row.class))
            .via(message -> {
              // This is a good place to add error handling.
              // The first transform should act as a validation layer to make sure
              // that any data coming to the processing pipeline must be valid.
              // See `MapElements.MapWithFailures` for more details.
              LOG.info("message: {}", message);
              var msg = GSON.fromJson(message, PageReviewMessage.class);
              return Row.withSchema(schema).addValues(
                  msg.url,                                    // row url
                  msg.review.equals("positive") ? 1.0 : 0.0,  // row page_score
                  new Instant()                               // row processing_time
              ).build();
            })).setRowSchema(schema) // make sure to set the row schema for the PCollection

        // Add timestamps and bundle elements into windows.
        .apply("Add processing time", WithTimestamps
            .of((row) -> row.getDateTime("processing_time").toInstant()))
        .apply("Fixed-size windows", Window.into(FixedWindows.of(Duration.standardMinutes(1))))

        // Apply a SQL query for every window of elements.
        .apply("Run Beam SQL query", SqlTransform.query(
            "SELECT "
                + "  url, "
                + "  COUNT(page_score) AS num_reviews, "
                + "  AVG(page_score) AS score, "
                + "  MIN(processing_time) AS first_date, "
                + "  MAX(processing_time) AS last_date "
                + "FROM PCOLLECTION "
                + "GROUP BY url"
        ))

        // Convert the SQL Rows into BigQuery TableRows and write them to BigQuery.
        .apply("Convert to BigQuery TableRow", MapElements.into(TypeDescriptor.of(TableRow.class))
            .via(row -> {
              LOG.info("rating summary: {} {} ({} reviews)", row.getDouble("score"),
                  row.getString("url"), row.getInt64("num_reviews"));
              return new TableRow()
                  .set("url", row.getString("url"))
                  .set("num_reviews", row.getInt64("num_reviews"))
                  .set("score", row.getDouble("score"))
                  .set("first_date", row.getDateTime("first_date").toInstant().toString())
                  .set("last_date", row.getDateTime("last_date").toInstant().toString());
            }))
        .apply("Write to BigQuery", BigQueryIO.writeTableRows()
            .to(options.getOutputTable())
            .withSchema(new TableSchema().setFields(Arrays.asList(
                // To learn more about the valid BigQuery types:
                //   https://cloud.google.com/bigquery/docs/reference/standard-sql/data-types
                new TableFieldSchema().setName("url").setType("STRING"),
                new TableFieldSchema().setName("num_reviews").setType("INTEGER"),
                new TableFieldSchema().setName("score").setType("FLOAT64"),
                new TableFieldSchema().setName("first_date").setType("TIMESTAMP"),
                new TableFieldSchema().setName("last_date").setType("TIMESTAMP"))))
            .withCreateDisposition(CreateDisposition.CREATE_IF_NEEDED)
            .withWriteDisposition(WriteDisposition.WRITE_APPEND));

    // For a Dataflow Flex Template, do NOT waitUntilFinish().
    pipeline.run();
  }
}
