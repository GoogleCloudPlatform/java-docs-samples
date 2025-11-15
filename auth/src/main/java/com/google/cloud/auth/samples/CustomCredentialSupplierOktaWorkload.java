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

// [START auth_custom_credential_supplier_okta]
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.oauth2.ExternalAccountSupplierContext;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdentityPoolCredentials;
import com.google.auth.oauth2.IdentityPoolSubjectTokenSupplier;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

// [END auth_custom_credential_supplier_okta]

/**
 * This sample demonstrates how to use a custom subject token supplier to authenticate to Google
 * Cloud Storage, using Okta as the identity provider.
 */
public class CustomCredentialSupplierOktaWorkload {

  public static void main(String[] args) throws IOException {
    // The audience for the workload identity federation.
    // Format: //iam.googleapis.com/projects/<project-number>/locations/global/
    //         workloadIdentityPools/<pool-id>/providers/<provider-id>
    String gcpWorkloadAudience = System.getenv("GCP_WORKLOAD_AUDIENCE");

    // The bucket to fetch data from.
    String gcsBucketName = System.getenv("GCS_BUCKET_NAME");

    // (Optional) The service account impersonation URL.
    String saImpersonationUrl = System.getenv("GCP_SERVICE_ACCOUNT_IMPERSONATION_URL");

    // Okta Configuration
    String oktaDomain = System.getenv("OKTA_DOMAIN");
    String oktaClientId = System.getenv("OKTA_CLIENT_ID");
    String oktaClientSecret = System.getenv("OKTA_CLIENT_SECRET");

    if (gcpWorkloadAudience == null
        || gcsBucketName == null
        || oktaDomain == null
        || oktaClientId == null
        || oktaClientSecret == null) {
      System.err.println(
          "Error: Missing required environment variables. "
              + "Required: GCP_WORKLOAD_AUDIENCE, GCS_BUCKET_NAME, "
              + "OKTA_DOMAIN, OKTA_CLIENT_ID, OKTA_CLIENT_SECRET");
      return;
    }

    System.out.println("Getting metadata for bucket: " + gcsBucketName + "...");
    Bucket bucket =
        authenticateWithOktaCredentials(
            gcpWorkloadAudience,
            saImpersonationUrl,
            gcsBucketName,
            oktaDomain,
            oktaClientId,
            oktaClientSecret);

    System.out.println(" --- SUCCESS! ---");
    System.out.printf("Bucket Name: %s%n", bucket.getName());
    System.out.printf("Bucket Location: %s%n", bucket.getLocation());
  }

  /**
   * Authenticates using a custom Okta credential supplier and retrieves bucket metadata.
   *
   * @param gcpWorkloadAudience The WIF provider audience.
   * @param saImpersonationUrl Optional service account impersonation URL.
   * @param gcsBucketName The GCS bucket name.
   * @param oktaDomain The Okta organization domain.
   * @param oktaClientId The Okta application Client ID.
   * @param oktaClientSecret The Okta application Client Secret.
   * @return The Bucket object containing metadata.
   * @throws IOException If authentication or the API request fails.
   */
  // [START auth_custom_credential_supplier_okta]
  public static Bucket authenticateWithOktaCredentials(
      String gcpWorkloadAudience,
      String saImpersonationUrl,
      String gcsBucketName,
      String oktaDomain,
      String oktaClientId,
      String oktaClientSecret)
      throws IOException {

    // 1. Instantiate our custom supplier with Okta credentials.
    OktaClientCredentialsSupplier oktaSupplier =
        new OktaClientCredentialsSupplier(oktaDomain, oktaClientId, oktaClientSecret);

    // 2. Instantiate an IdentityPoolCredentials with the required configuration.
    IdentityPoolCredentials.Builder credentialsBuilder =
        IdentityPoolCredentials.newBuilder()
            .setAudience(gcpWorkloadAudience)
            // This token type indicates that the subject token is a JSON Web Token (JWT).
            // This is required for Workload Identity Federation with an OIDC provider like Okta.
            .setSubjectTokenType("urn:ietf:params:oauth:token-type:jwt")
            .setTokenUrl("https://sts.googleapis.com/v1/token")
            .setSubjectTokenSupplier(oktaSupplier);

    if (saImpersonationUrl != null) {
      credentialsBuilder.setServiceAccountImpersonationUrl(saImpersonationUrl);
    }

    GoogleCredentials credentials = credentialsBuilder.build();

    // 3. Use the credentials to make an authenticated request.
    Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

    return storage.get(gcsBucketName);
  }

  /**
   * A custom SubjectTokenSupplier that authenticates with Okta using the Client Credentials grant
   * flow.
   */
  private static class OktaClientCredentialsSupplier implements IdentityPoolSubjectTokenSupplier {

    private static final long TOKEN_REFRESH_BUFFER_SECONDS = 60;

    private final String oktaTokenUrl;
    private final String clientId;
    private final String clientSecret;
    private String accessToken;
    private Instant expiryTime;

    public OktaClientCredentialsSupplier(String domain, String clientId, String clientSecret) {
      // Ensure domain doesn't have a trailing slash for cleaner URL construction
      String cleanedDomain =
          domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
      this.oktaTokenUrl = cleanedDomain + "/oauth2/default/v1/token";
      this.clientId = clientId;
      this.clientSecret = clientSecret;
    }

    /**
     * Main method called by the auth library. It will fetch a new token if one is not already
     * cached.
     */
    @Override
    public String getSubjectToken(ExternalAccountSupplierContext context) throws IOException {
      // Check if the current token is still valid (with a 60-second buffer).
      boolean isTokenValid =
          this.accessToken != null
              && this.expiryTime != null
              && Instant.now().isBefore(this.expiryTime.minusSeconds(TOKEN_REFRESH_BUFFER_SECONDS));

      if (isTokenValid) {
        return this.accessToken;
      }

      fetchOktaAccessToken();
      return this.accessToken;
    }

    /**
     * Performs the Client Credentials grant flow by making a POST request to Okta's token endpoint.
     */
    private void fetchOktaAccessToken() throws IOException {
      URL url = new URL(this.oktaTokenUrl);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      conn.setRequestProperty("Accept", "application/json");

      // The client_id and client_secret are sent in a Basic Auth header.
      String auth = this.clientId + ":" + this.clientSecret;
      String encodedAuth =
          Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
      conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

      conn.setDoOutput(true);
      try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
        // Scopes define the permissions the access token will have.
        // Update "gcp.test.read" to match your Okta configuration.
        String params = "grant_type=client_credentials&scope=gcp.test.read";
        out.writeBytes(params);
        out.flush();
      }

      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        try (BufferedReader in =
            new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {

          GenericJson jsonObject =
              GsonFactory.getDefaultInstance().createJsonParser(in).parse(GenericJson.class);

          if (jsonObject.containsKey("access_token") && jsonObject.containsKey("expires_in")) {
            this.accessToken = (String) jsonObject.get("access_token");
            Number expiresInNumber = (Number) jsonObject.get("expires_in");
            this.expiryTime = Instant.now().plusSeconds(expiresInNumber.longValue());
          } else {
            throw new IOException("Access token or expires_in not found in Okta response.");
          }
        }
      } else {
        throw new IOException("Failed to authenticate with Okta. Response code: " + responseCode);
      }
    }
  }
  // [END auth_custom_credential_supplier_okta]
}
