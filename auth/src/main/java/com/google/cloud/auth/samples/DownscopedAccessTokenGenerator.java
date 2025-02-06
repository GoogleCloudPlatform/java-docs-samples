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

package com.google.cloud.auth.samples;

// [START auth_client_cab_token_broker]
import com.google.auth.credentialaccessboundary.ClientSideCredentialAccessBoundaryFactory;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.CredentialAccessBoundary;
import com.google.auth.oauth2.GoogleCredentials;
import dev.cel.common.CelValidationException;
import java.io.IOException;
import java.security.GeneralSecurityException;
// [END auth_client_cab_token_broker]

/**
 * Demonstrates how to use ClientSideCredentialAccessBoundaryFactory to generate downscoped tokens.
 */
public class DownscopedAccessTokenGenerator {

  /**
   * Simulates a token broker generating downscoped tokens for specific objects in a  bucket.
   *
   * @param bucketName The name of the Cloud Storage bucket.
   * @param objectPrefix Prefix of the object name for downscoped token access.
   * @return An AccessToken representing the downscoped token.
   * @throws IOException If an error occurs during token generation.
   */
  // [START auth_client_cab_token_broker]
  public static AccessToken getTokenFromBroker(String bucketName, String objectPrefix)
      throws IOException {
    // Retrieve the source credentials from ADC.
    GoogleCredentials sourceCredentials =
        GoogleCredentials.getApplicationDefault()
            .createScoped("https://www.googleapis.com/auth/cloud-platform");

    // Initialize the Credential Access Boundary rules.
    String availableResource = "//storage.googleapis.com/projects/_/buckets/" + bucketName;

    // Downscoped credentials will have readonly access to the resource.
    String availablePermission = "inRole:roles/storage.objectViewer";

    // Only objects starting with the specified prefix string in the object name will be allowed
    // read access.
    String expression =
        "resource.name.startsWith('projects/_/buckets/"
            + bucketName
            + "/objects/"
            + objectPrefix
            + "')";

    // Build the AvailabilityCondition.
    CredentialAccessBoundary.AccessBoundaryRule.AvailabilityCondition availabilityCondition =
        CredentialAccessBoundary.AccessBoundaryRule.AvailabilityCondition.newBuilder()
            .setExpression(expression)
            .build();

    // Define the single access boundary rule using the above properties.
    CredentialAccessBoundary.AccessBoundaryRule rule =
        CredentialAccessBoundary.AccessBoundaryRule.newBuilder()
            .setAvailableResource(availableResource)
            .addAvailablePermission(availablePermission)
            .setAvailabilityCondition(availabilityCondition)
            .build();

    // Define the Credential Access Boundary with all the relevant rules.
    CredentialAccessBoundary credentialAccessBoundary =
        CredentialAccessBoundary.newBuilder().addRule(rule).build();

    // Create an instance of ClientSideCredentialAccessBoundaryFactory.
    ClientSideCredentialAccessBoundaryFactory factory =
        ClientSideCredentialAccessBoundaryFactory.newBuilder()
            .setSourceCredential(sourceCredentials)
            .build();

    // Generate the token and pass it to the Token Consumer.
    try {
      return factory.generateToken(credentialAccessBoundary);
    } catch (GeneralSecurityException | CelValidationException e) {
      throw new IOException("Error generating downscoped token", e);
    }
  }
  // [END auth_client_cab_token_broker]
}
