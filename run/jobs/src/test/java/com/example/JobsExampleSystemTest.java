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

package com.example;

import com.google.api.gax.paging.Page;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.Logging.EntryListOption;
import com.google.cloud.logging.LoggingOptions;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class JobsExampleSystemTest {
  // Environment variables
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String SERVICE_NAME = System.getenv().getOrDefault(
      "SERVICE_NAME", "cr-jobs-test");
  private static final String SAMPLE_VERSION = System.getenv().getOrDefault("SAMPLE_VERSION", "manual");

  // Settings
  private static final String JOB_NAME = SERVICE_NAME + "-" + SAMPLE_VERSION;
  private static final String REGION = "us-central1";
  private static final String JOB_NUM = "10";

  public static String runGCloudCommand(String cmd) throws IOException, InterruptedException {
    String baseDir = System.getProperty("user.dir");

    ProcessBuilder builder = new ProcessBuilder()
        .command(cmd.split(" "))
        .directory(new File(baseDir));

    Process process = builder.start();
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

    InputStream stdoutStream = process.getInputStream();
    InputStream stderrStream = process.getErrorStream();

    process.waitFor();

    // Fetch command output, in case it's needed for debugging
    outBytes.write(stdoutStream.readNBytes(stdoutStream.available()));
    outBytes.write(stderrStream.readNBytes(stderrStream.available()));

    String output = outBytes.toString(StandardCharsets.UTF_8);

    // Done!
    return output;
  }

  @BeforeClass
  public static void setUp() throws Exception {
    String substitutions = String.format(
        "--substitutions _JOB=%s,_VERSION=%s",
        JOB_NAME, SAMPLE_VERSION
    );
    String command = "gcloud builds submit " +
      "--project " + PROJECT_ID + " " +
      "--config e2e_test_setup.yaml " +
      substitutions;

    String output = runGCloudCommand(command);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    String substitutions = String.format(
        "--substitutions _JOB=%s,_REGION=%s",
        JOB_NAME, REGION
    );
    String command = "gcloud builds submit " +
        "--project " + PROJECT_ID + " " +
        "--config e2e_test_cleanup.yaml " +
        substitutions;

    runGCloudCommand(command);
  }

  @Test
  public void ranAllTasksSuccessfully() {
    // Ignore logs older than 4 minutes
    Instant startInstant = Instant.now().minus(Duration.ofMinutes(4));
    String startTimestamp = DateTimeFormatter.ISO_INSTANT.format(startInstant);

    // Construct log filter
    String logFilter =
        "resource.type=\"cloud_run_revision\" " +
        "resource.labels.service_name=\"" + JOB_NAME + "\" " +
        "timestamp >= \"" + startTimestamp + "\" " +
        "\"Starting\"";

    // Get matching log entries
    LoggingOptions options = LoggingOptions.getDefaultInstance();
    Logging logging = options.getService();

    Page<LogEntry> entries = logging.listLogEntries(
        EntryListOption.filter(logFilter));

    Iterator<LogEntry> entryIterator = entries.iterateAll().iterator();

    // Check that a matching log entry was found
    assert entryIterator.hasNext();
  }
}
