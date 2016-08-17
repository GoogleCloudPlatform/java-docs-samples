/*
 * Copyright (c) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not  use this file except in compliance with the License. You may obtain a
 * copy of the License at
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

import com.google.api.client.util.Data;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.Bigquery.Datasets;
import com.google.api.services.bigquery.model.DatasetList;
import com.google.api.services.bigquery.model.GetQueryResultsResponse;
import com.google.api.services.bigquery.model.ProjectList;
import com.google.api.services.bigquery.model.QueryRequest;
import com.google.api.services.bigquery.model.QueryResponse;
import com.google.api.services.bigquery.model.TableCell;
import com.google.api.services.bigquery.model.TableRow;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread;
import java.util.List;

/**
 * Invokes the BigQuery basic APIs for the given project id specified.
 *
 * Samples used in this page:
 *
 *     https://cloud.google.com/bigquery/bigquery-api-quickstart
 */
public class ListDatasetsProjects {
  /**
   * Run the sample.
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length != 1) {
      System.err.println("Usage: QuickStart <project-id>");
      return;
    }
    String projectId = args[0];

    Bigquery bigquery = BigQueryServiceFactory.getService();
    String query =
        "SELECT TOP( title, 10) as title, COUNT(*) as revision_count "
            + "FROM [publicdata:samples.wikipedia] WHERE wp_namespace = 0;";

    System.out.println();
    System.out.println("----- Running the asynchronous query and printing it to stdout.");
    runQueryRpcAndPrint(bigquery, projectId, query, System.out);

    System.out.println();
    System.out.println("----- Listing all the Datasets in the projectId");
    listDatasets(bigquery, projectId);

    System.out.println();
    System.out.println("----- Listing all the Projects");
    listProjects(bigquery);
  }

  /**
   * Lists all Datasets in a project specified by the projectId.
   *
   * @param bigquery The BigQuery object.
   * @param projectId The projectId from which lists the existing Datasets.
   * @throws IOException if there's trouble with the network request.
   */
  // [START listDatasets]
  public static void listDatasets(Bigquery bigquery, String projectId) throws IOException {
    Datasets.List datasetRequest = bigquery.datasets().list(projectId);
    DatasetList datasetList = datasetRequest.execute();

    if (datasetList.getDatasets() != null) {
      List<DatasetList.Datasets> datasets = datasetList.getDatasets();
      System.out.println("Dataset list:");

      for (DatasetList.Datasets dataset : datasets) {
        System.out.format("%s\n", dataset.getDatasetReference().getDatasetId());
      }
    }
  }
  // [END listDatasets]

  /**
   * Lists all Projects.
   *
   * @param bigquery The BigQuery object.
   * @throws IOException if there's trouble with the network request.
   */
  // [START listProjects]
  public static void listProjects(Bigquery bigquery) throws IOException {
    Bigquery.Projects.List projectListRequest = bigquery.projects().list();
    ProjectList projectList = projectListRequest.execute();

    if (projectList.getProjects() != null) {
      List<ProjectList.Projects> projects = projectList.getProjects();
      System.out.println("Project list:");

      for (ProjectList.Projects project : projects) {
        System.out.format("%s\n", project.getFriendlyName());
      }
    }
  }
  // [END listProjects]

  /**
   * Runs a synchronous BigQuery query and displays the result.
   *
   * @param bigquery An authorized BigQuery client
   * @param projectId The current project id
   * @param query A String containing a BigQuery SQL statement
   * @param out A PrintStream for output, normally System.out
   */
  static void runQueryRpcAndPrint(
      Bigquery bigquery, String projectId, String query, PrintStream out)
      throws IOException, InterruptedException {
    QueryRequest queryRequest = new QueryRequest().setQuery(query);
    QueryResponse queryResponse = bigquery.jobs().query(projectId, queryRequest).execute();
    if (queryResponse.getJobComplete()) {
      printRows(queryResponse.getRows(), out);
      if (null == queryResponse.getPageToken()) {
        return;
      }
    }
    // This loop polls until results are present, then loops over result pages.
    String pageToken = null;
    while (true) {
      GetQueryResultsResponse queryResults =
          bigquery
              .jobs()
              .getQueryResults(projectId, queryResponse.getJobReference().getJobId())
              .setPageToken(pageToken)
              .execute();
      if (queryResults.getJobComplete()) {
        printRows(queryResults.getRows(), out);
        pageToken = queryResults.getPageToken();
        if (null == pageToken) {
          return;
        }
      }
      Thread.sleep(500);
    }
  }

  /**
   * Print the given rows.
   *
   * @param rows the rows to print.
   * @param out the place to print them.
   */
  private static void printRows(java.util.List<TableRow> rows, PrintStream out) {
    if (rows != null) {
      for (TableRow row : rows) {
        for (TableCell cell : row.getF()) {
          // Data.isNull() is the recommended way to check for the 'null object' in TableCell.
          out.printf("%s, ", Data.isNull(cell.getV()) ? "null" : cell.getV().toString());
        }
        out.println();
      }
    }
  }
}
