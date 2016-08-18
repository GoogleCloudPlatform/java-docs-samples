/*
 Copyright 2015, Google, Inc.
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
package com.google.cloud.bigquery.samples;

import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.Bigquery.Jobs.GetQueryResults;
import com.google.api.services.bigquery.model.GetQueryResultsResponse;
import com.google.api.services.bigquery.model.QueryRequest;
import com.google.api.services.bigquery.model.QueryResponse;

import java.io.IOException;
import java.util.Iterator;

/**
 * Runs a synchronous query against Bigtable.
 */
public class SyncQuerySample {
  private static final String DEFAULT_QUERY =
      "SELECT corpus FROM `publicdata.samples.shakespeare` GROUP BY corpus;";
  private static final long TEN_SECONDS_MILLIS = 10000;

  /**
   * Protected because this is a collection of static methods.
   */
  protected SyncQuerySample() {}

  //[START main]
  /**
   * Prompts the user for the required parameters to perform a query.
   *
   * @param args args
   * @throws IOException ioexceptino
   */
  public static void main(final String[] args) throws IOException {
    String projectId = System.getProperty("projectId");
    if (projectId == null || projectId.isEmpty()) {
      System.err.println("The projectId property must be set.");
      System.exit(1);
    }
    System.out.printf("projectId: %s\n", projectId);

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

    Iterator<GetQueryResultsResponse> pages = run(projectId, queryString, waitTime, useLegacySql);
    while (pages.hasNext()) {
      BigQueryUtils.printRows(pages.next().getRows(), System.out);
    }
  }
  // [END main]

  /**
   * Perform the given query using the synchronous api.
   *
   * @param projectId project id from developer console
   * @param queryString query to run
   * @param waitTime Timeout in milliseconds before we abort
   * @param useLegacySql Boolean that is false if using standard SQL syntax.
   * @return Iterator that pages through the results of the query
   * @throws IOException ioexception
   */
  // [START run]
  public static Iterator<GetQueryResultsResponse> run(
      final String projectId,
      final String queryString,
      final long waitTime,
      final boolean useLegacySql) throws IOException {
    Bigquery bigquery = BigQueryServiceFactory.getService();

    // Wait until query is done with `waitTime` millisecond timeout, at most 5 retries on error.
    QueryResponse query =
        bigquery
            .jobs()
            .query(
                projectId,
                new QueryRequest()
                    .setTimeoutMs(waitTime)
                    .setQuery(queryString)
                    // Set the useLegacySql parameter to false to use standard SQL syntax. See:
                    // https://cloud.google.com/bigquery/sql-reference/enabling-standard-sql
                    .setUseLegacySql(useLegacySql))
            .execute();

    // Make a request to get the results of the query.
    GetQueryResults getRequest =
        bigquery
            .jobs()
            .getQueryResults(
                query.getJobReference().getProjectId(), query.getJobReference().getJobId());

    return BigQueryUtils.getPages(getRequest);
  }
  // [END run]

}
