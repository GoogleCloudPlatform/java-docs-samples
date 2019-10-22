/*
 * Copyright 2018 Google Inc.
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
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.Statement;

import io.opencensus.common.Scope;
import io.opencensus.contrib.grpc.metrics.RpcViews;
import io.opencensus.contrib.zpages.ZPageHandlers;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsExporter;
import io.opencensus.exporter.trace.stackdriver.StackdriverExporter;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;

import java.util.Arrays;

/**
 * This sample demonstrates how to enable opencensus tracing and stats in cloud spanner client.
 */
public class TracingSample {
  
  private static final String SAMPLE_SPAN = "CloudSpannerSample";

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Usage: TracingSample <instance_id> <database_id>");
      return;
    }
    SpannerOptions options = SpannerOptions.newBuilder().build();
    Spanner spanner = options.getService();

    // Installs a handler for /tracez page.
    ZPageHandlers.startHttpServerAndRegisterAll(8080);
    // Installs an exporter for stack driver traces.
    StackdriverExporter.createAndRegister();
    Tracing.getExportComponent().getSampledSpanStore().registerSpanNamesForCollection(
        Arrays.asList(SAMPLE_SPAN));

    // Installs an exporter for stack driver stats.
    StackdriverStatsExporter.createAndRegister();
    RpcViews.registerAllCumulativeViews();
    
    // Name of your instance & database.
    String instanceId = args[0];
    String databaseId = args[1];
    try {
      // Creates a database client
      DatabaseClient dbClient = spanner.getDatabaseClient(DatabaseId.of(
          options.getProjectId(), instanceId, databaseId));
      // Queries the database
      try (Scope ss = Tracing.getTracer()
          .spanBuilderWithExplicitParent(SAMPLE_SPAN, null)
          .setSampler(Samplers.alwaysSample())
          .startScopedSpan()) {
        ResultSet resultSet = dbClient.singleUse().executeQuery(Statement.of("SELECT 1"));

        System.out.println("\n\nResults:");
        // Prints the results
        while (resultSet.next()) {
          System.out.printf("%d\n\n", resultSet.getLong(0));
        }
      }
    } finally {
      // Closes the client which will free up the resources used
      spanner.close();
    }
  }

}
