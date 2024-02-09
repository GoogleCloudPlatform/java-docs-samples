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
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
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
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

/**
 * This sample demonstrates how to capture Cloud Spanner's Query Stats latency using OpenTelemetry.
 */
public class CaptureQueryStatsMetric {

  // [START spanner_opentelemetry_capture_query_stats_metric]
  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";

    // Enable OpenTelemetry metrics
    SpannerOptions.enableOpenTelemetryMetrics();

    // Create a new meter provider
    SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
        // Use Otlp exporter or any other exporter of your choice.
        .registerMetricReader(
            PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build())
        .build();

    // Configure OpenTelemetry object using Meter Provider.
    OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
        .setMeterProvider(sdkMeterProvider)
        .build();

    // Inject OpenTelemetry object via Spanner options or register as GlobalOpenTelemetry/
    SpannerOptions options = SpannerOptions.newBuilder()
        .setOpenTelemetry(openTelemetry)
        .build();

    Spanner spanner = options.getService();
    DatabaseClient dbClient = spanner
        .getDatabaseClient(DatabaseId.of(projectId, instanceId, databaseId));

    // Register query stats metric.
    Meter meter = openTelemetry.getMeter("cloud.google.com/java");
    DoubleHistogram gfeLatencies =
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
      gfeLatencies.record(elapsedTime);
    }
  }
  // [END spanner_opentelemetry_capture_query_stats_metric]
}
