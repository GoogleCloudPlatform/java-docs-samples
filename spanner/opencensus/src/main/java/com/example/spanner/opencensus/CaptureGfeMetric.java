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

package com.example.spanner.opencensus;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.opencensus.common.Scope;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsExporter;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opencensus.stats.View.Name;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This sample demonstrates how to capture GFE latency using OpenCensus.
 */
public class CaptureGfeMetric {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";

    SpannerOptions options = SpannerOptions.newBuilder()
        .setInterceptorProvider(() -> Collections.singletonList(interceptor))
        .build();
    Spanner spanner = options.getService();
    DatabaseClient dbClient = spanner
        .getDatabaseClient(DatabaseId.of(projectId, instanceId, databaseId));
    captureGfeMetric(dbClient);
  }

  // [START spanner_opencensus_capture_gfe_metric]
  private static final String MILLISECOND = "ms";
  private static final TagKey key = TagKey.create("grpc_client_method");

  // GFE t4t7 latency extracted from server-timing header.
  public static final MeasureLong SPANNER_GFE_LATENCY =
      MeasureLong.create(
          "cloud.google.com/java/spanner/gfe_latency",
          "Latency between Google's network receives an RPC and reads back the first byte of the"
              + " response",
          MILLISECOND);

  static final Aggregation AGGREGATION_WITH_MILLIS_HISTOGRAM =
      Distribution.create(BucketBoundaries.create(Arrays.asList(
          0.0, 0.01, 0.05, 0.1, 0.3, 0.6, 0.8, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0, 13.0,
          16.0, 20.0, 25.0, 30.0, 40.0, 50.0, 65.0, 80.0, 100.0, 130.0, 160.0, 200.0, 250.0,
          300.0, 400.0, 500.0, 650.0, 800.0, 1000.0, 2000.0, 5000.0, 10000.0, 20000.0, 50000.0,
          100000.0)));
  static final View GFE_LATENCY_VIEW = View
      .create(Name.create("cloud.google.com/java/spanner/gfe_latency"),
          "Latency between Google's network receives an RPC and reads back the first byte of the"
              + " response",
          SPANNER_GFE_LATENCY,
          AGGREGATION_WITH_MILLIS_HISTOGRAM,
          Collections.singletonList(key));

  static ViewManager manager = Stats.getViewManager();

  private static final Tagger tagger = Tags.getTagger();
  private static final StatsRecorder STATS_RECORDER = Stats.getStatsRecorder();

  static void captureGfeMetric(DatabaseClient dbClient) {
    // Register GFE view.
    manager.registerView(GFE_LATENCY_VIEW);

    // Enable OpenCensus exporters to export metrics to Stackdriver Monitoring.
    // Exporters use Application Default Credentials to authenticate.
    // See https://developers.google.com/identity/protocols/application-default-credentials
    // for more details.
    try {
      StackdriverStatsExporter.createAndRegister();
    } catch (IOException | IllegalStateException e) {
      System.out.println("Error during StackdriverStatsExporter");
    }

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

  private static final HeaderClientInterceptor interceptor = new HeaderClientInterceptor();
  private static final Metadata.Key<String> SERVER_TIMING_HEADER_KEY =
      Metadata.Key.of("server-timing", Metadata.ASCII_STRING_MARSHALLER);
  // Every response from Cloud Spanner, there will be an additional header that contains the total
  // elapsed time on GFE. The format is "server-timing: gfet4t7; dur=[GFE latency in ms]".
  private static final Pattern SERVER_TIMING_HEADER_PATTERN = Pattern.compile(".*dur=(?<dur>\\d+)");

  // ClientInterceptor to intercept the outgoing RPCs in order to retrieve the GFE header.
  private static class HeaderClientInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
        CallOptions callOptions, Channel next) {
      return new SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
          super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {
            @Override
            public void onHeaders(Metadata metadata) {
              processHeader(metadata, method.getFullMethodName());
              super.onHeaders(metadata);
            }
          }, headers);
        }
      };
    }

    // Process header, extract duration value and record it using OpenCensus.
    private static void processHeader(Metadata metadata, String method) {
      if (metadata.get(SERVER_TIMING_HEADER_KEY) != null) {
        String serverTiming = metadata.get(SERVER_TIMING_HEADER_KEY);
        Matcher matcher = SERVER_TIMING_HEADER_PATTERN.matcher(serverTiming);
        if (matcher.find()) {
          long latency = Long.parseLong(matcher.group("dur"));

          TagContext tctx = tagger.emptyBuilder().put(key, TagValue.create(method)).build();
          try (Scope ss = tagger.withTagContext(tctx)) {
            STATS_RECORDER.newMeasureMap()
                .put(SPANNER_GFE_LATENCY, latency)
                .record();
          }
        }
      }
    }
    // [END spanner_opencensus_capture_gfe_metric]
  }
}
