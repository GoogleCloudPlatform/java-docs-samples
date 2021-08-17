/*
 * Copyright 2021 Google LLC
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

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.CredentialAccessBoundary;
import com.google.auth.oauth2.DownscopedCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.OAuth2CredentialsWithRefresh;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;

/** Demonstrates how to use Downscoping with Credential Access Boundaries. */
public class DownscopingExample {

  /**
   * Tests the downscoping functionality.
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

    System.out.println("Testing token consumer read access...");
    String content = tokenConsumer(bucketName, objectName);
    System.out.println(content);
    System.out.println("done");
  }

  /** Simulates token broker generating downscoped tokens for specified bucket. */
  // [START auth_downscoping_token_broker]
  public static AccessToken getTokenFromBroker(String bucketName, String objectPrefix)
      throws IOException {
    // Retrieve the source credentials from ADC.
    GoogleCredentials sourceCredentials =
        GoogleCredentials.getApplicationDefault()
            .createScoped("https://www.googleapis.com/auth/cloud-platform");

    // [START auth_downscoping_rules]
    // Initialize the Credential Access Boundary rules.
    String availableResource =
        String.format("//storage.googleapis.com/projects/_/buckets/%s", bucketName);

    // Downscoped credentials will have readonly access to the resource.
    String availablePermission = "inRole:roles/storage.objectViewer";

    // Only objects starting with the specified prefix string in the object name will be allowed
    // read access.
    String expression =
        String.format(
            "resource.name.startsWith('projects/_/buckets/%s/objects/%s')",
            bucketName, objectPrefix);

    // Define the single access boundary rule using the above properties.
    CredentialAccessBoundary.AccessBoundaryRule rule =
        CredentialAccessBoundary.AccessBoundaryRule.newBuilder()
            .setAvailableResource(availableResource)
            .addAvailablePermission(availablePermission)
            .setAvailabilityCondition(
                CredentialAccessBoundary.AccessBoundaryRule.AvailabilityCondition.newBuilder()
                    .setExpression(expression)
                    .build())
            .build();

    // Define the Credential Access Boundary with all the relevant rules.
    CredentialAccessBoundary credentialAccessBoundary =
        CredentialAccessBoundary.newBuilder().addRule(rule).build();
    // [END auth_downscoping_rules]

    // [START auth_downscoping_initialize_downscoped_cred]
    // Create the downscoped credentials.
    DownscopedCredentials downscopedCredentials =
        DownscopedCredentials.newBuilder()
            .setSourceCredential(sourceCredentials)
            .setCredentialAccessBoundary(credentialAccessBoundary)
            .build();

    // Retrieve the token.
    // This will need to be passed to the Token Consumer.
    AccessToken accessToken = downscopedCredentials.refreshAccessToken();
    // [END auth_downscoping_initialize_downscoped_cred]
    return accessToken;
  }
  // [END auth_downscoping_token_broker]

  /** Simulates token consumer readonly access to the specified object. */
  // [START auth_downscoping_token_consumer]
  public static String tokenConsumer(final String bucketName, final String objectName)
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
            return getTokenFromBroker(bucketName, objectName.substring(0, 3));
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
    return new String(blob.getContent());
  }
  // [END auth_downscoping_token_consumer]
}
