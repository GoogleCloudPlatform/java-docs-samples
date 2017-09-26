/*
  Copyright 2017 Google Inc.

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

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.MonitoredResource;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.logging.Payload.StringPayload;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for quickstart sample.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class LoggingIT {

  private ByteArrayOutputStream bout;
  private PrintStream out;
  private Logging logging = LoggingOptions.getDefaultInstance().getService();

  private void deleteLog(String logName) {
    logging.deleteLog(logName);
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testQuickstart() throws Exception {
    String logName = "my-log";
    deleteLog(logName);
    QuickstartSample.main(logName);
    String got = bout.toString();
    assertThat(got).contains("Logged: Hello, world!");
    deleteLog(logName);
  }

  @Test(timeout = 10000)
  public void testWriteAndListLogs() throws Exception {
    String logName = "test-log";
    deleteLog(logName);
    // write a log entry
    LogEntry entry = LogEntry.newBuilder(StringPayload.of("Hello world again"))
        .setLogName(logName)
        .setResource(MonitoredResource.newBuilder("global").build())
        .build();
    logging.write(Collections.singleton(entry));
    // flush out log immediately
    logging.flush();
    bout.reset();
    while (bout.toString().isEmpty()) {
      ListLogs.main(logName);
      Thread.sleep(1000);
    }
    assertThat(bout.toString().contains("Hello world again")).isTrue();
    deleteLog(logName);
  }
}
