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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

//@RunWith(PowerMockRunner.class)
@PrepareForTest(HelloGcs.class)
public class HelloGcsIT {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gson = new Gson();

  private EnvironmentVariables environmentVariables;

  public final TestLogHandler logHandler = new TestLogHandler();

  private Logger logger;

  @Before
  public void beforeTest() throws Exception {
    environmentVariables = new EnvironmentVariables();

    logger = Logger.getLogger(HelloGcs.class.getName());
    logger.addHandler(logHandler);

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
    logHandler.clear();
    Mockito.reset();
  }

  @Test
  public void helloGcs_shouldPrintUploadedMessage() throws Exception {
    GcsEvent event = new GcsEvent();
    event.name = "foo.txt";

    MockContext context = new MockContext();
    context.eventType = "google.storage.object.finalize";

    new HelloGcs().accept(event, context);

    String message = logHandler.getStoredLogRecords().get(0).getMessage();
    assertThat("File foo.txt uploaded.").isEqualTo(message);
  }

  @Test
  public void helloGcs_shouldDisregardOtherEvents() throws Exception {
    GcsEvent event = new GcsEvent();
    event.name = "baz.txt";

    MockContext context = new MockContext();
    context.eventType = "google.storage.object.metadataUpdate";

    new HelloGcs().accept(event, context);

    String message = logHandler.getStoredLogRecords().get(0).getMessage();
    assertThat("Unsupported event type: google.storage.object.metadataUpdate").isEqualTo(message);
  }
}
