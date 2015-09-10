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
import java.util.Scanner;
/**
 * Runs a synchronous query against Bigtable.
 */
public class SyncQuerySample {


  /**
   * Protected because this is a collection of static methods.
   */
  protected SyncQuerySample() {

  }

  //[START main]
  /**
   * Prompts the user for the required parameters to perform a query.
   *
   * @param args args
   * @throws IOException ioexceptino
   */
  public static void main(final String[] args)
      throws IOException {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Enter your project id: ");
    String projectId = scanner.nextLine();
    System.out.println("Enter your query string: ");
    String queryString = scanner.nextLine();
    System.out.println("Enter how long to wait for the query to complete"
        + " (in milliseconds):\n "
        + "(if longer than 10 seconds, use an asynchronous query)");
    long waitTime = scanner.nextLong();
    scanner.close();
    Iterator<GetQueryResultsResponse> pages = run(projectId, queryString,
        waitTime);
    while (pages.hasNext()) {
      BigqueryUtils.printRows(pages.next().getRows(), System.out);
    }
  }
  // [END main]


  /**
   * Perform the given query using the synchronous api.
   *
   * @param projectId project id from developer console
   * @param queryString query to run
   * @param waitTime Timeout in milliseconds before we abort
   * @return Iterator that pages through the results of the query
   * @throws IOException ioexception
   */
  // [START run]
  public static Iterator<GetQueryResultsResponse> run(final String projectId,
      final String queryString,
      final long waitTime) throws IOException {
    Bigquery bigquery = BigqueryServiceFactory.getService();
    //Wait until query is done with 10 second timeout, at most 5 retries on error
    QueryResponse query = bigquery.jobs().query(
        projectId,
        new QueryRequest().setTimeoutMs(waitTime).setQuery(queryString))
        .execute();

    //Make a request to get the results of the query
    //(timeout is zero since job should be complete)

    GetQueryResults getRequest = bigquery.jobs().getQueryResults(
        query.getJobReference().getProjectId(),
        query.getJobReference().getJobId());


    return BigqueryUtils.getPages(getRequest);
  }
  // [END run]


}
