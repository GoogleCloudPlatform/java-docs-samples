/*
 * Copyright 2020 Google LLC
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

package functions;

// [START functions_log_retrieve]

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.logging.v2.LoggingClient;
import com.google.cloud.logging.v2.LoggingClient.ListLogEntriesPagedResponse;
import com.google.logging.v2.ListLogEntriesRequest;
import com.google.logging.v2.LogEntry;
import java.io.BufferedWriter;
import java.io.IOException;

public class RetrieveLogs implements HttpFunction {

  private LoggingClient client;

  // Retrieve the latest Cloud Function log entries
  @Override
  public void service(HttpRequest request, HttpResponse response)
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

    BufferedWriter writer = response.getWriter();
    for (LogEntry entry : entriesResponse.getPage().getValues()) {
      writer.write(String.format("%s: %s%n", entry.getLogName(), entry.getTextPayload()));
    }
    writer.write("%n%nLogs retrieved successfully.%n");
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
