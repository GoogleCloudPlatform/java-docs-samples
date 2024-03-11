/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.spanner;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.ReadContext.QueryAnalyzeMode;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import com.google.protobuf.Value;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

/**
 * This sample demonstrates how to configure OpenTelemetry and inject via Spanner Options.
 */
public class OpenTelemetryUsage {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";

    // [START spanner_opentelemetry_usage]
    // Enable OpenTelemetry metrics and traces before Injecting OpenTelemetry
    SpannerOptions.enableOpenTelemetryMetrics();
    SpannerOptions.enableOpenTelemetryTraces();

    // Create a new meter provider
    SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
        // Use Otlp exporter or any other exporter of your choice.
        .registerMetricReader(
            PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build())
        .build();

    // Create a new tracer provider
    SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
        // Use Otlp exporter or any other exporter of your choice.
        .addSpanProcessor(SimpleSpanProcessor.builder(OtlpGrpcSpanExporter
            .builder().build()).build())
            .build();

    // Configure OpenTelemetry object using Meter Provider and Tracer Provider
    OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
        .setMeterProvider(sdkMeterProvider)
        .setTracerProvider(sdkTracerProvider)
        .build();

    // Inject OpenTelemetry object via Spanner options or register as GlobalOpenTelemetry.
    SpannerOptions options = SpannerOptions.newBuilder()
        .setOpenTelemetry(openTelemetry)
        .build();
    Spanner spanner = options.getService();

    // [END spanner_opentelemetry_usage]
    DatabaseClient dbClient = spanner
        .getDatabaseClient(DatabaseId.of(projectId, instanceId, databaseId));

    captureGfeMetric(dbClient);
    captureQueryStatsMetric(openTelemetry, dbClient);
    sdkMeterProvider.forceFlush();
    sdkTracerProvider.forceFlush();
  }


  // [START spanner_opentelemetry_capture_query_stats_metric]
  static void captureQueryStatsMetric(OpenTelemetry openTelemetry, DatabaseClient dbClient) {
    // Register query stats metric.
    // This should be done once before start recording the data.
    Meter meter = openTelemetry.getMeter("cloud.google.com/java");
    DoubleHistogram queryStatsMetricLatencies =
        meter
            .histogramBuilder("spanner/query_stats_elapsed")
            .setDescription("The execution of the query")
            .setUnit("ms")
            .build();

    // Capture query stats metric data.
    try (ResultSet resultSet = dbClient.singleUse()
        .analyzeQuery(Statement.of("SELECT SingerId, AlbumId, AlbumTitle FROM Albums"),
            QueryAnalyzeMode.PROFILE)) {

      while (resultSet.next()) {
        System.out.printf(
            "%d %d %s", resultSet.getLong(0), resultSet.getLong(1), resultSet.getString(2));
      }

      String value = resultSet.getStats().getQueryStats()
          .getFieldsOrDefault("elapsed_time", Value.newBuilder().setStringValue("0 msecs").build())
          .getStringValue();
      double elapsedTime = value.contains("msecs")
          ? Double.parseDouble(value.replaceAll(" msecs", ""))
          : Double.parseDouble(value.replaceAll(" secs", "")) * 1000;
      queryStatsMetricLatencies.record(elapsedTime);
    }
  }
  // [END spanner_opentelemetry_capture_query_stats_metric]

  // [START spanner_opentelemetry_gfe_metric]
  static void captureGfeMetric(DatabaseClient dbClient) {
    // GFE_latency and other Spanner metrics are automatically collected
    // when OpenTelemetry metrics are enabled.

    try (ResultSet resultSet =
        dbClient
            .singleUse() // Execute a single read or query against Cloud Spanner.
            .executeQuery(Statement.of("SELECT SingerId, AlbumId, AlbumTitle FROM Albums"))) {
      while (resultSet.next()) {
        System.out.printf(
            "%d %d %s", resultSet.getLong(0), resultSet.getLong(1), resultSet.getString(2));
      }
    }
  }
  // [END spanner_opentelemetry_gfe_metric]
}
