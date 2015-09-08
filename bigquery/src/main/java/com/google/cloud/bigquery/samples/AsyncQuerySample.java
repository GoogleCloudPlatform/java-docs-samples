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
import java.util.Scanner;


/**
 * Example of authorizing with BigQuery and reading from a public dataset.
 */
public class AsyncQuerySample extends BigqueryUtils {
  // [START main]
  /**
   * Prompts for all the parameters required to make a query.
   *
   * @param args Command line args
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   */
  public static void main(final String[] args)
      throws IOException, InterruptedException {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Enter your project id: ");
    String projectId = scanner.nextLine();
    System.out.println("Enter your query string: ");
    String queryString = scanner.nextLine();
    System.out.println("Run query in batch mode? [true|false] ");
    boolean batch = Boolean.valueOf(scanner.nextLine());
    System.out.println("Enter how often to check if your job is complete "
        + "(milliseconds): ");
    long waitTime = scanner.nextLong();
    scanner.close();
    Iterator<GetQueryResultsResponse> pages = run(projectId, queryString,
        batch, waitTime);
    while (pages.hasNext()) {
      printRows(pages.next().getRows(), System.out);
    }
  }
  // [END main]

  // [START run]
  /**
   * Run the query.
   *
   * @param projectId Get this from Google Developers console
   * @param queryString Query we want to run against BigQuery
   * @param batch True if you want to batch the queries
   * @param waitTime How long to wait before retries
   * @return An interator to the result of your pages
   * @throws IOException Thrown if there's an IOException
   * @throws InterruptedException Thrown if there's an Interrupted Exception
   */
  public static Iterator<GetQueryResultsResponse> run(final String projectId,
      final String queryString,
      final boolean batch,
      final long waitTime)
      throws IOException, InterruptedException {

    Bigquery bigquery = BigqueryServiceFactory.getService();

    Job query = asyncQuery(bigquery, projectId, queryString, batch);
    Bigquery.Jobs.Get getRequest = bigquery.jobs().get(
        projectId, query.getJobReference().getJobId());

    //Poll every waitTime milliseconds,
    //retrying at most retries times if there are errors
    pollJob(getRequest, waitTime);

    GetQueryResults resultsRequest = bigquery.jobs().getQueryResults(
        projectId, query.getJobReference().getJobId());

    return getPages(resultsRequest);
  }
  // [END run]

  // [START asyncQuery]
  /**
   * Inserts an asynchronous query Job for a particular query.
   *
   * @param bigquery  an authorized BigQuery client
   * @param projectId a String containing the project ID
   * @param querySql  the actual query string
   * @param batch True if you want to run the query as BATCH
   * @return a reference to the inserted query job
   * @throws IOException Thrown if there's a network exception
   */
  public static Job asyncQuery(final Bigquery bigquery,
      final String projectId,
      final String querySql,
      final boolean batch) throws IOException {

    JobConfigurationQuery queryConfig = new JobConfigurationQuery()
        .setQuery(querySql);

    if (batch) {
      queryConfig.setPriority("BATCH");
    }

    Job job = new Job().setConfiguration(
        new JobConfiguration().setQuery(queryConfig));

    return bigquery.jobs().insert(projectId, job).execute();
  }
  // [END asyncQuery]

}
