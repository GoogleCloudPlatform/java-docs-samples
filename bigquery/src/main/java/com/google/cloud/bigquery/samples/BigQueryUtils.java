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

import com.google.api.client.json.GenericJson;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.Bigquery.Datasets;
import com.google.api.services.bigquery.BigqueryRequest;
import com.google.api.services.bigquery.model.DatasetList;
import com.google.api.services.bigquery.model.Job;
import com.google.api.services.bigquery.model.TableCell;
import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Helper functions for the other classes.
 */
public class BigQueryUtils {

  /**
   * Private constructor to prevent creation of this class, which is just all
   * static helper methods.
   */
  private BigQueryUtils() {}

  /**
   * Print rows to the output stream in a formatted way.
   * @param rows rows in bigquery
   * @param out Output stream we want to print to
   */
  // [START print_rows]
  public static void printRows(final List<TableRow> rows, final PrintStream out) {
    for (TableRow row : rows) {
      for (TableCell field : row.getF()) {
        out.printf("%-50s", field.getV());
      }
      out.println();
    }
  }
  // [END print_rows]

  /**
   * Polls the job for completion.
   * @param request The bigquery request to poll for completion
   * @param interval Number of milliseconds between each poll
   * @return The finished job
   * @throws IOException IOException
   * @throws InterruptedException InterruptedException
   */
  // [START poll_job]
  public static Job pollJob(final Bigquery.Jobs.Get request, final long interval)
      throws IOException, InterruptedException {
    Job job = request.execute();
    while (!job.getStatus().getState().equals("DONE")) {
      System.out.println(
          "Job is " + job.getStatus().getState() + " waiting " + interval + " milliseconds...");
      Thread.sleep(interval);
      job = request.execute();
    }
    return job;
  }
  // [END poll_job]

  /**
   * Pages through the results of an arbitrary Bigquery request.
   * @param requestTemplate  The object that represents the call to fetch
   *                          the results.
   * @param <T> The type of the returned objects
   * @return An iterator that pages through the returned object
   */
  // [START paging]
  public static <T extends GenericJson> Iterator<T> getPages(
      final BigqueryRequest<T> requestTemplate) {

    /**
     * An iterator class that pages through a Bigquery request.
     */
    class PageIterator implements Iterator<T> {

      private BigqueryRequest<T> request;
      private boolean hasNext = true;

      /**
       * Inner class that represents our iterator to page through results.
       * @param requestTemplate The object that represents the call to fetch
       *                          the results.
       */
      public PageIterator(final BigqueryRequest<T> requestTemplate) {
        this.request = requestTemplate;
      }

      /**
       * Checks whether there is another page of results.
       * @return True if there is another page of results.
       */
      public boolean hasNext() {
        return hasNext;
      }

      /**
       * Returns the next page of results.
       * @return The next page of resul.ts
       */
      public T next() {
        if (!hasNext) {
          throw new NoSuchElementException();
        }
        try {
          T response = request.execute();
          if (response.containsKey("pageToken")) {
            request = request.set("pageToken", response.get("pageToken"));
          } else {
            hasNext = false;
          }
          return response;
        } catch (IOException e) {
          e.printStackTrace();
          return null;
        }
      }

      /**
       * Skips the page by moving the iterator to the next page.
       */
      public void remove() {
        this.next();
      }
    }

    return new PageIterator(requestTemplate);
  }
  // [END paging]

  /**
   * Loads a Bigquery schema.
   * @param schemaSource  The source of the schema
   * @return The TableSchema
   */
  // [START load_schema]
  public static TableSchema loadSchema(final Reader schemaSource) {
    TableSchema sourceSchema = new TableSchema();

    List<TableFieldSchema> fields =
        (new Gson())
            .<List<TableFieldSchema>>fromJson(
                schemaSource, (new ArrayList<TableFieldSchema>()).getClass());

    sourceSchema.setFields(fields);

    return sourceSchema;
  }
  // [END load_schema]

  // [START list_datasets]
  /**
   * Display all BigQuery datasets associated with a project.
   *
   * @param bigquery  an authorized BigQuery client
   * @param projectId a string containing the current project ID
   * @throws IOException Thrown if there is a network error connecting to
   *                     Bigquery.
   */
  public static void listDatasets(final Bigquery bigquery, final String projectId)
      throws IOException {
    Datasets.List datasetRequest = bigquery.datasets().list(projectId);
    DatasetList datasetList = datasetRequest.execute();
    if (datasetList.getDatasets() != null) {
      List<DatasetList.Datasets> datasets = datasetList.getDatasets();
      System.out.println("Available datasets\n----------------");
      System.out.println(datasets.toString());
      for (DatasetList.Datasets dataset : datasets) {
        System.out.format("%s\n", dataset.getDatasetReference().getDatasetId());
      }
    }
  }
  // [END list_datasets]
}
