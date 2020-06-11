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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpRequest.HttpPart;
import com.google.cloud.functions.HttpResponse;
import com.google.common.testing.TestLogHandler;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class HttpFormDataTest {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  private static final Logger logger = Logger.getLogger(HttpFormData.class.getName());
  private static final TestLogHandler logHandler = new TestLogHandler();

  @BeforeClass
  public static void setUp() {
    logger.addHandler(logHandler);
  }

  @Before
  public void beforeTest() throws IOException {
    MockitoAnnotations.initMocks(this);

    responseOut = new StringWriter();
    writerOut = new BufferedWriter(responseOut);
    when(response.getWriter()).thenReturn(writerOut);

    logHandler.clear();
  }

  @Test
  public void functionsHttpMethod_shouldErrorOnGet() throws IOException {
    when(request.getMethod()).thenReturn("GET");

    new HttpFormData().service(request, response);

    writerOut.flush();
    verify(response, times(1)).setStatusCode(HttpURLConnection.HTTP_BAD_METHOD);
  }

  @Test
  public void functionsHttpFormData_shouldSaveFiles() throws IOException {
    when(request.getMethod()).thenReturn("POST");

    InputStream stream = new ByteArrayInputStream("foo text%n".getBytes(StandardCharsets.UTF_8));

    MockHttpPart mockHttpPart = new MockHttpPart();
    mockHttpPart.setFileName("foo.txt");
    mockHttpPart.setInputStream(stream);

    Map<String, HttpPart> httpParts = Map.of("mock", mockHttpPart);
    when(request.getParts()).thenReturn(httpParts);

    new HttpFormData().service(request, response);

    assertThat(logHandler.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Processed file: foo.txt");
  }

  @Test
  public void functionsHttpFormData_shouldProcessFields() throws IOException {
    when(request.getMethod()).thenReturn("POST");
    when(request.getParts()).thenReturn(Map.of());

    Map<String, List<String>> queryParams = Map.of("foo", List.of("bar"));

    when(request.getQueryParameters()).thenReturn(queryParams);
    when(request.getFirstQueryParameter("foo")).thenReturn(Optional.of("bar"));

    new HttpFormData().service(request, response);

    assertThat(logHandler.getStoredLogRecords().get(0).getMessage()).isEqualTo(
        "Processed field: foo (value: bar)");
  }
}
