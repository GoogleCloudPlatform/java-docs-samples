package com.google.cloud.auth.samples;

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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;

import com.google.cloud.storage.Bucket;
import org.junit.BeforeClass;
import org.junit.Test;

public class CustomCredentialSupplierOktaWorkloadTest {

    private static final String AUDIENCE_ENV = "GCP_WORKLOAD_AUDIENCE";
    private static final String BUCKET_ENV = "GCS_BUCKET_NAME";
    private static final String IMPERSONATION_URL_ENV = "GCP_SERVICE_ACCOUNT_IMPERSONATION_URL";

    private static final String OKTA_DOMAIN_ENV = "OKTA_DOMAIN";
    private static final String OKTA_CLIENT_ID_ENV = "OKTA_CLIENT_ID";
    private static final String OKTA_CLIENT_SECRET_ENV = "OKTA_CLIENT_SECRET";

    @BeforeClass
    public static void checkRequirements() {
        // System tests require these variables to be set.
        // If they are missing, the test suite is skipped (standard behavior for Google Cloud samples).
        requireEnvVar(AUDIENCE_ENV);
        requireEnvVar(BUCKET_ENV);
        requireEnvVar(OKTA_DOMAIN_ENV);
        requireEnvVar(OKTA_CLIENT_ID_ENV);
        requireEnvVar(OKTA_CLIENT_SECRET_ENV);
    }

    private static void requireEnvVar(String varName) {
        assumeTrue(
                "Skipping test: " + varName + " is missing.",
                System.getenv(varName) != null && !System.getenv(varName).isEmpty());
    }

    /**
     * System Test: Verifies the full end-to-end authentication flow.
     * This runs against the real Google Cloud and Okta APIs.
     */
    @Test
    public void testAuthenticateWithOktaCredentials_system() throws Exception {
        String audience = System.getenv(AUDIENCE_ENV);
        String bucketName = System.getenv(BUCKET_ENV);
        String impersonationUrl = System.getenv(IMPERSONATION_URL_ENV);

        String oktaDomain = System.getenv(OKTA_DOMAIN_ENV);
        String oktaClientId = System.getenv(OKTA_CLIENT_ID_ENV);
        String oktaSecret = System.getenv(OKTA_CLIENT_SECRET_ENV);

        // Act: Run the authentication sample
        Bucket bucket =
                CustomCredentialSupplierOktaWorkload.authenticateWithOktaCredentials(
                        audience,
                        impersonationUrl,
                        bucketName,
                        oktaDomain,
                        oktaClientId,
                        oktaSecret);

        // Assert: Verify we got a valid bucket object back from the API
        assertThat(bucket).isNotNull();
        assertThat(bucket.getName()).isEqualTo(bucketName);

        // Verify we can actually access metadata (proving auth worked)
        assertThat(bucket.getLocation()).isNotNull();
    }
}