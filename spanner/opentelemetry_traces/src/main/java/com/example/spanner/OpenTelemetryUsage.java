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

import com.google.cloud.opentelemetry.trace.TraceConfiguration;
import com.google.cloud.opentelemetry.trace.TraceExporter;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;

/**
 * This sample demonstrates how to configure OpenTelemetry and inject via Spanner Options.
 */
public class OpenTelemetryUsage {

  static SdkTracerProvider sdkTracerProvider;
  static Spanner spanner;

  // TODO(developer): Replace these variables before running the sample.
  static String projectId = "my-project";
  static String instanceId = "my-instance";
  static String databaseId = "my-database";

  // Replace these variables to use OTLP Exporter
  static boolean useCloudTraceExporter = true; // Replace to false for OTLP
  static String otlpEndpoint = "http://localhost:4317"; // Replace with your OTLP endpoint

  public static void main(String[] args) {

    if (useCloudTraceExporter) {
      spanner = getSpannerWithCloudTraceExporter();
    } else {
      spanner = getSpannerWithOtlpExporter();
    }

    DatabaseClient dbClient = spanner
        .getDatabaseClient(DatabaseId.of(projectId, instanceId, databaseId));

    try (ResultSet resultSet =
        dbClient
            .singleUse() // Execute a single read or query against Cloud Spanner.
            .executeQuery(Statement.of("SELECT SingerId, AlbumId, AlbumTitle FROM Albums"))) {
      while (resultSet.next()) {
        System.out.printf(
            "%d %d %s", resultSet.getLong(0), resultSet.getLong(1), resultSet.getString(2));
      }
    }

    sdkTracerProvider.forceFlush();
  }

  public static Spanner getSpannerWithOtlpExporter() {
    // [START spanner_opentelemetry_traces_otlp_usage]
    Resource resource = Resource
        .getDefault().merge(Resource.builder().put("service.name", "My App").build());

    OtlpGrpcSpanExporter otlpGrpcSpanExporter =
        OtlpGrpcSpanExporter
            .builder()
            .setEndpoint(otlpEndpoint) // Replace with your OTLP endpoint
            .build();

    // Using a batch span processor
    // You can use `.setScheduleDelay()`, `.setExporterTimeout()`,
    // `.setMaxQueueSize`(), and `.setMaxExportBatchSize()` to further customize.
    BatchSpanProcessor otlpGrpcSpanProcessor =
        BatchSpanProcessor.builder(otlpGrpcSpanExporter).build();

    // Create a new tracer provider
    sdkTracerProvider = SdkTracerProvider.builder()
        // Use Otlp exporter or any other exporter of your choice.
        .addSpanProcessor(otlpGrpcSpanProcessor)
        .setResource(resource)
        .setSampler(Sampler.traceIdRatioBased(0.1))
        .build();

    // Export to a collector that is expecting OTLP using gRPC.
    OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
        .setTracerProvider(sdkTracerProvider).build();

    // Enable OpenTelemetry traces before Injecting OpenTelemetry
    SpannerOptions.enableOpenTelemetryTraces();

    // Inject OpenTelemetry object via Spanner options or register as GlobalOpenTelemetry.
    SpannerOptions options = SpannerOptions.newBuilder()
        .setOpenTelemetry(openTelemetry)
        .build();
    Spanner spanner = options.getService();
    // [END spanner_opentelemetry_traces_otlp_usage]

    return spanner;
  }

  public static Spanner getSpannerWithCloudTraceExporter() {
    // [START spanner_opentelemetry_traces_cloudtrace_usage]
    Resource resource = Resource
        .getDefault().merge(Resource.builder().put("service.name", "My App").build());

    SpanExporter traceExporter = TraceExporter.createWithConfiguration(
        TraceConfiguration.builder().setProjectId(projectId).build()
    );

    // Using a batch span processor
    // You can use `.setScheduleDelay()`, `.setExporterTimeout()`,
    // `.setMaxQueueSize`(), and `.setMaxExportBatchSize()` to further customize.
    BatchSpanProcessor otlpGrpcSpanProcessor =
        BatchSpanProcessor.builder(traceExporter).build();

    // Create a new tracer provider
    sdkTracerProvider = SdkTracerProvider.builder()
        // Use Otlp exporter or any other exporter of your choice.
        .addSpanProcessor(otlpGrpcSpanProcessor)
        .setResource(resource)
        .setSampler(Sampler.traceIdRatioBased(0.1))
        .build();

    // Export to a collector that is expecting OTLP using gRPC.
    OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
        .setTracerProvider(sdkTracerProvider).build();

    // Enable OpenTelemetry traces before Injecting OpenTelemetry
    SpannerOptions.enableOpenTelemetryTraces();

    // Inject OpenTelemetry object via Spanner options or register it as global object.
    // To register as the global OpenTelemetry object,
    // use "OpenTelemetrySdk.builder()....buildAndRegisterGlobal()".
    SpannerOptions options = SpannerOptions.newBuilder()
        .setOpenTelemetry(openTelemetry)
        .build();
    Spanner spanner = options.getService();
    // [END spanner_opentelemetry_traces_cloudtrace_usage]

    return spanner;
  }
}
