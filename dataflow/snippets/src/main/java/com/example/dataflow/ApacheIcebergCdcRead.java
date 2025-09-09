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

// [START dataflow_apache_iceberg_cdc_read]
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.RowCoder;
import org.apache.beam.sdk.extensions.gcp.options.GcpOptions;
import org.apache.beam.sdk.managed.Managed;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.Duration;

/**
 * A streaming pipeline that reads CDC events from an Iceberg table, aggregates user clicks, and
 * writes the results to another Iceberg table. For more information on BigLake, 
 * see the documentation at https://cloud.google.com/bigquery/docs/blms-rest-catalog.
 *
 * <p>This pipeline can be used to process the output of {@link
 * ApacheIcebergRestCatalogStreamingWrite}.
 */
public class ApacheIcebergCdcRead {

  // Schema for the source table containing click events.
  public static final Schema SOURCE_SCHEMA =
      Schema.builder().addStringField("user_id").addInt64Field("click_count").build();

  // Schema for the destination table containing aggregated click counts.
  public static final Schema DESTINATION_SCHEMA =
      Schema.builder().addStringField("user_id").addInt64Field("total_clicks").build();

  /** Pipeline options for this example. */
  public interface Options extends GcpOptions {
    @Description("The source Iceberg table to read CDC events from")
    @Validation.Required
    String getSourceTable();

    void setSourceTable(String sourceTable);

    @Description("The destination Iceberg table to write aggregated results to")
    @Validation.Required
    String getDestinationTable();

    void setDestinationTable(String destinationTable);

    @Description("Warehouse location for the Iceberg catalog")
    @Validation.Required
    String getWarehouse();

    void setWarehouse(String warehouse);

    @Description("The URI for the REST catalog")
    @Default.String("https://biglake.googleapis.com/iceberg/v1beta/restcatalog")
    String getCatalogUri();

    void setCatalogUri(String value);

    @Description("The name of the Iceberg catalog")
    @Validation.Required
    String getCatalogName();

    void setCatalogName(String catalogName);
  }

  public static void main(String[] args) throws IOException {
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);

    // Note: The token expires in 1 hour. Users may need to re-run the pipeline.
    // Future updates to Iceberg and the BigLake Metastore will support token refreshing.
    Map<String, String> catalogProps =
        ImmutableMap.<String, String>builder()
            .put("type", "rest")
            .put("uri", options.getCatalogUri())
            .put("warehouse", options.getWarehouse())
            .put("header.x-goog-user-project", options.getProject())
            .put(
                "header.Authorization",
                "Bearer "
                    + GoogleCredentials.getApplicationDefault()
                        .createScoped("https://www.googleapis.com/auth/cloud-platform")
                        .refreshAccessToken()
                        .getTokenValue())
            .put("rest-metrics-reporting-enabled", "false")
            .build();

    Pipeline p = Pipeline.create(options);

    // Configure the Iceberg CDC read
    Map<String, Object> icebergReadConfig =
        ImmutableMap.<String, Object>builder()
            .put("table", options.getSourceTable())
            .put("catalog_name", options.getCatalogName())
            .put("catalog_properties", catalogProps)
            .put("streaming", Boolean.TRUE)
            .put("poll_interval_seconds", 20)
            .build();

    PCollection<Row> cdcEvents =
        p.apply("ReadFromIceberg", Managed.read(Managed.ICEBERG_CDC).withConfig(icebergReadConfig))
            .getSinglePCollection()
            .setRowSchema(SOURCE_SCHEMA);

    PCollection<Row> aggregatedRows =
        cdcEvents
            .apply("ApplyWindow", Window.into(FixedWindows.of(Duration.standardSeconds(30))))
            .apply(
                "ExtractUserAndCount",
                MapElements.into(
                        TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.longs()))
                    .via(
                        row -> {
                          String userId = row.getString("user_id");
                          Long clickCount = row.getInt64("click_count");
                          return KV.of(userId, clickCount == null ? 0L : clickCount);
                        }))
            .apply("SumClicksPerUser", Sum.longsPerKey())
            .apply(
                "FormatToRow",
                MapElements.into(TypeDescriptors.rows())
                    .via(
                        kv ->
                            Row.withSchema(DESTINATION_SCHEMA)
                                .withFieldValue("user_id", kv.getKey())
                                .withFieldValue("total_clicks", kv.getValue())
                                .build()))
            .setCoder(RowCoder.of(DESTINATION_SCHEMA));

    // Configure the Iceberg write
    Map<String, Object> icebergWriteConfig =
        ImmutableMap.<String, Object>builder()
            .put("table", options.getDestinationTable())
            .put("catalog_properties", catalogProps)
            .put("catalog_name", options.getCatalogName())
            .put("triggering_frequency_seconds", 30)
            .build();

    aggregatedRows.apply(
        "WriteToIceberg", Managed.write(Managed.ICEBERG).withConfig(icebergWriteConfig));

    p.run();
  }
}
// [END dataflow_apache_iceberg_cdc_read]
