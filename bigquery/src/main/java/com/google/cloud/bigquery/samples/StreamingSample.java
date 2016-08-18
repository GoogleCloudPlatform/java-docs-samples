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
import com.google.api.services.bigquery.model.TableDataInsertAllRequest;
import com.google.api.services.bigquery.model.TableDataInsertAllResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

/**
 * Example of Bigquery Streaming.
 */
public class StreamingSample {

  /**
   * Empty constructor since this is just a collection of static methods.
   */
  protected StreamingSample() {}

  /**
   * Command line that demonstrates Bigquery streaming.
   *
   * @param args Command line args, should be empty
   * @throws IOException IOexception
   */
  // [START main]
  public static void main(final String[] args) throws IOException {
    final Scanner scanner = new Scanner(System.in);
    System.out.println("Enter your project id: ");
    String projectId = scanner.nextLine();
    System.out.println("Enter your dataset id: ");
    String datasetId = scanner.nextLine();
    System.out.println("Enter your table id: ");
    String tableId = scanner.nextLine();
    scanner.close();

    System.out.println(
        "Enter JSON to stream to BigQuery: \n" + "Press End-of-stream (CTRL-D) to stop");

    JsonReader fromCli = new JsonReader(new InputStreamReader(System.in));

    Iterator<TableDataInsertAllResponse> responses = run(projectId, datasetId, tableId, fromCli);

    while (responses.hasNext()) {
      System.out.println(responses.next());
    }

    fromCli.close();
  }
  // [END main]

  /**
   * Run the bigquery ClI.
   *
   * @param projectId Project id
   * @param datasetId datasetid
   * @param tableId tableid
   * @param rows The source of the JSON rows we are streaming in.
   * @return Returns Iterates through the stream responses
   * @throws IOException Thrown if there is an error connecting to Bigquery.
   * @throws InterruptedException Should never be thrown
   */
  // [START run]
  public static Iterator<TableDataInsertAllResponse> run(
      final String projectId, final String datasetId, final String tableId, final JsonReader rows)
      throws IOException {

    final Bigquery bigquery = BigQueryServiceFactory.getService();
    final Gson gson = new Gson();
    rows.beginArray();

    return new Iterator<TableDataInsertAllResponse>() {

      /**
       * Check whether there is another row to stream.
       *
       * @return True if there is another row in the stream
       */
      public boolean hasNext() {
        try {
          return rows.hasNext();
        } catch (IOException e) {
          e.printStackTrace();
        }
        return false;
      }

      /**
       * Insert the next row, and return the response.
       *
       * @return Next page of data
       */
      public TableDataInsertAllResponse next() {
        try {
          Map<String, Object> rowData =
              gson.<Map<String, Object>>fromJson(rows, (new HashMap<String, Object>()).getClass());
          return streamRow(
              bigquery,
              projectId,
              datasetId,
              tableId,
              new TableDataInsertAllRequest.Rows().setJson(rowData));
        } catch (JsonSyntaxException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
        return null;
      }

      public void remove() {
        this.next();
      }
    };
  }
  // [END run]

  /**
   * Stream the given row into the given bigquery table.
   *
   * @param bigquery The bigquery service
   * @param projectId project id from Google Developers console
   * @param datasetId id of the dataset
   * @param tableId id of the table we're streaming
   * @param row the row we're inserting
   * @return Response from the insert
   * @throws IOException ioexception
   */
  // [START streamRow]
  public static TableDataInsertAllResponse streamRow(
      final Bigquery bigquery,
      final String projectId,
      final String datasetId,
      final String tableId,
      final TableDataInsertAllRequest.Rows row)
      throws IOException {

    return bigquery
        .tabledata()
        .insertAll(
            projectId,
            datasetId,
            tableId,
            new TableDataInsertAllRequest().setRows(Collections.singletonList(row)))
        .execute();
  }
  // [END streamRow]
}
