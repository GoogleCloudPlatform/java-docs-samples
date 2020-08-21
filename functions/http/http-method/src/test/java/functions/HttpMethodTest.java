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
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class HttpMethodTest {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  @Before
  public void beforeTest() throws IOException {
    MockitoAnnotations.initMocks(this);

    responseOut = new StringWriter();
    writerOut = new BufferedWriter(responseOut);
    when(response.getWriter()).thenReturn(writerOut);
  }

  @Test
  public void functionsHttpMethod_shouldAcceptGet() throws IOException {
    when(request.getMethod()).thenReturn("GET");

    new HttpMethod().service(request, response);

    writerOut.flush();
    verify(response, times(1)).setStatusCode(HttpURLConnection.HTTP_OK);
    assertThat(responseOut.toString()).isEqualTo("Hello world!");
  }

  @Test
  public void functionsHttpMethod_shouldForbidPut() throws IOException {
    when(request.getMethod()).thenReturn("PUT");

    new HttpMethod().service(request, response);

    writerOut.flush();
    verify(response, times(1)).setStatusCode(HttpURLConnection.HTTP_FORBIDDEN);
    assertThat(responseOut.toString()).isEqualTo("Forbidden!");
  }

  @Test
  public void functionsHttpMethod_shouldErrorOnPost() throws IOException {
    when(request.getMethod()).thenReturn("POST");

    new HttpMethod().service(request, response);

    writerOut.flush();
    verify(response, times(1)).setStatusCode(HttpURLConnection.HTTP_BAD_METHOD);
    assertThat(responseOut.toString()).isEqualTo("Something blew up!");
  }
}
