/**
 * Copyright (c) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not  use this file except  in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.bigquery.samples;

import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.Bigquery.Jobs.GetQueryResults;
import com.google.api.services.bigquery.model.GetQueryResultsResponse;
import com.google.api.services.bigquery.model.Job;
import com.google.api.services.bigquery.model.JobConfiguration;
import com.google.api.services.bigquery.model.JobConfigurationQuery;

import java.io.IOException;
import java.util.Iterator;

/**
 * Example of authorizing with BigQuery and reading from a public dataset.
 */
public class AsyncQuerySample {
  private static final String DEFAULT_QUERY =
      "SELECT corpus FROM `publicdata.samples.shakespeare` GROUP BY corpus;";

  // [START main]
  /**
   * Prompts for all the parameters required to make a query.
   *
   * @param args Command line args
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   */
  public static void main(final String[] args) throws IOException, InterruptedException {
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

    String useBatchString = System.getProperty("useBatchMode");
    if (useBatchString == null || useBatchString.isEmpty()) {
      useBatchString = "false";
    }
    boolean useBatchMode = Boolean.parseBoolean(useBatchString);
    System.out.printf("useBatchMode: %b\n", useBatchMode);

    String waitTimeString = System.getProperty("waitTime");
    if (waitTimeString == null || waitTimeString.isEmpty()) {
      waitTimeString = "1000";
    }
    long waitTime = Long.parseLong(waitTimeString);
    System.out.printf("waitTime: %d (milliseconds)\n", waitTime);

    String useLegacySqlString = System.getProperty("useLegacySql");
    if (useLegacySqlString == null || useLegacySqlString.isEmpty()) {
      useLegacySqlString = "false";
    }
    boolean useLegacySql = Boolean.parseBoolean(useLegacySqlString);

    Iterator<GetQueryResultsResponse> pages =
        run(projectId, queryString, useBatchMode, waitTime, useLegacySql);
    while (pages.hasNext()) {
      BigQueryUtils.printRows(pages.next().getRows(), System.out);
    }
  }
  // [END main]

  // [START run]
  /**
   * Run the query.
   *
   * @param projectId Get this from Google Developers console
   * @param queryString Query we want to run against BigQuery
   * @param useBatchMode True if you want to batch the queries
   * @param waitTime How long to wait before retries
   * @param useLegacySql Boolean that is false if using standard SQL syntax.
   * @return An iterator to the result of your pages
   * @throws IOException Thrown if there's an IOException
   * @throws InterruptedException Thrown if there's an Interrupted Exception
   */
  public static Iterator<GetQueryResultsResponse> run(
      final String projectId,
      final String queryString,
      final boolean useBatchMode,
      final long waitTime,
      final boolean useLegacySql)
      throws IOException, InterruptedException {

    Bigquery bigquery = BigQueryServiceFactory.getService();

    Job query = asyncQuery(bigquery, projectId, queryString, useBatchMode, useLegacySql);
    Bigquery.Jobs.Get getRequest =
        bigquery.jobs().get(projectId, query.getJobReference().getJobId());

    // Poll every waitTime milliseconds,
    // retrying at most retries times if there are errors
    BigQueryUtils.pollJob(getRequest, waitTime);

    GetQueryResults resultsRequest =
        bigquery.jobs().getQueryResults(projectId, query.getJobReference().getJobId());

    return BigQueryUtils.getPages(resultsRequest);
  }
  // [END run]

  // [START asyncQuery]
  /**
   * Inserts an asynchronous query Job for a particular query.
   *
   * @param bigquery an authorized BigQuery client
   * @param projectId a String containing the project ID
   * @param querySql  the actual query string
   * @param useBatchMode True if you want to run the query as BATCH
   * @param useLegacySql Boolean that is false if using standard SQL syntax.
   * @return a reference to the inserted query job
   * @throws IOException Thrown if there's a network exception
   */
  public static Job asyncQuery(
      final Bigquery bigquery,
      final String projectId,
      final String querySql,
      final boolean useBatchMode,
      final boolean useLegacySql)
      throws IOException {

    JobConfigurationQuery queryConfig =
        new JobConfigurationQuery()
            .setQuery(querySql)
            // Set the useLegacySql parameter to false to use standard SQL syntax. See:
            // https://cloud.google.com/bigquery/sql-reference/enabling-standard-sql
            .setUseLegacySql(useLegacySql);

    if (useBatchMode) {
      queryConfig.setPriority("BATCH");
    }

    Job job = new Job().setConfiguration(new JobConfiguration().setQuery(queryConfig));

    return bigquery.jobs().insert(projectId, job).execute();
  }
  // [END asyncQuery]

}
