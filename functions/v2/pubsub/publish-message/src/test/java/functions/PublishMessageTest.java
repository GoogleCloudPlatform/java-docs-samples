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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.common.testing.TestLogHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class PublishMessageTest {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  private static final String FUNCTIONS_TOPIC = System.getenv("FUNCTIONS_TOPIC");

  private static final Logger logger = Logger.getLogger(PublishMessage.class.getName());
  private static final TestLogHandler logHandler = new TestLogHandler();

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(logHandler);
  }

  @Before
  public void beforeTest() throws IOException {
    MockitoAnnotations.initMocks(this);

    BufferedReader reader = new BufferedReader(new StringReader("{}"));
    when(request.getReader()).thenReturn(reader);

    responseOut = new StringWriter();
    writerOut = new BufferedWriter(responseOut);
    when(response.getWriter()).thenReturn(writerOut);

    logHandler.clear();
  }

  @Test
  public void functionsPubsubPublish_shouldFailWithoutParameters() throws IOException {
    new PublishMessage().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).isEqualTo(
        "Missing 'topic' and/or 'message' parameter(s).");
  }

  @Test
  public void functionsPubsubPublish_shouldPublishMessage() throws Exception {
    when(request.getFirstQueryParameter("topic")).thenReturn(Optional.of(FUNCTIONS_TOPIC));
    when(request.getFirstQueryParameter("message")).thenReturn(Optional.of("hello"));

    new PublishMessage().service(request, response);

    writerOut.flush();
    assertThat(logHandler.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Publishing message to topic: " + FUNCTIONS_TOPIC);
    assertThat(responseOut.toString()).isEqualTo("Message published.");
  }
}
