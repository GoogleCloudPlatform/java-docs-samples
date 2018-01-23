package com.example.spanner;

import java.util.Arrays;

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
    Tracing.getExportComponent().getSampledSpanStore().registerSpanNamesForCollection(Arrays.asList(SAMPLE_SPAN));

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
