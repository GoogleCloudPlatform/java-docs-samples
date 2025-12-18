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

package com.google.cloud.auth.samples.customcredentials.okta;

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
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

// [END auth_custom_credential_supplier_okta]

/**
 * This sample demonstrates how to use a custom subject token supplier to authenticate to Google
 * Cloud Storage, using Okta as the identity provider.
 */
public class CustomCredentialSupplierOktaWorkload {

  public static void main(String[] args) throws IOException {

    // Reads the custom-credentials-okta-secrets.json if running locally.
    loadConfigFromFile();

    // The audience for the workload identity federation.
    // Format: //iam.googleapis.com/projects/<project-number>/locations/global/
    //         workloadIdentityPools/<pool-id>/providers/<provider-id>
    String gcpWorkloadAudience = getConfiguration("GCP_WORKLOAD_AUDIENCE");

    // The bucket to fetch data from.
    String gcsBucketName = getConfiguration("GCS_BUCKET_NAME");

    // (Optional) The service account impersonation URL.
    String saImpersonationUrl = getConfiguration("GCP_SERVICE_ACCOUNT_IMPERSONATION_URL");

    // Okta Configuration
    String oktaDomain = getConfiguration("OKTA_DOMAIN");
    String oktaClientId = getConfiguration("OKTA_CLIENT_ID");
    String oktaClientSecret = getConfiguration("OKTA_CLIENT_SECRET");

    if (gcpWorkloadAudience == null
        || gcsBucketName == null
        || oktaDomain == null
        || oktaClientId == null
        || oktaClientSecret == null) {
      System.err.println(
          "Error: Missing required configuration. "
              + "Please provide it in a custom-credentials-okta-secrets.json file or as "
              + "environment variables: GCP_WORKLOAD_AUDIENCE, GCS_BUCKET_NAME, "
              + "OKTA_DOMAIN, OKTA_CLIENT_ID, OKTA_CLIENT_SECRET");
      return;
    }

    try {
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
    } catch (Exception e) {
      System.err.println("Authentication or Request failed: " + e.getMessage());
    }
  }

  /**
   * Helper method to retrieve configuration. It checks Environment variables first, then System
   * properties (populated by loadConfigFromFile).
   */
  static String getConfiguration(String key) {
    String value = System.getenv(key);
    if (value == null) {
      value = System.getProperty(key);
    }
    return value;
  }

  /**
   * If a local secrets file is present, load it into the System Properties. This is a
   * "just-in-time" configuration for local development. These variables are only set for the
   * current process.
   */
  static void loadConfigFromFile() {
    // By default, this expects the file to be in the project root.
    String secretsFilePath = "custom-credentials-okta-secrets.json";
    if (!Files.exists(Paths.get(secretsFilePath))) {
      return;
    }

    try (Reader reader = Files.newBufferedReader(Paths.get(secretsFilePath))) {
      Gson gson = new Gson();
      Type type = new TypeToken<Map<String, String>>() {}.getType();
      Map<String, String> secrets = gson.fromJson(reader, type);

      if (secrets == null) {
        return;
      }

      // Map JSON keys (snake_case) to System Properties (UPPER_UNDERSCORE)
      if (secrets.containsKey("gcp_workload_audience")) {
        System.setProperty("GCP_WORKLOAD_AUDIENCE", secrets.get("gcp_workload_audience"));
      }
      if (secrets.containsKey("gcs_bucket_name")) {
        System.setProperty("GCS_BUCKET_NAME", secrets.get("gcs_bucket_name"));
      }
      if (secrets.containsKey("gcp_service_account_impersonation_url")) {
        System.setProperty(
            "GCP_SERVICE_ACCOUNT_IMPERSONATION_URL",
            secrets.get("gcp_service_account_impersonation_url"));
      }
      if (secrets.containsKey("okta_domain")) {
        System.setProperty("OKTA_DOMAIN", secrets.get("okta_domain"));
      }
      if (secrets.containsKey("okta_client_id")) {
        System.setProperty("OKTA_CLIENT_ID", secrets.get("okta_client_id"));
      }
      if (secrets.containsKey("okta_client_secret")) {
        System.setProperty("OKTA_CLIENT_SECRET", secrets.get("okta_client_secret"));
      }

    } catch (IOException e) {
      System.err.println("Error reading secrets file: " + e.getMessage());
    } catch (JsonSyntaxException e) {
      System.err.println("Error: File is not valid JSON.");
    }
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

    OktaClientCredentialsSupplier oktaSupplier =
        new OktaClientCredentialsSupplier(oktaDomain, oktaClientId, oktaClientSecret);

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
      try (java.io.OutputStream out = conn.getOutputStream()) {
        // Scopes define the permissions the access token will have.
        // Update "gcp.test.read" to match your Okta configuration.
        String params = "grant_type=client_credentials&scope=gcp.test.read";
        out.write(params.getBytes(StandardCharsets.UTF_8));
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
