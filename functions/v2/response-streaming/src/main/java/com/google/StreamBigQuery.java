/*
 * Copyright 2023 Google LLC
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

package com.example.functions;

// [START functions_response_streaming]
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedWriter;
import java.io.IOException;

public class StreamBigQuery implements HttpFunction {
  @Override
  public void service(HttpRequest request, HttpResponse response) {
    String query = "SELECT abstract FROM `bigquery-public-data.breathe.bioasq` LIMIT 1000";
    streamQueryResult(query, response);
  }

  public static void streamQueryResult(String query, HttpResponse response) {
    try {
      BufferedWriter writer = response.getWriter();
      // Initialize client that will be used to send requests.
      // This client only needs to be created once,
      // and can be reused for multiple requests.
      BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
      TableResult results = bigquery.query(QueryJobConfiguration.of(query));

      results.iterateAll().forEach(
          row -> row.forEach(val -> {
            try {
              writer.write(val.getValue().toString() + "\n");
              writer.flush();
              System.out.println("Successfully flushed row");
            } catch (IOException e) {
              System.out.println("Could not get rows: " + e.toString());
            }
          }));
    } catch (BigQueryException | InterruptedException | IOException e) {
      System.out.println("Query not performed: " + e.toString());
    }
  }
}
// [END functions_response_streaming]