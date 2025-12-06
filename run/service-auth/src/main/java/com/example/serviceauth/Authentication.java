/*
 * Copyright 2025 Google LLC
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

package com.example.serviceauth;

// [START cloudrun_service_to_service_receive]

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Arrays;
import java.util.Collection;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Authentication {
  @RestController
  class AuthenticationController {

    private final AuthenticationService authService = new AuthenticationService();

    @GetMapping("/")
    public ResponseEntity<String> getEmailFromAuthHeader(
        @RequestHeader(value = "Authorization", required = false) String authHeader) {
      String responseBody;
      if (authHeader == null) {
        responseBody = "Error verifying ID token: missing Authorization header";
        return new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED);
      }

      String email = authService.parseAuthHeader(authHeader);
      if (email == null) {
        responseBody = "Unauthorized request. Please supply a valid bearer token.";
        HttpHeaders headers = new HttpHeaders();
        headers.add("WWW-Authenticate", "Bearer");
        return new ResponseEntity<>(responseBody, headers, HttpStatus.UNAUTHORIZED);
      }

      responseBody = "Hello, " + email;
      return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
  }

  public class AuthenticationService {
    /*
     * Parse the authorization header, validate and decode the Bearer token.
     *
     * Args:
     *    authHeader: String of HTTP header with a Bearer token.
     *
     * Returns:
     *    A string containing the email from the token.
     *    null if the token is invalid or the email can't be retrieved.
     */
    public String parseAuthHeader(String authHeader) {
      // Split the auth type and value from the header.
      String[] authHeaderStrings = authHeader.split(" ");
      if (authHeaderStrings.length != 2) {
        System.out.println("Malformed Authorization header");
        return null;
      }
      String authType = authHeaderStrings[0];
      String tokenValue = authHeaderStrings[1];
      // Validate and decode the ID token in the header.
      if (!"bearer".equals(authType.toLowerCase())) {
        System.out.println("Unhandled header format: " + authType);
        return null;
      }

      // Get the service URL from the environment variable
      // set at the time of deployment.
      String serviceUrl = System.getenv("SERVICE_URL");
      // Define the expected audience as the Service Base URL.
      Collection<String> audience = Arrays.asList(serviceUrl);

      try {
        // Find more information about the verification process in:
        // https://developers.google.com/identity/sign-in/web/backend-auth#java
        // https://cloud.google.com/java/docs/reference/google-api-client/latest/com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
        GoogleIdTokenVerifier verifier =
            new GoogleIdTokenVerifier.Builder(new ApacheHttpTransport(), new GsonFactory())
                .setAudience(audience)
                .build();
        GoogleIdToken googleIdToken = verifier.verify(tokenValue);

        // More info about the structure for the decoded ID Token here:
        // https://cloud.google.com/docs/authentication/token-types#id
        // https://cloud.google.com/java/docs/reference/google-api-client/latest/com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
        // https://cloud.google.com/java/docs/reference/google-api-client/latest/com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload
        GoogleIdToken.Payload payload = googleIdToken.getPayload();
        if (!payload.getEmailVerified()) {
          System.out.println("Invalid token. Email wasn't verified.");
          return null;
        }
        System.out.println("Email verified: " + payload.getEmail());
        return payload.getEmail();

      } catch (Exception exception) {
        System.out.println("Ivalid token: " + exception);
      }
      return null;
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(Authentication.class, args);
  }

  // [END cloudrun_service_to_service_receive]
}
