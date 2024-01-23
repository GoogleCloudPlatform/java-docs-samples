/*
 * Copyright 2021 Google Inc.
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
public class CaptureSpannerMetric {

  // [START spanner_opentelemetry_usage]
  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "span-cloud-testing";
    String instanceId = "surbhi-testing";
    String databaseId = "test-db";

    // Enable OpenTelemetry metrics and traces before Injecting OpenTelemetry
    SpannerOptions.enableOpenTelemetryMetrics();
    SpannerOptions.enableOpenTelemetryTraces();

    OpenTelemetry openTelemetry = openTelemetry();

    SpannerOptions options = SpannerOptions.newBuilder()
        .setOpenTelemetry(openTelemetry)
        .build();
    Spanner spanner = options.getService();
    DatabaseClient dbClient = spanner
        .getDatabaseClient(DatabaseId.of(projectId, instanceId, databaseId));

    captureGfeMetric(dbClient);
    registerQueryStatsMetric(openTelemetry);
    captureQueryStatsMetric(dbClient);

    // Testing
    burstReadWithQuery(dbClient);
    burstRead(dbClient);
    Thread.sleep(80000);
  }

  static public OpenTelemetry openTelemetry() {

    SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
        // Use Otlp exporter or any other exporter of your choice.
        .registerMetricReader(
            PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build())
        .build();

    SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
        // Use Otlp exporter or any other exporter of your choice.
        .setSampler(Sampler.alwaysOn())
        .addSpanProcessor(SimpleSpanProcessor.builder(OtlpGrpcSpanExporter.builder().build()).build())
        .build();

    return OpenTelemetrySdk.builder()
        .setMeterProvider(sdkMeterProvider)
        .setTracerProvider(sdkTracerProvider)
        .build();
  }

  // [END spanner_opentelemetry_usage]

  // [START spanner_opentelemetry_gfe_metric]
  static void captureGfeMetric(DatabaseClient dbClient) {
    try (ResultSet resultSet =
        dbClient
            .singleUse() // Execute a single read or query against Cloud Spanner.
            .executeQuery(Statement.of("SELECT id, userId, eventType FROM archive_posts"))) {
      while (resultSet.next()) {
        // System.out.printf(
        //     "%s %s %s", resultSet.getString(0), resultSet.getString(1), resultSet.getString(2));
      }
    }
  }
  // [END spanner_opentelemetry_gfe_metric]

  // [START spanner_opentelemetry_capture_query_stats_metric]

  private static final String MILLISECOND = "ms";
  private static final String Instrumentation_Scope = "cloud.google.com/java";
  private static final List<Double> RPC_MILLIS_BUCKET_BOUNDARIES =
      Arrays.asList(
          0.0, 0.01, 0.05, 0.1, 0.3, 0.6, 0.8, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0, 13.0,
          16.0, 20.0, 25.0, 30.0, 40.0, 50.0, 65.0, 80.0, 100.0, 130.0, 160.0, 200.0, 250.0,
          300.0, 400.0, 500.0, 650.0, 800.0, 1000.0, 2000.0, 5000.0, 10000.0, 20000.0, 50000.0,
          100000.0);

  private static DoubleHistogram gfeLatencies;

  // Get the OpenTelemetry object registered in the beginning of the application
  static void registerQueryStatsMetric(OpenTelemetry openTelemetry) {
    Meter meter = openTelemetry.getMeter(Instrumentation_Scope);
    gfeLatencies =
        meter
            .histogramBuilder("spanner/query_stats_elapsed")
            .setDescription("The execution of the query")
            .setUnit(MILLISECOND)
            .setExplicitBucketBoundariesAdvice(RPC_MILLIS_BUCKET_BOUNDARIES)
            .build();
  }

  static void captureQueryStatsMetric(DatabaseClient dbClient) {
    try (ResultSet resultSet = dbClient.singleUse()
        .analyzeQuery(Statement.of("SELECT id, userId, eventType FROM archive_posts"),
            QueryAnalyzeMode.PROFILE)) {

      while (resultSet.next()) {
        // System.out.printf("%s %s %s", resultSet.getString(0), resultSet.getString(1),
        //     resultSet.getString(2));
      }

      String value = resultSet.getStats().getQueryStats()
          .getFieldsOrDefault("elapsed_time", Value.newBuilder().setStringValue("0 msecs").build())
          .getStringValue();

      double elapsedTime = value.contains("msecs") ?
          Double.parseDouble(value.replaceAll(" msecs", "")) :
          Double.parseDouble(value.replaceAll(" secs", "")) * 1000;

      // Record the latency.
      gfeLatencies.record(elapsedTime);
      System.out.println("Completed captureQueryStatsMetric");
    }
  }

  // [END spanner_opentelemetry_capture_query_stats_metric]

  // Extra test code
  private static final int RND_WAIT_TIME_BETWEEN_REQUESTS = 10;
  private static final Random RND = new Random();

  private static final int HOLD_SESSION_TIME = 100;

  public static void burstReadWithQuery(DatabaseClient dbClient) throws Exception {
    {

      try {
        System.out.println("started burstReadWithQuery");
        int totalQueries = 10000;
        int parallelThreads = 200;

        ListeningScheduledExecutorService service =
            MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(parallelThreads));
        List<ListenableFuture<?>> futures = new ArrayList<>(totalQueries);
        for (int i = 0; i < totalQueries; i++) {
          futures.add(
              service.submit(
                  () -> {
                    int random = 0;
                    try {
                      random = RND.nextInt(RND_WAIT_TIME_BETWEEN_REQUESTS);
                      Thread.sleep(random);
                    } catch (InterruptedException e) {
                      System.out.println("Error in burst read" + e.getMessage());
                      // throw new RuntimeException(e);
                    }
                    try (ResultSet rs =
                        dbClient.singleUse().executeQuery(
                            Statement.of("SELECT * FROM archive_posts Limit 5"))) {
                      while (rs.next()) {
                        try {
                          Thread.sleep(RND.nextInt(HOLD_SESSION_TIME));
                        } catch (InterruptedException e) {
                          System.out.println("Error in burst read" + e.getMessage());
                          // throw new RuntimeException(e);
                        }
                      }
                    }
                  }));
        }
        System.out.println("futures added with query");
        Futures.allAsList(futures).get();
        System.out.println("futures completed with new query");
        service.shutdown();
      } catch (Exception ex) {
        System.out.println("Error in burst read with query" + ex.getMessage());
      }
    }

  }
  public static void burstRead(DatabaseClient dbClient) throws Exception {
    {

      try {
        System.out.println("started burstRead");
        int totalQueries = 10000;
        int parallelThreads = 200;

        ListeningScheduledExecutorService service =
            MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(parallelThreads));
        List<ListenableFuture<?>> futures = new ArrayList<>(totalQueries);
        for (int i = 0; i < totalQueries; i++) {
          futures.add(
              service.submit(
                  () -> {
                    int random = 0;
                    try {
                      random = RND.nextInt(RND_WAIT_TIME_BETWEEN_REQUESTS);
                      Thread.sleep(random);
                    } catch (InterruptedException e) {
                      System.out.println("Error in burst read" + e.getMessage());
                     // throw new RuntimeException(e);
                    }
                    try (ResultSet rs =
                        dbClient.singleUse().analyzeQuery(
                            Statement.of("SELECT * FROM archive_posts Limit 5"),
                            QueryAnalyzeMode.PROFILE)) {
                      while (rs.next()) {
                        try {
                          Thread.sleep(RND.nextInt(HOLD_SESSION_TIME));
                        } catch (InterruptedException e) {
                          System.out.println("Error in burst read" + e.getMessage());
                          // throw new RuntimeException(e);
                        }
                      }

                      String value = rs.getStats().getQueryStats()
                          .getFieldsOrDefault("elapsed_time",
                              Value.newBuilder().setStringValue("0 msecs").build())
                          .getStringValue();

                      double elapsedTime = value.contains("msecs") ?
                          Double.parseDouble(value.replaceAll(" msecs", "")) :
                          Double.parseDouble(value.replaceAll(" secs", "")) * 1000;

                      // Record the latency.
                      gfeLatencies.record(elapsedTime + random);
                    }

                    try (ResultSet rs =
                        dbClient.singleUse().analyzeQuery(
                            Statement.of("SELECT 1 AS COL1"),
                            QueryAnalyzeMode.PROFILE)) {
                      while (rs.next()) {
                        try {
                          Thread.sleep(RND.nextInt(HOLD_SESSION_TIME));
                        } catch (InterruptedException e) {
                          System.out.println("Error in burst read" + e.getMessage());
                          // throw new RuntimeException(e);
                        }
                      }

                      String value = rs.getStats().getQueryStats()
                          .getFieldsOrDefault("elapsed_time",
                              Value.newBuilder().setStringValue("0 msecs").build())
                          .getStringValue();

                      double elapsedTime = value.contains("msecs") ?
                          Double.parseDouble(value.replaceAll(" msecs", "")) :
                          Double.parseDouble(value.replaceAll(" secs", "")) * 1000;

                      // Record the latency.
                      gfeLatencies.record(elapsedTime + random);

                    }
                    return null;
                  }));
        }
        System.out.println("futures added");
        Futures.allAsList(futures).get();
        System.out.println("futures completed with new");
        service.shutdown();
      } catch (Exception ex) {
        System.out.println("Error in burst read" + ex.getMessage());
      }
    }

  }
}
