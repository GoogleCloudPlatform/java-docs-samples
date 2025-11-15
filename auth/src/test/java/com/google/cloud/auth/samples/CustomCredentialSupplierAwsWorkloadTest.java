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

public class CustomCredentialSupplierAwsWorkloadTest {

    private static final String AUDIENCE_ENV = "GCP_WORKLOAD_AUDIENCE";
    private static final String BUCKET_ENV = "GCS_BUCKET_NAME";
    private static final String IMPERSONATION_URL_ENV = "GCP_SERVICE_ACCOUNT_IMPERSONATION_URL";

    // AWS Credentials required for the AWS SDK DefaultCredentialsProvider to work
    private static final String AWS_REGION_ENV = "AWS_REGION";
    private static final String AWS_KEY_ENV = "AWS_ACCESS_KEY_ID";
    private static final String AWS_SECRET_KEY_ENV = "AWS_SECRET_ACCESS_KEY";

    @BeforeClass
    public static void checkRequirements() {
        // Skip the test if required environment variables are missing
        requireEnvVar(AUDIENCE_ENV);
        requireEnvVar(BUCKET_ENV);

        // Verify AWS specific environment variables
        requireEnvVar(AWS_REGION_ENV);
        requireEnvVar(AWS_KEY_ENV);
        requireEnvVar(AWS_SECRET_KEY_ENV);
    }

    private static void requireEnvVar(String varName) {
        assumeTrue(
                "Skipping test: " + varName + " is missing.",
                System.getenv(varName) != null && !System.getenv(varName).isEmpty());
    }

    @Test
    public void testAuthenticateWithAwsCredentials_system() throws Exception {
        String audience = System.getenv(AUDIENCE_ENV);
        String bucketName = System.getenv(BUCKET_ENV);
        String impersonationUrl = System.getenv(IMPERSONATION_URL_ENV);

        // Act: Run the authentication sample
        Bucket bucket =
                CustomCredentialSupplierAwsWorkload.authenticateWithAwsCredentials(
                        audience, impersonationUrl, bucketName);

        // Assert: Verify we got a valid bucket object back from the API
        assertThat(bucket).isNotNull();
        assertThat(bucket.getName()).isEqualTo(bucketName);

        // Verify we can actually access metadata (proving auth worked)
        assertThat(bucket.getLocation()).isNotNull();
    }
}