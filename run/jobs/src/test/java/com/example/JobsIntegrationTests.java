/*
 * Copyright 2022 Google LLC
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

package com.example;

import static org.junit.Assert.assertTrue;

import com.google.api.gax.paging.Page;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.Logging.EntryListOption;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.logging.Payload;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.InterruptedByTimeoutException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class JobsIntegrationTests {

  private static final String project = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String suffix = UUID.randomUUID().toString();
  private static String service;

  @BeforeClass
  public static void setup() throws Exception {
    service = "job-quickstart-" + suffix;

    ProcessBuilder setup = new ProcessBuilder();
    setup.command(
        "gcloud",
        "builds",
        "submit",
        "--project=" + project,
        "--config=./src/test/java/com/example/resources/e2e_test_setup.yaml",
        String.format("--substitutions=_SERVICE=%s,_VERSION=%s", service, suffix));

    setup.redirectErrorStream(true);
    System.out.println("Start Cloud Build...");
    Process p = setup.start();

    // Read process output
    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line;
    while ((line = in.readLine()) != null) {
      System.out.println(line);
    }
    in.close();
    System.out.println("Cloud Build completed.");
  }

  @AfterClass
  public static void cleanup() throws IOException, InterruptedException {
    ProcessBuilder cleanup = new ProcessBuilder();
    cleanup.command(
        "gcloud",
        "builds",
        "submit",
        "--config",
        "./src/test/java/com/example/resources/e2e_test_cleanup.yaml",
        "--region=us-central1",
        "--project=" + project,
        String.format("--substitutions _SERVICE=%s,_VERSION=%s", service, suffix));

    cleanup.start();
  }

  @Test
  public void generatesLogs() throws Exception {
    try (Logging logging = LoggingOptions.getDefaultInstance().getService()) {

      Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      calendar.add(Calendar.MINUTE, -5);
      DateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
      String logFilter =
          "resource.type = \"cloud_run_revision\""
              + " resource.labels.service_name = \""
              + service
              + "\" resource.labels.location = \"us-central1\""
              + " timestamp>=\""
              + rfc3339.format(calendar.getTime())
              + "\" -protoPayload.serviceName=\"run.googleapis.com\"";

      System.out.println(logFilter);
      Boolean found = false;
      // Retry up to 5 times
      for (int i = 1; i <= 5; i++) {
        Page<LogEntry> entries = logging.listLogEntries(EntryListOption.filter(logFilter));
        for (LogEntry logEntry : entries.iterateAll()) {
          if (!logEntry.getLogName().contains("cloudaudit")) {
            Payload<String> payload = logEntry.getPayload();
            if (payload.getData().contains("Task")) { 
              found = true;
              break;
            }
          }
        }
        Thread.sleep(i * 30000);
      }
      assertTrue("Log was not found.", found);
    }
  }
}
