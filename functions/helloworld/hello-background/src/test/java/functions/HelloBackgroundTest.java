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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.common.testing.TestLogHandler;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;

@PrepareForTest(HelloBackground.class)
public class HelloBackgroundTest {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  // Must be declared at class-level, or LoggingHandler won't detect log records!
  private static final Logger LOGGER = Logger.getLogger(HelloBackground.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  @Before
  public void beforeTest() throws Exception {
    LOGGER.addHandler(LOG_HANDLER);

    // Use a new mock for each test
    request = mock(HttpRequest.class);
    response = mock(HttpResponse.class);

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
  public void helloBackground_printsName() throws Exception {
    when(request.getFirstQueryParameter("name")).thenReturn(Optional.of("John"));

    new HelloBackground().accept(request, null);

    String message = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Hello John!").isEqualTo(message);
  }

  @Test
  public void helloBackground_printsHelloWorld() throws Exception {
    new HelloBackground().accept(request, null);

    String message = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Hello world!").isEqualTo(message);
  }
}
