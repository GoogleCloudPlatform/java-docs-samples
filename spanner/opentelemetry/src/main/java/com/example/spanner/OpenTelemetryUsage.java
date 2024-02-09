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
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import io.opentelemetry.api.OpenTelemetry;
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

  // [START spanner_opentelemetry_usage]
  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";

    // Enable OpenTelemetry metrics and traces before Injecting OpenTelemetry
    SpannerOptions.enableOpenTelemetryMetrics();
    SpannerOptions.enableOpenCensusTraces();

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
    DatabaseClient dbClient = spanner
        .getDatabaseClient(DatabaseId.of(projectId, instanceId, databaseId));
  }
  // [END spanner_opentelemetry_usage]
}
