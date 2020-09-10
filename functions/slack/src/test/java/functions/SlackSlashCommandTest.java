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
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.slack.api.app_backend.SlackSignature;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SlackSlashCommandTest {

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  private static final Gson gson = new Gson();

  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  @Mock private SlackSignature.Verifier alwaysValidVerifier;

  @Before
  public void beforeTest() throws IOException {
    MockitoAnnotations.initMocks(this);

    when(request.getReader()).thenReturn(new BufferedReader(new StringReader("")));

    responseOut = new StringWriter();

    writerOut = new BufferedWriter(responseOut);
    when(response.getWriter()).thenReturn(writerOut);

    when(alwaysValidVerifier.isValid(any(), any(), any(), anyLong())).thenReturn(true);

    // Construct valid header list
    String validSlackSignature = System.getenv("SLACK_TEST_SIGNATURE");
    String timestamp = "0"; // start of Unix epoch

    Map<String, List<String>> validHeaders = Map.of(
        "X-Slack-Signature", List.of(validSlackSignature),
        "X-Slack-Request-Timestamp", List.of(timestamp));

    when(request.getHeaders()).thenReturn(validHeaders);
    when(request.getFirstHeader(any())).thenCallRealMethod();
  }

  @Test
  public void onlyAcceptsPostRequestsTest() throws IOException, GeneralSecurityException {
    when(request.getMethod()).thenReturn("GET");
    new SlackSlashCommand().service(request, response);

    writerOut.flush();
    verify(response, times(1)).setStatusCode(HttpURLConnection.HTTP_BAD_METHOD);
  }

  @Test
  public void requiresSlackAuthHeadersTest() throws IOException, GeneralSecurityException {
    String urlEncodedStr = "text=foo";
    StringReader requestReadable = new StringReader(urlEncodedStr);

    when(request.getMethod()).thenReturn("POST");
    when(request.getReader()).thenReturn(new BufferedReader(requestReadable));

    new SlackSlashCommand().service(request, response);

    // Do NOT look for HTTP_BAD_REQUEST here (that means the request WAS authorized)!
    verify(response, times(1)).setStatusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
  }

  @Test
  public void recognizesValidSlackTokenTest() throws IOException, GeneralSecurityException {
    StringReader requestReadable = new StringReader("");

    when(request.getReader()).thenReturn(new BufferedReader(requestReadable));
    when(request.getMethod()).thenReturn("POST");

    new SlackSlashCommand().service(request, response);

    verify(response, times(1)).setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
  }

  @Test
  public void handlesSearchErrorTest() throws IOException, GeneralSecurityException {
    String urlEncodedStr = "text=foo";
    StringReader requestReadable = new StringReader(urlEncodedStr);

    when(request.getReader()).thenReturn(new BufferedReader(requestReadable));
    when(request.getMethod()).thenReturn("POST");

    SlackSlashCommand functionInstance = new SlackSlashCommand(alwaysValidVerifier, "gibberish");

    // Should throw a GoogleJsonResponseException (due to invalid API key)
    assertThrows(
        GoogleJsonResponseException.class, () -> functionInstance.service(request, response));
  }

  @Test
  public void handlesEmptyKgResultsTest() throws IOException, GeneralSecurityException {
    String urlEncodedStr = "text=asdfjkl13579";
    StringReader requestReadable = new StringReader(urlEncodedStr);

    when(request.getReader()).thenReturn(new BufferedReader(requestReadable));
    when(request.getMethod()).thenReturn("POST");

    SlackSlashCommand functionInstance = new SlackSlashCommand(alwaysValidVerifier);

    functionInstance.service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("No results match your query...");
  }

  @Test
  public void handlesPopulatedKgResultsTest() throws IOException, GeneralSecurityException {
    String urlEncodedStr = "text=lion";
    StringReader requestReadable = new StringReader(urlEncodedStr);

    when(request.getReader()).thenReturn(new BufferedReader(requestReadable));
    when(request.getMethod()).thenReturn("POST");

    SlackSlashCommand functionInstance = new SlackSlashCommand(alwaysValidVerifier);

    functionInstance.service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("https://en.wikipedia.org/wiki/Lion");
  }

  @Test
  public void handlesMultipleUrlParamsTest() throws IOException, GeneralSecurityException {
    String urlEncodedStr = "unused=foo&text=lion";
    StringReader requestReadable = new StringReader(urlEncodedStr);

    when(request.getReader()).thenReturn(new BufferedReader(requestReadable));
    when(request.getMethod()).thenReturn("POST");

    SlackSlashCommand functionInstance = new SlackSlashCommand(alwaysValidVerifier);

    functionInstance.service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("https://en.wikipedia.org/wiki/Lion");
  }
}
