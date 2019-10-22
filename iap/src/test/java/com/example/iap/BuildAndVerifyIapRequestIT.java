/*
 * Copyright 2017 Google Inc.
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

package com.example.iap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
//CHECKSTYLE OFF: AbbreviationAsWordInName
public class BuildAndVerifyIapRequestIT {
  //CHECKSTYLE ON: AbbreviationAsWordInName

  // Update these fields to reflect your IAP protected App Engine credentials
  private static Long IAP_PROJECT_NUMBER = 320431926067L;
  private static String IAP_PROJECT_ID = "gcp-devrel-iap-reflect";
  private static String IAP_PROTECTED_URL = "https://gcp-devrel-iap-reflect.appspot.com";
  private static String IAP_CLIENT_ID =
      "320431926067-ldm6839p8l2sei41nlsfc632l4d0v2u1.apps.googleusercontent.com";

  private HttpTransport httpTransport = new NetHttpTransport();
  private VerifyIapRequestHeader verifyIapRequestHeader = new VerifyIapRequestHeader();

  // Access an IAP protected url without signed jwt authorization header
  @Test
  public void accessIapProtectedResourceFailsWithoutJwtHeader() throws Exception {
    HttpRequest request =
        httpTransport.createRequestFactory().buildGetRequest(new GenericUrl(IAP_PROTECTED_URL));
    try {
      request.execute();
    } catch (HttpResponseException e) {
      assertEquals(e.getStatusCode(), HttpStatus.SC_UNAUTHORIZED);
    }
  }

  // Access an IAP protected url with a signed jwt authorization header, verify jwt token
  @Test
  public void testGenerateAndVerifyIapRequestIsSuccessful() throws Exception {
    HttpRequest request =
        httpTransport.createRequestFactory().buildGetRequest(new GenericUrl(IAP_PROTECTED_URL));
    HttpRequest iapRequest = BuildIapRequest.buildIapRequest(request, IAP_CLIENT_ID);
    HttpResponse response = iapRequest.execute();
    assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    String headerWithtoken = response.parseAsString();
    String[] split = headerWithtoken.split(":");
    assertNotNull(split);
    assertEquals(2, split.length);
    assertEquals("x-goog-authenticated-user-jwt", split[0].trim());

    String jwtToken = split[1].trim();
    HttpRequest verifyJwtRequest =
        httpTransport
            .createRequestFactory()
            .buildGetRequest(new GenericUrl(IAP_PROTECTED_URL))
            .setHeaders(new HttpHeaders().set("x-goog-iap-jwt-assertion", jwtToken));
    boolean verified =
        verifyIapRequestHeader.verifyJwtForAppEngine(
            verifyJwtRequest, IAP_PROJECT_NUMBER, IAP_PROJECT_ID);
    assertTrue(verified);
  }
}
