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

// [START dataflow_apache_iceberg_rest_catalog_streaming_write]
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.RowCoder;
import org.apache.beam.sdk.extensions.gcp.options.GcpOptions;
import org.apache.beam.sdk.io.GenerateSequence;
import org.apache.beam.sdk.managed.Managed;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.Row;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.Duration;

/**
 * A streaming pipeline that writes data to an Iceberg table using the REST catalog.
 *
 * <p>This example demonstrates writing to an Iceberg table backed by the BigLake Metastore. For
 * more information, see the documentation at
 * https://cloud.google.com/bigquery/docs/iceberg-biglake-metastore.
 */
public class ApacheIcebergRestCatalogStreamingWrite {

  // The schema for the generated records.
  public static final Schema SCHEMA =
      Schema.builder().addStringField("user_id").addInt64Field("click_count").build();

  /** Pipeline options for this example. */
  public interface Options extends GcpOptions, StreamingOptions {
    @Description(
        "Warehouse location where the table's data will be written to. "
            + "BigLake only supports Single Region buckets")
    @Validation.Required
    String getWarehouse();

    void setWarehouse(String warehouse);

    @Description("The URI for the REST catalog.")
    @Validation.Required
    @Default.String("https://biglake.googleapis.com/iceberg/v1beta/restcatalog")
    String getCatalogUri();

    void setCatalogUri(String value);

    @Description("The name of the table to write to")
    @Validation.Required
    String getIcebergTable();

    void setIcebergTable(String value);

    @Description("The name of the Apache Iceberg catalog")
    @Validation.Required
    String getCatalogName();

    void setCatalogName(String catalogName);
  }

  /**
   * The main entry point for the pipeline.
   *
   * @param args Command-line arguments
   * @throws IOException If there is an issue with Google Credentials
   */
  public static void main(String[] args) throws IOException {
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    options.setStreaming(true);

    // Note: The token expires in 1 hour. Users may need to re-run the pipeline.
    // Future updates to Iceberg and the BigLake Metastore will support token refreshing.
    Map<String, String> catalogProps =
        ImmutableMap.<String, String>builder()
            .put("type", "rest")
            .put("uri", options.getCatalogUri())
            .put("warehouse", options.getWarehouse())
            .put("header.x-goog-user-project", options.getProject())
            .put("oauth2-server-uri", "https://oauth2.googleapis.com/token")
            .put(
                "token",
                GoogleCredentials.getApplicationDefault()
                    .createScoped("https://www.googleapis.com/auth/cloud-platform")
                    .refreshAccessToken()
                    .getTokenValue())
            .put("rest-metrics-reporting-enabled", "false")
            .build();

    Map<String, Object> icebergWriteConfig =
        ImmutableMap.<String, Object>builder()
            .put("table", options.getIcebergTable())
            .put("catalog_properties", catalogProps)
            .put("catalog_name", options.getCatalogName())
            .put("triggering_frequency_seconds", 20)
            .build();

    Pipeline p = Pipeline.create(options);

    p.apply(
            "GenerateSequence",
            GenerateSequence.from(0).withRate(1, Duration.standardSeconds(5)))
        .apply(
            "ConvertToRows",
            MapElements.into(TypeDescriptors.rows())
                .via(
                    i ->
                        Row.withSchema(SCHEMA)
                            .withFieldValue("user_id", "user-" + (i % 10))
                            .withFieldValue("click_count", i % 100)
                            .build()))
        .setCoder(RowCoder.of(SCHEMA))
        .apply("WriteToIceberg", Managed.write(Managed.ICEBERG).withConfig(icebergWriteConfig));

    p.run();
  }
}
// [END dataflow_apache_iceberg_rest_catalog_streaming_write]
