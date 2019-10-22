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

package com.example.appengine.bigquerylogging;

import com.google.api.Metric;
import com.google.api.MetricDescriptor;
import com.google.cloud.ServiceOptions;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceClient.ListMetricDescriptorsPagedResponse;
import com.google.cloud.monitoring.v3.MetricServiceClient.ListTimeSeriesPagedResponse;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.monitoring.v3.CreateMetricDescriptorRequest;
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import com.google.monitoring.v3.ListMetricDescriptorsRequest;
import com.google.monitoring.v3.ListTimeSeriesRequest;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.TypedValue;
import com.google.protobuf.util.Timestamps;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class BigQueryRunner {
  private static final String CUSTOM_METRIC_FILTER =
      "metric.type = starts_with(\"custom.googleapis.com/\")";
  private static BigQueryRunner instance;

  private static final MetricDescriptor QUERY_DURATION_METRIC = MetricDescriptor
      .newBuilder()
      .setName("custom.googleapis.com/queryDuration")
      .setType("custom.googleapis.com/queryDuration")
      .setDisplayName("queryDuration")
      .setDescription("Time it took a query to run.")
      .setMetricKind(MetricDescriptor.MetricKind.GAUGE)
      .setValueType(MetricDescriptor.ValueType.INT64)
      .build();
  private static final MetricDescriptor ROWS_RETURNED_METRIC = MetricDescriptor
      .newBuilder()
      .setName("custom.googleapis.com/rowsReturned")
      .setType("custom.googleapis.com/rowsReturned")
      .setDisplayName("rowsReturned")
      .setDescription("Total rows returned by the query result.")
      .setMetricKind(MetricDescriptor.MetricKind.GAUGE)
      .setValueType(MetricDescriptor.ValueType.INT64)
      .build();
  private static final Set<MetricDescriptor> REQUIRED_METRICS = ImmutableSet.of(
      QUERY_DURATION_METRIC, ROWS_RETURNED_METRIC
  );

  private static TableResult mostRecentRunResult;
  private static Set<String> existingMetrics = Sets.newHashSet();

  private final MetricServiceClient client;
  private final BigQuery bigquery;
  private final String projectName;
  private PrintStream os;

  // Retrieve a singleton instance
  public static synchronized BigQueryRunner getInstance() throws IOException {
    if (instance == null) {
      instance = new BigQueryRunner();
    }
    return instance;
  }

  private BigQueryRunner() throws IOException {
    this(MetricServiceClient.create(),
        BigQueryOptions.getDefaultInstance().getService(),
        System.out);
  }

  BigQueryRunner(MetricServiceClient metricsClient, BigQuery bigquery, PrintStream os) {
    client = metricsClient;
    this.os = os;
    this.projectName = String.format("projects/%s", ServiceOptions.getDefaultProjectId());
    this.bigquery = bigquery;
  }

  public static TableResult getMostRecentRunResult() {
    return mostRecentRunResult;
  }

  public void runQuery() throws InterruptedException {
    QueryJobConfiguration queryConfig =
        QueryJobConfiguration.newBuilder(
            "SELECT "
                + "CONCAT('https://stackoverflow.com/questions/', CAST(id as STRING)) as url, "
                + "view_count "
                + "FROM `bigquery-public-data.stackoverflow.posts_questions` "
                + "WHERE tags like '%google-bigquery%' "
                + "ORDER BY favorite_count DESC LIMIT 10")
            // Use standard SQL syntax for queries.
            // See: https://cloud.google.com/bigquery/sql-reference/
            .setUseLegacySql(false)
            .build();

    List<TimeSeries> timeSeriesList = new ArrayList<>();

    long queryStartTime = System.currentTimeMillis();

    // Create a job ID so that we can safely retry.
    JobId jobId = JobId.of(UUID.randomUUID().toString());
    Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

    // Wait for the query to complete.
    queryJob = queryJob.waitFor();

    // Check for errors
    if (queryJob == null) {
      throw new RuntimeException("Job no longer exists");
    } else if (queryJob.getStatus().getError() != null) {
      // You can also look at queryJob.getStatus().getExecutionErrors() for all
      // errors, not just the latest one.
      throw new RuntimeException(queryJob.getStatus().getError().toString());
    }

    // Log the result metrics.
    TableResult result = queryJob.getQueryResults();

    long queryEndTime = System.currentTimeMillis();
    // Add query duration metric.
    timeSeriesList.add(prepareMetric(QUERY_DURATION_METRIC, queryEndTime - queryStartTime));

    // Add rows returned metric.
    timeSeriesList.add(prepareMetric(ROWS_RETURNED_METRIC, result.getTotalRows()));

    // Prepares the time series request
    CreateTimeSeriesRequest request = CreateTimeSeriesRequest.newBuilder()
        .setName(projectName)
        .addAllTimeSeries(timeSeriesList)
        .build();

    createMetricsIfNeeded();
    client.createTimeSeries(request);
    os.println("Done writing metrics.");

    mostRecentRunResult = result;
  }

  // Returns a metric time series with a single int64 data point.
  private TimeSeries prepareMetric(MetricDescriptor requiredMetric, long metricValue) {
    TimeInterval interval = TimeInterval.newBuilder()
        .setEndTime(Timestamps.fromMillis(System.currentTimeMillis()))
        .build();
    TypedValue value = TypedValue
        .newBuilder()
        .setInt64Value(metricValue)
        .build();

    Point point = Point.newBuilder()
        .setInterval(interval)
        .setValue(value)
        .build();

    List<Point> pointList = Lists.newArrayList();
    pointList.add(point);

    Metric metric = Metric.newBuilder()
        .setType(requiredMetric.getName())
        .build();

    return TimeSeries.newBuilder()
        .setMetric(metric)
        .addAllPoints(pointList)
        .build();
  }

  public List<TimeSeriesSummary> getTimeSeriesValues() {
    List<TimeSeriesSummary> summaries = Lists.newArrayList();
    createMetricsIfNeeded();
    for (MetricDescriptor metric : REQUIRED_METRICS) {
      ListTimeSeriesRequest listTimeSeriesRequest = ListTimeSeriesRequest
          .newBuilder()
          .setName(projectName)
          .setFilter(String.format("metric.type = \"%s\"", metric.getType()))
          .setInterval(TimeInterval.newBuilder()
              .setStartTime(Timestamps.subtract(Timestamps.fromMillis(System.currentTimeMillis()),
                  com.google.protobuf.Duration.newBuilder()
                      .setSeconds(60L * 60L * 24L * 30L)  //  30 days ago
                      .build()))
              .setEndTime(Timestamps.fromMillis(System.currentTimeMillis()))
              .build())
          .build();
      try {
        ListTimeSeriesPagedResponse listTimeSeriesResponse = client.listTimeSeries(
            listTimeSeriesRequest);
        ArrayList<TimeSeries> timeSeries = Lists.newArrayList(listTimeSeriesResponse.iterateAll());
        summaries.addAll(timeSeries
            .stream()
            .map(TimeSeriesSummary::fromTimeSeries)
            .collect(Collectors.toList()));
      } catch (RuntimeException ex) {
        os.println("MetricDescriptors not yet synced. Please try again in a moment.");
      }
    }
    return summaries;
  }

  private void createMetricsIfNeeded() {
    // If all required metrics already exist, no need to make service calls.
    if (REQUIRED_METRICS.stream()
        .map(MetricDescriptor::getDisplayName)
        .allMatch(existingMetrics::contains)) {
      return;
    }
    ListMetricDescriptorsRequest listMetricsRequest = ListMetricDescriptorsRequest
        .newBuilder()
        .setName(projectName)
        .setFilter(CUSTOM_METRIC_FILTER)
        .build();
    ListMetricDescriptorsPagedResponse listMetricsResponse = client.listMetricDescriptors(
        listMetricsRequest);

    for (MetricDescriptor existingMetric : listMetricsResponse.iterateAll()) {
      existingMetrics.add(existingMetric.getDisplayName());
    }

    REQUIRED_METRICS.stream()
        .filter(metric -> !existingMetrics.contains(metric.getDisplayName()))
        .forEach(this::createMetric);
  }

  private void createMetric(MetricDescriptor newMetric) {
    CreateMetricDescriptorRequest request = CreateMetricDescriptorRequest.newBuilder()
        .setName(projectName)
        .setMetricDescriptor(newMetric)
        .build();

    client.createMetricDescriptor(request);
  }

}
