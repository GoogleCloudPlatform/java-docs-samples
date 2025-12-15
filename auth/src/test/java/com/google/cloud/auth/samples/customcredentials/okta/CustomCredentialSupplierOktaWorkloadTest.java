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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;

import com.google.cloud.storage.Bucket;
import org.junit.BeforeClass;
import org.junit.Test;

public class CustomCredentialSupplierOktaWorkloadTest {

  private static final String AUDIENCE_KEY = "GCP_WORKLOAD_AUDIENCE";
  private static final String BUCKET_KEY = "GCS_BUCKET_NAME";
  private static final String IMPERSONATION_KEY = "GCP_SERVICE_ACCOUNT_IMPERSONATION_URL";

  private static final String OKTA_DOMAIN_KEY = "OKTA_DOMAIN";
  private static final String OKTA_CLIENT_ID_KEY = "OKTA_CLIENT_ID";
  private static final String OKTA_CLIENT_SECRET_KEY = "OKTA_CLIENT_SECRET";

  @BeforeClass
  public static void setupConfiguration() {
    // 1. Call the loader from the main class.
    // This will load custom-credentials-okta-secrets.json into System Properties if the file
    // exists.
    CustomCredentialSupplierOktaWorkload.loadConfigFromFile();

    // 2. Validate requirements using the main class's getConfiguration helper.
    // This checks both Environment Variables and the System Properties we just loaded.
    requireConfig(AUDIENCE_KEY);
    requireConfig(BUCKET_KEY);
    requireConfig(OKTA_DOMAIN_KEY);
    requireConfig(OKTA_CLIENT_ID_KEY);
    requireConfig(OKTA_CLIENT_SECRET_KEY);
  }

  private static void requireConfig(String key) {
    String value = CustomCredentialSupplierOktaWorkload.getConfiguration(key);
    assumeTrue("Skipping test: " + key + " is missing.", value != null && !value.isEmpty());
  }

  /**
   * System Test: Verifies the full end-to-end authentication flow. This runs against the real
   * Google Cloud and Okta APIs.
   */
  @Test
  public void testAuthenticateWithOktaCredentials_system() throws Exception {
    // Retrieve values using the helper from the main class
    String audience = CustomCredentialSupplierOktaWorkload.getConfiguration(AUDIENCE_KEY);
    String bucketName = CustomCredentialSupplierOktaWorkload.getConfiguration(BUCKET_KEY);
    String impersonationUrl =
        CustomCredentialSupplierOktaWorkload.getConfiguration(IMPERSONATION_KEY);

    String oktaDomain = CustomCredentialSupplierOktaWorkload.getConfiguration(OKTA_DOMAIN_KEY);
    String oktaClientId = CustomCredentialSupplierOktaWorkload.getConfiguration(OKTA_CLIENT_ID_KEY);
    String oktaSecret =
        CustomCredentialSupplierOktaWorkload.getConfiguration(OKTA_CLIENT_SECRET_KEY);

    // Act: Run the authentication sample
    Bucket bucket =
        CustomCredentialSupplierOktaWorkload.authenticateWithOktaCredentials(
            audience, impersonationUrl, bucketName, oktaDomain, oktaClientId, oktaSecret);

    // Assert: Verify we got a valid bucket object back from the API
    assertThat(bucket).isNotNull();
    assertThat(bucket.getName()).isEqualTo(bucketName);

    // Verify we can actually access metadata (proving auth worked)
    assertThat(bucket.getLocation()).isNotNull();
  }
}
