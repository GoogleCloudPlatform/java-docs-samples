/*
  Copyright 2016, Google, Inc.

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

package com.example.logging;

// [START logging_quickstart]
// Imports the Google Cloud client library

import com.google.cloud.MonitoredResource;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.logging.Payload.StringPayload;

import java.util.Collections;

public class QuickstartSample {

  public static void main(String... args) throws Exception {
    // Instantiates a client
    Logging logging = LoggingOptions.getDefaultInstance().getService();

    // The name of the log to write to
    String logName = args[0];  // "my-log";

    // The data to write to the log
    String text = "Hello, world!";

    LogEntry entry = LogEntry.newBuilder(StringPayload.of(text))
        .setLogName(logName)
        .setResource(MonitoredResource.newBuilder("global").build())
        .build();

    // Writes the log entry
    logging.write(Collections.singleton(entry));

    System.out.printf("Logged: %s%n", text);
  }
}
// [END logging_quickstart]
