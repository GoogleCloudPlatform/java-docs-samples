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

import com.google.auth.credentialaccessboundary.ClientSideCredentialAccessBoundaryFactory;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.CredentialAccessBoundary;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.OAuth2CredentialsWithRefresh;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import dev.cel.common.CelValidationException;
import java.io.IOException;
import java.security.GeneralSecurityException;

/** Demonstrates how to use ClientSideCredentialAccessBoundaryFactory to generate downscoped tokens. */
public class ClientSideCredentialAccessBoundaryFactoryExample {

  /**
   * Tests the ClientSideCredentialAccessBoundaryFactory functionality.
   *
   * <p>This will generate a downscoped token with readonly access to the specified GCS bucket,
   * inject them into a storage instance and then test print the contents of the specified object.
   */
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // The Cloud Storage bucket name.
    String bucketName = "your-gcs-bucket-name";
    // The Cloud Storage object name that resides in the specified bucket.
    String objectName = "your-gcs-object-name";

    tokenConsumer(bucketName, objectName);
  }

  /** Simulates token broker generating downscoped tokens for specified bucket. */
  // [START auth_client_cab_token_broker]
  public static AccessToken getTokenFromBroker(String bucketName, String objectPrefix)
      throws IOException, GeneralSecurityException, CelValidationException {
    // Retrieve the source credentials from ADC.
    GoogleCredentials sourceCredentials =
        GoogleCredentials.getApplicationDefault()
            .createScoped("https://www.googleapis.com/auth/cloud-platform");

    // [START auth_client_cab_rules]
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
    // [END auth_client_cab_rules]

    // [START auth_client_cab_initialize_factory]
    // Create an instance of ClientSideCredentialAccessBoundaryFactory.
    ClientSideCredentialAccessBoundaryFactory factory =
        ClientSideCredentialAccessBoundaryFactory.newBuilder()
            .setSourceCredential(sourceCredentials)
            .build();
    // [END auth_client_cab_initialize_factory]

    // [START auth_client_cab_generate_downscoped_token]
    // Generate the token.
    // This will need to be passed to the Token Consumer.
    AccessToken accessToken = factory.generateToken(credentialAccessBoundary);
    // [END auth_client_cab_generate_downscoped_token]
    return accessToken;
  }
  // [END auth_client_cab_token_broker]

  /** Simulates token consumer readonly access to the specified object. */
  // [START auth_client_cab_token_consumer]
  public static void tokenConsumer(final String bucketName, final String objectName)
      throws IOException {
    // You can pass an `OAuth2RefreshHandler` to `OAuth2CredentialsWithRefresh` which will allow the
    // library to seamlessly handle downscoped token refreshes on expiration.
    OAuth2CredentialsWithRefresh.OAuth2RefreshHandler handler =
        new OAuth2CredentialsWithRefresh.OAuth2RefreshHandler() {
          @Override
          public AccessToken refreshAccessToken() throws IOException {
            // The common pattern of usage is to have a token broker pass the downscoped short-lived
            // access tokens to a token consumer via some secure authenticated channel.
            // For illustration purposes, we are generating the downscoped token locally.
            // We want to test the ability to limit access to objects with a certain prefix string
            // in the resource bucket. objectName.substring(0, 3) is the prefix here. This field is
            // not required if access to all bucket resources are allowed. If access to limited
            // resources in the bucket is needed, this mechanism can be used.
            try {
              return getTokenFromBroker(bucketName, objectName.substring(0, 3));
            } catch (GeneralSecurityException | CelValidationException e) {
              throw new RuntimeException(e);
            }
          }
        };

    // Downscoped token retrieved from token broker.
    AccessToken downscopedToken = handler.refreshAccessToken();

    // Create the OAuth2CredentialsWithRefresh from the downscoped token and pass a refresh handler
    // which will handle token expiration.
    // This will allow the consumer to seamlessly obtain new downscoped tokens on demand every time
    // token expires.
    OAuth2CredentialsWithRefresh credentials =
        OAuth2CredentialsWithRefresh.newBuilder()
            .setAccessToken(downscopedToken)
            .setRefreshHandler(handler)
            .build();

    // Use the credentials with the Cloud Storage SDK.
    StorageOptions options = StorageOptions.newBuilder().setCredentials(credentials).build();
    Storage storage = options.getService();

    // Call Cloud Storage APIs.
    Blob blob = storage.get(bucketName, objectName);
    String content = new String(blob.getContent());
    System.out.println(
        "Retrieved object, "
            + objectName
            + ", from bucket,"
            + bucketName
            + ", with content: "
            + content);
  }
  // [END auth_client_cab_token_consumer]
}
