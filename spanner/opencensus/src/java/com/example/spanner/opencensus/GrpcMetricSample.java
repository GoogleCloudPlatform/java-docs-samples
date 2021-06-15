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

// [START spanner_opencensus_grpc_metric]
// Imports the Google Cloud client library

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;
import io.opencensus.contrib.grpc.metrics.RpcViews;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsExporter;

/**
 * This sample demonstrates how to record and export client round-trip latency using OpenCensus.
 */
public class GrpcMetricSample {

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: GrpcMetricSample <instance_id> <database_id>");
      return;
    }

    SpannerOptions options = SpannerOptions.newBuilder().build();
    Spanner spanner = options.getService();
    String instanceId = args[0];
    String databaseId = args[1];

    // Creates a database client.
    DatabaseClient dbClient = spanner.getDatabaseClient(DatabaseId.of(
        options.getProjectId(), instanceId, databaseId));

    // Register basic gRPC views.
    RpcViews.registerClientGrpcBasicViews();

    // Enable OpenCensus exporters to export metrics to Stackdriver Monitoring.
    // Exporters use Application Default Credentials to authenticate.
    // See https://developers.google.com/identity/protocols/application-default-credentials
    // for more details.
    StackdriverStatsExporter.createAndRegister();

    try (ResultSet resultSet =
        dbClient
            .singleUse() // Execute a single read or query against Cloud Spanner.
            .executeQuery(Statement.of("SELECT SingerId, AlbumId, AlbumTitle FROM Albums"))) {
      while (resultSet.next()) {
        System.out.printf(
            "%d %d %s\n", resultSet.getLong(0), resultSet.getLong(1), resultSet.getString(2));
      }
    } finally {
      // Closes the client which will free up the resources used
      spanner.close();
    }
  }
}

// [END spanner_opencensus_grpc_metric]
