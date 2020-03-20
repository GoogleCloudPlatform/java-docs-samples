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

package com.example.functions;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.github.seratch.jslack.app_backend.SlackSignature;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;

public class SlackSlashCommandTest {

  private BufferedWriter writerOut;
  private StringWriter responseOut;

  @Mock private HttpRequest request;
  @Mock private HttpResponse response;

  @Mock private SlackSignature.Verifier alwaysValidVerifier;

  @Before
  public void beforeTest() throws IOException {
    request = mock(HttpRequest.class);
    when(request.getReader()).thenReturn(new BufferedReader(new StringReader("")));

    response = mock(HttpResponse.class);

    responseOut = new StringWriter();

    writerOut = new BufferedWriter(responseOut);
    when(response.getWriter()).thenReturn(writerOut);

    alwaysValidVerifier = mock(SlackSignature.Verifier.class);
    when(alwaysValidVerifier.isValid(
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
          ArgumentMatchers.anyLong())
    ).thenReturn(true);

    // Construct valid header list
    HashMap<String, List<String>> validHeaders = new HashMap<String, List<String>>();
    String validSlackSignature = System.getenv("SLACK_TEST_SIGNATURE");
    String timestamp = "0"; // start of Unix epoch

    validHeaders.put("X-Slack-Signature", Arrays.asList(validSlackSignature));
    validHeaders.put("X-Slack-Request-Timestamp", Arrays.asList(timestamp));

    when(request.getHeaders()).thenReturn(validHeaders);
  }

  @Test
  public void onlyAcceptsPostRequestsTest() throws IOException, GeneralSecurityException {
    when(request.getMethod()).thenReturn("GET");
    new SlackSlashCommand().service(request, response);

    writerOut.flush();
    verify(response, times(1)).setStatusCode(HttpURLConnection.HTTP_BAD_METHOD);

    // DBG
    String envStr = String.join("/", System.getenv().keySet());
    Logger.getAnonymousLogger().info(envStr);
  }

  @Test
  public void requiresSlackAuthHeadersTest() throws IOException, GeneralSecurityException {
    StringReader requestReadable = new StringReader("{ \"text\": \"foo\" }\n");

    when(request.getMethod()).thenReturn("POST");
    when(request.getReader()).thenReturn(new BufferedReader(requestReadable));

    new SlackSlashCommand().service(request, response);

    // Do NOT look for HTTP_BAD_REQUEST here (that means the request WAS authorized)!
    verify(response, times(1)).setStatusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
  }

  @Test
  public void recognizesValidSlackTokenTest() throws IOException, GeneralSecurityException {
    StringReader requestReadable = new StringReader("{}");

    when(request.getReader()).thenReturn(new BufferedReader(requestReadable));
    when(request.getMethod()).thenReturn("POST");

    new SlackSlashCommand().service(request, response);

    verify(response, times(1)).setStatusCode(HttpURLConnection.HTTP_BAD_REQUEST);
  }

  @Test(expected = GoogleJsonResponseException.class)
  public void handlesSearchErrorTest() throws IOException, GeneralSecurityException {
    StringReader requestReadable = new StringReader("{ \"text\": \"foo\" }\n");

    when(request.getReader()).thenReturn(new BufferedReader(requestReadable));
    when(request.getMethod()).thenReturn("POST");

    SlackSlashCommand functionInstance = new SlackSlashCommand();
    Whitebox.setInternalState(functionInstance, "verifier", alwaysValidVerifier);
    Whitebox.setInternalState(SlackSlashCommand.class, "API_KEY", "gibberish");

    // Should throw a GoogleJsonResponseException (due to invalid API key)
    functionInstance.service(request, response);
  }

  @Test
  public void handlesEmptyKgResultsTest() throws IOException, GeneralSecurityException {
    StringReader requestReadable = new StringReader("{ \"text\": \"asdfjkl13579\" }\n");

    when(request.getReader()).thenReturn(new BufferedReader(requestReadable));
    when(request.getMethod()).thenReturn("POST");

    SlackSlashCommand functionInstance = new SlackSlashCommand();
    Whitebox.setInternalState(functionInstance, "verifier", alwaysValidVerifier);


    functionInstance.service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("No results match your query...");
  }

  @Test
  public void handlesPopulatedKgResultsTest() throws IOException, GeneralSecurityException {
    StringReader requestReadable = new StringReader("{ \"text\": \"lion\" }\n");

    when(request.getReader()).thenReturn(new BufferedReader(requestReadable));
    when(request.getMethod()).thenReturn("POST");

    SlackSlashCommand functionInstance = new SlackSlashCommand();
    Whitebox.setInternalState(functionInstance, "verifier", alwaysValidVerifier);


    functionInstance.service(request, response);

    writerOut.flush();
    assertThat(responseOut.toString()).contains("https://en.wikipedia.org/wiki/Lion");
  }
}
