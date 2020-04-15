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
import functions.eventpojos.MockContext;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

@RunWith(JUnit4.class)
public class KotlinHelloBackgroundTest {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  private static final Logger LOGGER = Logger.getLogger(
      KotlinHelloBackground.class.getName());
  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  @BeforeClass
  public static void beforeClass() {
    LOGGER.addHandler(LOG_HANDLER);
  }

  @Before
  public void beforeTest() throws IOException {
    Mockito.mockitoSession().initMocks(this);

    request = PowerMockito.mock(HttpRequest.class);
    response = PowerMockito.mock(HttpResponse.class);

    responseOut = new StringWriter();
    writerOut = new BufferedWriter(responseOut);
    PowerMockito.when(response.getWriter()).thenReturn(writerOut);
  }

  @After
  public void afterTest() {
    LOG_HANDLER.clear();
  }

  @Test
  public void functionsHelloworldBackgroundKotlin_shouldPrintName() throws Exception {
    when(request.getFirstQueryParameter("name")).thenReturn(Optional.of("John"));

    new KotlinHelloBackground().accept(request, new MockContext());

    String message = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Hello John!").isEqualTo(message);
  }

  @Test
  public void functionsHelloworldBackgroundKotlin_shouldPrintHelloWorld() throws Exception {
    new KotlinHelloBackground().accept(request, new MockContext());

    String message = LOG_HANDLER.getStoredLogRecords().get(0).getMessage();
    assertThat("Hello world!").isEqualTo(message);
  }
}
