/**
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

import static com.example.iap.BuildIapRequest.buildIAPRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BuildAndVerifyIapRequestIT {

  private String iapProtectedUrl = System.getenv("IAP_PROTECTED_URL");
  private HttpTransport httpTransport = new NetHttpTransport();
  private VerifyIapRequestHeader verifyIapRequestHeader = new VerifyIapRequestHeader();

  @Before
  public void setUp() {
    assertNotNull(iapProtectedUrl);
  }

  // Access an IAP protected url without signed jwt authorization header
  @Test
  public void accessIapProtectedResourceFailsWithoutJwtHeader() throws Exception {
    HttpRequest request =
        httpTransport.createRequestFactory().buildGetRequest(new GenericUrl(iapProtectedUrl));
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
        httpTransport.createRequestFactory().buildGetRequest(new GenericUrl(iapProtectedUrl));
    HttpRequest iapRequest = buildIAPRequest(request);
    HttpResponse response = iapRequest.execute();
    assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    String headerWithtoken = response.parseAsString();
    String[] split = headerWithtoken.split(":");
    assertNotNull(split);
    assertEquals(split.length, 2);
    assertEquals(split[0].trim(), "x-goog-authenticated-user-jwt");
    DecodedJWT decodedJWT = verifyIapRequestHeader.verifyJWTToken(split[1].trim(), iapProtectedUrl);
    assertNotNull(decodedJWT);
  }
}
