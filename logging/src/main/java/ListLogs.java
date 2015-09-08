/**
 * Copyright (c) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
// [START imports]
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Strings;
import com.google.api.services.logging.Logging;
import com.google.api.services.logging.LoggingScopes;
import com.google.api.services.logging.model.ListLogsResponse;
import com.google.api.services.logging.model.Log;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
// [END imports]

/**
 * Cloud Logging Java API sample that lists the logs available to a project.
 * Uses the v1beta3 Cloud Logging API, version 1.20.0 or later.
 * See https://cloud.google.com/logging/docs/api/libraries/.
 */
public class ListLogs {

  private static final List<String> LOGGING_SCOPES = Collections.singletonList(
      LoggingScopes.LOGGING_READ);

  private static final String APPLICATION_NAME = "ListLogs sample";

  /**
   * Returns an authorized Cloud Logging API service client that is usable
   * on Google App Engine, Google Compute Engine, workstations with the Google Cloud SDK,
   * and other computers if you install service account private credentials.
   * See https://cloud.google.com/logging/docs/api/tasks.
   */
  // [START auth]
  public static Logging getLoggingService() throws IOException { 
    HttpTransport transport = new NetHttpTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    GoogleCredential credential = GoogleCredential.getApplicationDefault(transport, jsonFactory);
    if (credential.createScopedRequired()) {
      credential = credential.createScoped(LOGGING_SCOPES);
    }
    Logging service = new Logging.Builder(transport, jsonFactory, credential)
        .setApplicationName(APPLICATION_NAME).build();
    return service;
  }
  // [END auth]

  /**
   * Lists the names of the logs visible to a project, which may require fetching multiple
   * pages of results from the Cloud Logging API. This method converts log resource names
   * ("/projects/PROJECTID/logs/SERVICENAME%2FLOGNAME") to simple log names ("SERVICENAME/LOGNAME").
   * 
   * @param service The logging service client returned by getLoggingService.
   * @param projectId The project whose logs are to be listed.
   * @throws IOException If the Cloud Logging API fails because, for example, the project ID
   *     doesn't exist or authorization fails.
   *     See https://cloud.google.com//logging/docs/api/tasks/#java_sample_code.
   */
  // [START listlogs]
  private static void listLogs(Logging service, String projectId) throws IOException {
    final int pageSize = 3;
    final int resourcePrefixLength = ("/projects/" + projectId + "/logs/").length();
    String nextPageToken = "";

    do {
      ListLogsResponse response = service.projects().logs().list(projectId)
          .setPageToken(nextPageToken).setPageSize(pageSize).execute();
      if (response.isEmpty()) {
        break;
      }
      for (Log log: response.getLogs()) {
        System.out.println(URLDecoder.decode(
            log.getName().substring(resourcePrefixLength), "utf-8"));
      }
      nextPageToken = response.getNextPageToken();
    } while (!Strings.isNullOrEmpty(nextPageToken));
    System.out.println("Done.");
  }
  // [END listlogs]

  /**
   * Demonstrates the Cloud Logging API by listing the logs in a project.
   * @param args The project ID.
   * @throws IOException if a Cloud Logging API call fails because, say, the project ID is wrong
   *     or authorization fails.
   */
  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println(String.format("Usage: %s <project-name>",
            ListLogs.class.getSimpleName()));
      return;
    }

    String projectId = args[0];
    Logging service = getLoggingService();
    listLogs(service, projectId);
  }
}
// [END all]
