/**
 * Copyright 2017 Google Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.iap;

import static com.example.iap.BuildIapRequest.buildIAPRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import io.jsonwebtoken.Jwt;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BuildAndVerifyIapRequestIT {

  private String iapProtectedUrl = System.getenv("IAP_PROTECTED_URL");
  private String iapClientId = System.getenv("IAP_CLIENT_ID");
  private Long projectNumber = Long.parseLong(System.getenv("IAP_PROJECT_NUMBER"));
  private String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
  private HttpTransport httpTransport = new NetHttpTransport();
  private VerifyIapRequestHeader verifyIapRequestHeader = new VerifyIapRequestHeader();

  @Before
  public void setUp() {
    assertNotNull(iapProtectedUrl);
    assertNotNull(iapClientId);
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
    HttpRequest iapRequest = buildIAPRequest(request, iapClientId);
    HttpResponse response = iapRequest.execute();
    assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    String headerWithtoken = response.parseAsString();
    String[] split = headerWithtoken.split(":");
    assertNotNull(split);
    assertEquals(split.length, 2);
    assertEquals(split[0].trim(), "x-goog-iap-jwt-assertion");

    String jwtToken = split[1].trim();
    HttpRequest verifyJwtRequest = httpTransport
        .createRequestFactory()
        .buildGetRequest(new GenericUrl(iapProtectedUrl)).setHeaders(
        new HttpHeaders().set("x-goog-iap-jwt-assertion", jwtToken));
    Jwt decodedJWT = verifyIapRequestHeader.verifyJWTTokenForAppEngine(
        verifyJwtRequest, projectNumber, projectId);
    assertNotNull(decodedJWT);
  }
}
