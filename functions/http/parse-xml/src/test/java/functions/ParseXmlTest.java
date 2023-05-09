/*
 * Copyright 2022 Google LLC
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class)
public class ParseXmlTest {
  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  @Before
  public void beforeTest() throws IOException {
    MockitoAnnotations.openMocks(this);

    responseOut = new StringWriter();
    writerOut = new BufferedWriter(responseOut);
    when(response.getWriter()).thenReturn(writerOut);
  }

  @Test
  public void parseXmlTest_handlesXml() throws IOException, ParserConfigurationException {
    // Send a request with XML data
    String requestContent = "<?xml version=\"1.0\"?>\n<name>John</name>\n";
    BufferedReader bodyReader = new BufferedReader(new StringReader(requestContent));

    when(request.getInputStream()).thenReturn(
        new ByteArrayInputStream(requestContent.getBytes(StandardCharsets.UTF_8)));

    new ParseXml().service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("Root element: name");
  }


  @Test
  public void parseXmlTest_handlesNonXml() throws IOException, ParserConfigurationException {
    // Send a request with plain text
    String requestContent = "I am not XML!";
    BufferedReader bodyReader = new BufferedReader(new StringReader(requestContent));

    when(request.getInputStream()).thenReturn(
        new ByteArrayInputStream(requestContent.getBytes(StandardCharsets.UTF_8)));

    new ParseXml().service(request, response);

    verify(response, times(1)).setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
  }
}
