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
import com.google.cloud.spanner.spi.v1.SpannerRpcViews;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsExporter;
import java.io.IOException;

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
        .build();
    Spanner spanner = options.getService();
    DatabaseClient dbClient = spanner
        .getDatabaseClient(DatabaseId.of(projectId, instanceId, databaseId));
    captureGfeMetric(dbClient);
  }

  // [START spanner_opencensus_capture_gfe_metric]
  static void captureGfeMetric(DatabaseClient dbClient) {
    // Capture GFE Latency and GFE Header missing count.
    SpannerRpcViews.registerGfeLatencyAndHeaderMissingCountViews();

    // Capture only GFE Latency.
    // SpannerRpcViews.registerGfeLatencyView();

    // Capture only GFE Header missing count.
    // SpannerRpcViews.registerGfeHeaderMissingCountView();

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

  // [END spanner_opencensus_capture_gfe_metric]

}
