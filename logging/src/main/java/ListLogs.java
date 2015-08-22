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
// [START all]
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.logging.Logging;
import com.google.api.services.logging.model.ListLogsResponse;
import com.google.api.services.logging.model.Log;

/**
 * Cloud Logging Java API sample that lists the logs available to a project.
 * Uses the v1beta3 Cloud Logging API, version 1.20.0 or later.
 * See https://cloud.google.com/logging/docs/api/libraries/.
 */
public class ListLogs {

  private static final List<String> LOGGING_SCOPES = Collections.singletonList(
      "https://www.googleapis.com/auth/logging.read");
  
  private static final String APPLICATION_NAME = "ListLogs sample";

  /** Returns an authorized Cloud Logging API service client. */
  public static Logging getLoggingService() throws GeneralSecurityException,
      IOException {
    GoogleCredential credential = GoogleCredential.getApplicationDefault()
        .createScoped(LOGGING_SCOPES);
    Logging service = new Logging.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),
        JacksonFactory.getDefaultInstance(),
        credential).setApplicationName(APPLICATION_NAME).build();
    return service;
  }

  /** Extract simple log names from URL-encoded resource names. */
  public static List<String> getSimpleLogNames(List<Log> logs,
      String projectId) throws UnsupportedEncodingException {
    final int RESOURCE_PREFIX_LENGTH = ("/projects/" + projectId + "/logs/")
        .length();
    List<String> logNames = new ArrayList<String>();
    for (Log log: logs) {
      logNames.add(URLDecoder.decode(log.getName(), "utf-8").substring(
          RESOURCE_PREFIX_LENGTH));
    }
    return logNames;
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println(String.format("Usage: %s <project-name>",
            ListLogs.class.getSimpleName()));
      return;
    }

    String projectId = args[0];
    Logging service = getLoggingService();
    ListLogsResponse response = service.projects().logs().list(projectId)
        .execute();
    System.out.println("RAW: " + response.toPrettyString());
    System.out.println("SIMPLE: " +
        getSimpleLogNames(response.getLogs(), projectId));

  }
}
// [END all]
