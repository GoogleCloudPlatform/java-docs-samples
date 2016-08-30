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
import com.google.cloud.bigquery.QueryRequest;
import com.google.cloud.bigquery.QueryResponse;
import com.google.cloud.bigquery.QueryResult;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Runs a synchronous query against BigQuery.
 */
public class SyncQuerySample {
  private static final String DEFAULT_QUERY =
      "SELECT corpus FROM `publicdata.samples.shakespeare` GROUP BY corpus;";
  private static final long TEN_SECONDS_MILLIS = 10000;

  /**
   * Prompts the user for the required parameters to perform a query.
   */
  public static void main(final String[] args) throws IOException {
    String queryString = System.getProperty("query");
    if (queryString == null || queryString.isEmpty()) {
      System.out.println("The query property was not set, using default.");
      queryString = DEFAULT_QUERY;
    }
    System.out.printf("query: %s\n", queryString);

    String waitTimeString = System.getProperty("waitTime");
    if (waitTimeString == null || waitTimeString.isEmpty()) {
      waitTimeString = "1000";
    }
    long waitTime = Long.parseLong(waitTimeString);
    System.out.printf("waitTime: %d (milliseconds)\n", waitTime);
    if (waitTime > TEN_SECONDS_MILLIS) {
      System.out.println(
          "WARNING: If the query is going to take longer than 10 seconds to complete, use an"
          + " asynchronous query.");
    }

    String useLegacySqlString = System.getProperty("useLegacySql");
    if (useLegacySqlString == null || useLegacySqlString.isEmpty()) {
      useLegacySqlString = "false";
    }
    boolean useLegacySql = Boolean.parseBoolean(useLegacySqlString);

    run(System.out, queryString, waitTime, useLegacySql);
  }

  /**
   * Perform the given query using the synchronous api.
   *
   * @param out stream to write results to
   * @param queryString query to run
   * @param waitTime Timeout in milliseconds before we abort
   * @param useLegacySql Boolean that is false if using standard SQL syntax.
   */
  // [START run]
  public static void run(
      final PrintStream out,
      final String queryString,
      final long waitTime,
      final boolean useLegacySql) throws IOException {
    BigQuery bigquery =
        new BigQueryOptions.DefaultBigqueryFactory().create(BigQueryOptions.defaultInstance());

    QueryRequest queryRequest =
        QueryRequest.builder(queryString)
            .maxWaitTime(waitTime)
            // Use standard SQL syntax or legacy SQL syntax for queries.
            // See: https://cloud.google.com/bigquery/sql-reference/
            .useLegacySql(useLegacySql)
            .build();
    QueryResponse response = bigquery.query(queryRequest);

    if (response.hasErrors()) {
      throw new RuntimeException(
          response
              .executionErrors()
              .stream()
              .<String>map(err -> err.message())
              .collect(Collectors.joining("\n")));
    }

    QueryResult result = response.result();
    Iterator<List<FieldValue>> iter = result.iterateAll();
    while (iter.hasNext()) {
      List<FieldValue> row = iter.next();
      out.println(row.stream().map(val -> val.toString()).collect(Collectors.joining(",")));
    }
  }
  // [END run]
}
