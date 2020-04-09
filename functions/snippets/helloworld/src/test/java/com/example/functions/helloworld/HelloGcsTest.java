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

package com.example.functions.helloworld;

// [START functions_storage_unit_test]
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.functions.helloworld.eventpojos.GcsEvent;
import com.example.functions.helloworld.eventpojos.MockContext;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.common.testing.TestLogHandler;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;

/**
 * Unit tests for main.java.com.example.functions.helloworld.HelloGcs.
 */
@PrepareForTest(HelloGcs.class)
public class HelloGcsTest {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gson = new Gson();

  private EnvironmentVariables environmentVariables;

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  private static final Logger LOGGER = Logger.getLogger(HelloGcs.class.getName());

  @Before
  public void beforeTest() throws Exception {
    environmentVariables = new EnvironmentVariables();

    LOGGER.addHandler(LOG_HANDLER);

    // Use a new mock for each test
    request = mock(HttpRequest.class);
    response = mock(HttpResponse.class);

    BufferedReader reader = new BufferedReader(new StringReader("{}"));
    when(request.getReader()).thenReturn(reader);

    BufferedWriter writer = new BufferedWriter(new StringWriter());
    when(response.getWriter()).thenReturn(writer);
  }

  @After
  public void afterTest() {
    request = null;
    response = null;
    LOG_HANDLER.clear();
  }

  @Test
  public void helloGcs_shouldPrintUploadedMessage() throws Exception {
    GcsEvent event = new GcsEvent();
    event.setName("foo.txt");

    MockContext context = new MockContext();
    context.eventType = "google.storage.object.finalize";

    new HelloGcs().accept(event, context);

    String message = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("File foo.txt uploaded.").isEqualTo(message);
  }

  @Test
  public void helloGcs_shouldDisregardOtherEvents() throws Exception {
    GcsEvent event = new GcsEvent();
    event.setName("baz.txt");

    MockContext context = new MockContext();
    context.eventType = "google.storage.object.metadataUpdate";

    new HelloGcs().accept(event, context);

    String message = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Unsupported event type: google.storage.object.metadataUpdate").isEqualTo(message);
  }
}
// [END functions_storage_unit_test]
