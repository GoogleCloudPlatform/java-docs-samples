/*
  Copyright 2016, Google, Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package com.example.bigquery;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;
import com.google.cloud.bigquery.TableId;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/** Runs a query against BigQuery. */
public class QuerySample {
  // [START query_config_simple]
  public static void runSimpleQuery(String queryString)
      throws TimeoutException, InterruptedException {
    QueryJobConfiguration queryConfig =
        QueryJobConfiguration.newBuilder(queryString).build();

    runQuery(queryConfig);
  }
  // [END query_config_simple]

  // [START query_config_standard_sql]
  public static void runStandardSqlQuery(String queryString)
      throws TimeoutException, InterruptedException {
    QueryJobConfiguration queryConfig =
        QueryJobConfiguration.newBuilder(queryString)
            // To use standard SQL syntax, set useLegacySql to false.
            // See: https://cloud.google.com/bigquery/sql-reference/
            .setUseLegacySql(false)
            .build();

    runQuery(queryConfig);
  }
  // [END query_config_standard_sql]

  // [START query_config_permanent_table]
  public static void runQueryPermanentTable(
      String queryString,
      String destinationDataset,
      String destinationTable,
      boolean allowLargeResults) throws TimeoutException, InterruptedException {
    QueryJobConfiguration queryConfig =
        QueryJobConfiguration.newBuilder(queryString)
            // Save the results of the query to a permanent table.
            // See: https://cloud.google.com/bigquery/querying-data#permanent-table
            .setDestinationTable(TableId.of(destinationDataset, destinationTable))
            // Allow results larger than the maximum response size.
            // If true, a destination table must be set.
            // See: https://cloud.google.com/bigquery/querying-data#large-results
            .setAllowLargeResults(allowLargeResults)
            .build();

    runQuery(queryConfig);
  }
  // [END query_config_permanent_table]

  // [START query_config_cache]
  public static void runUncachedQuery(String queryString)
      throws TimeoutException, InterruptedException {
    QueryJobConfiguration queryConfig =
        QueryJobConfiguration.newBuilder(queryString)
            // Do not use the query cache. Force live query evaluation.
            // See: https://cloud.google.com/bigquery/querying-data#query-caching
            .setUseQueryCache(false)
            .build();

    runQuery(queryConfig);
  }
  // [END query_config_cache]

  // [START query_config_batch]
  public static void runBatchQuery(String queryString)
      throws TimeoutException, InterruptedException {
    QueryJobConfiguration queryConfig =
        QueryJobConfiguration.newBuilder(queryString)
            // Run at batch priority, which won't count toward concurrent rate
            // limit.
            // See: https://cloud.google.com/bigquery/querying-data#interactive-batch
            .setPriority(QueryJobConfiguration.Priority.BATCH)
            .build();

    runQuery(queryConfig);
  }
  // [END query_config_batch]


  // [START run_query]
  public static void runQuery(QueryJobConfiguration queryConfig)
      throws TimeoutException, InterruptedException {
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

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

    // Get the results.
    QueryResponse response = bigquery.getQueryResults(jobId);
    QueryResult result = response.getResult();

    // Print all pages of the results.
    while (result != null) {
      for (List<FieldValue> row : result.iterateAll()) {
        for (FieldValue val : row) {
          System.out.printf("%s,", val.toString());
        }
        System.out.printf("\n");
      }

      result = result.getNextPage();
    }
  }
  // [END run_query]

  /** Prompts the user for the required parameters to perform a query. */
  public static void main(final String[] args)
      throws IOException, InterruptedException, TimeoutException, ParseException {
    Options options = new Options();

    // Use an OptionsGroup to choose which sample to run.
    OptionGroup samples = new OptionGroup();
    samples.addOption(Option.builder().longOpt("runSimpleQuery").build());
    samples.addOption(Option.builder().longOpt("runStandardSqlQuery").build());
    samples.addOption(Option.builder().longOpt("runPermanentTableQuery").build());
    samples.addOption(Option.builder().longOpt("runUncachedQuery").build());
    samples.addOption(Option.builder().longOpt("runBatchQuery").build());
    samples.isRequired();
    options.addOptionGroup(samples);

    options.addOption(Option.builder().longOpt("query").hasArg().required().build());
    options.addOption(Option.builder().longOpt("destDataset").hasArg().build());
    options.addOption(Option.builder().longOpt("destTable").hasArg().build());
    options.addOption(Option.builder().longOpt("allowLargeResults").build());

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    String query = cmd.getOptionValue("query");
    if (cmd.hasOption("runSimpleQuery")) {
      runSimpleQuery(query);
    } else if (cmd.hasOption("runStandardSqlQuery")) {
      runStandardSqlQuery(query);
    } else if (cmd.hasOption("runPermanentTableQuery")) {
      String destDataset = cmd.getOptionValue("destDataset");
      String destTable = cmd.getOptionValue("destTable");
      boolean allowLargeResults = cmd.hasOption("allowLargeResults");
      runQueryPermanentTable(query, destDataset, destTable, allowLargeResults);
    } else if (cmd.hasOption("runUncachedQuery")) {
      runUncachedQuery(query);
    } else if (cmd.hasOption("runBatchQuery")) {
      runBatchQuery(query);
    }
  }
}
