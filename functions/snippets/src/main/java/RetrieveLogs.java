/*
 * Copyright 2019 Google LLC
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

// [START functions_log_retrieve]

import com.google.cloud.logging.v2.LoggingClient;
import com.google.cloud.logging.v2.LoggingClient.ListLogEntriesPagedResponse;
import com.google.logging.v2.ListLogEntriesRequest;
import com.google.logging.v2.LogEntry;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RetrieveLogs {

  private LoggingClient client;

  // Retrieve the latest Cloud Function log entries
  public void retrieveLogs(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // Get the LoggingClient for the function.
    client = getClient();

    // Construct the request
    ListLogEntriesRequest entriesRequest =
        ListLogEntriesRequest.newBuilder()
            .setPageSize(10)
            .setFilter("resource.type=\"cloud_function\"")
            .build();

    ListLogEntriesPagedResponse entriesResponse = client.listLogEntries(entriesRequest);

    PrintWriter writer = response.getWriter();
    for (LogEntry entry : entriesResponse.getPage().getValues()) {
      writer.println(String.format("%s: %s", entry.getLogName(), entry.getTextPayload()));
    }
    writer.println("\n\nLogs retrieved successfully.");
  }

  // Returns a client for interacting with the Logging API. The client is stored in the global scope
  // and reused by all requests.
  private LoggingClient getClient() throws IOException {
    if (client == null) {
      client = LoggingClient.create();
    }
    return client;
  }
}

// [END functions_log_retrieve]
