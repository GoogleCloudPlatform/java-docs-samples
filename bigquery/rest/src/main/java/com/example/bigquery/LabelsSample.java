/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bigquery;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Sample demonstrating labeling a BigQuery dataset or table. */
public class LabelsSample {

  // [START bigquery_label_dataset]
  static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  static final JsonFactory JSON_FACTORY = new JacksonFactory();

  public static class Dataset {
    @Key private Map<String, String> labels;

    public Map<String, String> getLabels() {
      return this.labels;
    }

    public Dataset addLabel(String key, String value) {
      if (this.labels == null) {
        this.labels = new HashMap<>();
      }
      this.labels.put(key, value);
      return this;
    }
  }

  /**
   * Add or modify a label on a dataset.
   *
   * <p>See <a href="https://cloud.google.com/bigquery/docs/labeling-datasets">the BigQuery
   * documentation</a>.
   */
  public static void labelDataset(
      String projectId, String datasetId, String labelKey, String labelValue) throws IOException {

    // Authenticate requests using Google Application Default credentials.
    GoogleCredential credential = GoogleCredential.getApplicationDefault();
    credential = credential.createScoped(Arrays.asList("https://www.googleapis.com/auth/bigquery"));

    // Get a new access token.
    // Note that access tokens have an expiration. You can reuse a token rather than requesting a
    // new one if it is not yet expired.
    credential.refreshToken();
    String accessToken = credential.getAccessToken();

    // Set the content of the request.
    Dataset dataset = new Dataset();
    dataset.addLabel(labelKey, labelValue);
    HttpContent content = new JsonHttpContent(JSON_FACTORY, dataset);

    // Send the request to the BigQuery API.
    String urlFormat =
        "https://www.googleapis.com/bigquery/v2/projects/%s/datasets/%s"
            + "?fields=labels&access_token=%s";
    GenericUrl url = new GenericUrl(String.format(urlFormat, projectId, datasetId, accessToken));
    HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
    HttpRequest request = requestFactory.buildPostRequest(url, content);
    request.setParser(JSON_FACTORY.createJsonObjectParser());

    // Workaround for transports which do not support PATCH requests.
    // See: http://stackoverflow.com/a/32503192/101923
    request.setHeaders(new HttpHeaders().set("X-HTTP-Method-Override", "PATCH"));
    HttpResponse response = request.execute();

    // Check for errors.
    if (response.getStatusCode() != 200) {
      throw new RuntimeException(response.getStatusMessage());
    }

    Dataset responseDataset = response.parseAs(Dataset.class);
    System.out.printf(
        "Updated label \"%s\" with value \"%s\"\n",
        labelKey, responseDataset.getLabels().get(labelKey));
  }
  // [END bigquery_label_dataset]

  // [START bigquery_label_table]
  public static class Table {
    @Key private Map<String, String> labels;

    public Map<String, String> getLabels() {
      return this.labels;
    }

    public Table addLabel(String key, String value) {
      if (this.labels == null) {
        this.labels = new HashMap<>();
      }
      this.labels.put(key, value);
      return this;
    }
  }

  /**
   * Add or modify a label on a table.
   *
   * <p>See <a href="https://cloud.google.com/bigquery/docs/labeling-datasets">the BigQuery
   * documentation</a>.
   */
  public static void labelTable(
      String projectId,
      String datasetId,
      String tableId,
      String labelKey,
      String labelValue)
      throws IOException {

    // Authenticate requests using Google Application Default credentials.
    GoogleCredential credential = GoogleCredential.getApplicationDefault();
    credential = credential.createScoped(Arrays.asList("https://www.googleapis.com/auth/bigquery"));

    // Get a new access token.
    // Note that access tokens have an expiration. You can reuse a token rather than requesting a
    // new one if it is not yet expired.
    credential.refreshToken();
    String accessToken = credential.getAccessToken();

    // Set the content of the request.
    Table table = new Table();
    table.addLabel(labelKey, labelValue);
    HttpContent content = new JsonHttpContent(JSON_FACTORY, table);

    // Send the request to the BigQuery API.
    String urlFormat =
        "https://www.googleapis.com/bigquery/v2/projects/%s/datasets/%s/tables/%s"
            + "?fields=labels&access_token=%s";
    GenericUrl url =
        new GenericUrl(String.format(urlFormat, projectId, datasetId, tableId, accessToken));
    HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
    HttpRequest request = requestFactory.buildPostRequest(url, content);
    request.setParser(JSON_FACTORY.createJsonObjectParser());

    // Workaround for transports which do not support PATCH requests.
    // See: http://stackoverflow.com/a/32503192/101923
    request.setHeaders(new HttpHeaders().set("X-HTTP-Method-Override", "PATCH"));
    HttpResponse response = request.execute();

    // Check for errors.
    if (response.getStatusCode() != 200) {
      throw new RuntimeException(response.getStatusMessage());
    }

    Table responseTable = response.parseAs(Table.class);
    System.out.printf(
        "Updated label \"%s\" with value \"%s\"\n",
        labelKey, responseTable.getLabels().get(labelKey));
  }
  // [END bigquery_label_table]

  public static void printUsage() {
    System.err.println("Command expects 4 or 5 arguments:");
    System.err.println("\tproject dataset [table] key value");
  }

  public static void main(final String[] args) throws IOException, InterruptedException {
    if (args.length != 4 && args.length != 5) {
      printUsage();
      System.exit(1);
    }

    if (args.length == 4) {
      String projectId = args[0];
      String datasetId = args[1];
      String labelKey = args[2];
      String labelValue = args[3];
      labelDataset(projectId, datasetId, labelKey, labelValue);
    } else {
      String projectId = args[0];
      String datasetId = args[1];
      String tableId = args[2];
      String labelKey = args[3];
      String labelValue = args[4];
      labelTable(projectId, datasetId, tableId, labelKey, labelValue);
    }
  }
}
