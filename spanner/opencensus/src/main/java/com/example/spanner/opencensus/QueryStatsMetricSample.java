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

// [START spanner_opencensus_query_stats_metric]

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.ReadContext.QueryAnalyzeMode;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import com.google.protobuf.Value;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsExporter;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opencensus.stats.View.Name;
import io.opencensus.stats.ViewManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This sample demonstrates how to record and export Cloud Spanner's Query Stats latency using
 * OpenCensus.
 */
public class QueryStatsMetricSample {
  private static final String MILLISECOND = "ms";
  static final List<Double> RPC_MILLIS_BUCKET_BOUNDARIES =
      Collections.unmodifiableList(
          Arrays.asList(
              0.0, 0.01, 0.05, 0.1, 0.3, 0.6, 0.8, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 8.0, 10.0, 13.0,
              16.0, 20.0, 25.0, 30.0, 40.0, 50.0, 65.0, 80.0, 100.0, 130.0, 160.0, 200.0, 250.0,
              300.0, 400.0, 500.0, 650.0, 800.0, 1000.0, 2000.0, 5000.0, 10000.0, 20000.0, 50000.0,
              100000.0));
  static final Aggregation AGGREGATION_WITH_MILLIS_HISTOGRAM =
      Distribution.create(BucketBoundaries.create(RPC_MILLIS_BUCKET_BOUNDARIES));

  static MeasureDouble QUERY_STATS_ELAPSED =
      MeasureDouble.create(
          "cloud.google.com/java/spanner/query_stats_elapsed",
          "The execution of the query",
          MILLISECOND);

  // Register the view. It is imperative that this step exists,
  // otherwise recorded metrics will be dropped and never exported.
  static View QUERY_STATS_LATENCY_VIEW = View
      .create(Name.create("cloud.google.com/java/spanner/query_stats_elapsed"),
          "The execution of the query",
          QUERY_STATS_ELAPSED,
          AGGREGATION_WITH_MILLIS_HISTOGRAM,
          Collections.emptyList());

  static ViewManager manager = Stats.getViewManager();
  private static final StatsRecorder STATS_RECORDER = Stats.getStatsRecorder();

  public static void captureQueryStatsMetric(DatabaseClient dbClient) throws IOException {
    manager.registerView(QUERY_STATS_LATENCY_VIEW);

    // Enable OpenCensus exporters to export metrics to Cloud Monitoring.
    // Exporters use Application Default Credentials to authenticate.
    // See https://developers.google.com/identity/protocols/application-default-credentials
    // for more details.
    StackdriverStatsExporter.createAndRegister();

    try (ResultSet resultSet = dbClient.singleUse()
        .analyzeQuery(Statement.of("SELECT SingerId, AlbumId, AlbumTitle FROM Albums"),
            QueryAnalyzeMode.PROFILE)) {

      while (resultSet.next()) {
        System.out.printf(
            "%d %d %s", resultSet.getLong(0), resultSet.getLong(1), resultSet.getString(2));
      }
      Value value = resultSet.getStats().getQueryStats()
          .getFieldsOrDefault("elapsed_time", Value.newBuilder().setStringValue("0 msecs").build());
      double elapasedTime = Double.parseDouble(value.getStringValue().replaceAll(" msecs", ""));
      STATS_RECORDER.newMeasureMap()
          .put(QUERY_STATS_ELAPSED, elapasedTime)
          .record();
    }
  }
}
// [END spanner_opencensus_query_stats_metric]
