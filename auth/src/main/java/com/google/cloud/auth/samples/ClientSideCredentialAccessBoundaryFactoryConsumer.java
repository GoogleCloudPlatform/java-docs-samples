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

// [START auth_client_cab_consumer]
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.OAuth2CredentialsWithRefresh;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
// [END auth_client_cab_consumer]


/**
 * Demonstrates retrieving a Cloud Storage blob using a downscoped. This example showcases the
 * consumer side of the downscoping process. It retrieves a blob's content using credentials that
 * have limited access based on a pre-defined Credential Access Boundary.
 */
public class ClientSideCredentialAccessBoundaryFactoryConsumer {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // The Cloud Storage bucket name.
    String bucketName = "your-gcs-bucket-name";
    // The Cloud Storage object name that resides in the specified bucket.
    String objectName = "your-gcs-object-name";

    retrieveBlobWithDownscopedToken(bucketName, objectName);
  }

  /**
   * Simulates token consumer readonly access to the specified object.
   *
   * @param bucketName The name of the Cloud Storage bucket containing the blob.
   * @param objectName The name of the Cloud Storage object (blob).
   * @return The content of the blob as a String, or {@code null} if the blob does not exist.
   * @throws IOException If an error occurs during communication with Cloud Storage or token
   *     retrieval.  This can include issues with authentication, authorization, or network
   *     connectivity.
   */
  // [START auth_client_cab_consumer]
  public static String retrieveBlobWithDownscopedToken(
      final String bucketName, final String objectName) throws IOException {
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
            return ClientSideCredentialAccessBoundaryFactoryExample
                .getTokenFromBroker(bucketName, objectName);
          }
        };

    AccessToken downscopedToken = handler.refreshAccessToken();

    OAuth2CredentialsWithRefresh credentials =
        OAuth2CredentialsWithRefresh.newBuilder()
            .setAccessToken(downscopedToken)
            .setRefreshHandler(handler)
            .build();

    StorageOptions options = StorageOptions.newBuilder().setCredentials(credentials).build();
    Storage storage = options.getService();

    Blob blob = storage.get(bucketName, objectName);
    if (blob == null) {
      return null;
    }
    return new String(blob.getContent());
  }
  // [END auth_client_cab_consumer]
}
