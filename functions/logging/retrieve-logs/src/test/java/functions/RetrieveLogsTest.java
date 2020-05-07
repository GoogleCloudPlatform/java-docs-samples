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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class RetrieveLogsTest {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log records!)
  private static final Logger logger = Logger.getLogger(RetrieveLogs.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(LOG_HANDLER);
  }

  @Before
  public void beforeTest() throws IOException {
    MockitoAnnotations.initMocks(this);

    responseOut = new StringWriter();
    writerOut = new BufferedWriter(responseOut);
    when(response.getWriter()).thenReturn(writerOut);
  }

  @Test
  public void retrieveLogsTest() throws IOException {
    new RetrieveLogs().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Logs retrieved successfully.");
  }
}
