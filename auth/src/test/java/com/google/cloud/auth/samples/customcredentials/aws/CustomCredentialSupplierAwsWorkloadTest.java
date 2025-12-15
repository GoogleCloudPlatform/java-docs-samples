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

package com.google.cloud.auth.samples.customcredentials.aws;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;

import com.google.cloud.storage.Bucket;
import org.junit.BeforeClass;
import org.junit.Test;

public class CustomCredentialSupplierAwsWorkloadTest {

  private static final String AUDIENCE_KEY = "GCP_WORKLOAD_AUDIENCE";
  private static final String BUCKET_KEY = "GCS_BUCKET_NAME";
  private static final String IMPERSONATION_KEY = "GCP_SERVICE_ACCOUNT_IMPERSONATION_URL";

  @BeforeClass
  public static void setupConfiguration() {
    // This will load secrets.json into System Properties if the file exists.
    CustomCredentialSupplierAwsWorkload.loadConfigFromFile();

    String audience = CustomCredentialSupplierAwsWorkload.getConfiguration(AUDIENCE_KEY);
    String bucket = CustomCredentialSupplierAwsWorkload.getConfiguration(BUCKET_KEY);

    // Check for AWS specific requirements (needed by the SDK)
    String awsKey = System.getProperty("aws.accessKeyId"); // Set by loadConfigFromFile
    if (awsKey == null) {
      awsKey = System.getenv("AWS_ACCESS_KEY_ID");
    }

    // Skip the test if configuration is missing (mirrors pytest.skip).
    assumeTrue("Skipping test: " + AUDIENCE_KEY + " is missing.", audience != null);
    assumeTrue("Skipping test: " + BUCKET_KEY + " is missing.", bucket != null);
    assumeTrue("Skipping test: AWS Credentials not found.", awsKey != null);
  }

  @Test
  public void testAuthenticateWithAwsCredentials_system() throws Exception {
    // Retrieve values using the helper
    String audience = CustomCredentialSupplierAwsWorkload.getConfiguration(AUDIENCE_KEY);
    String bucketName = CustomCredentialSupplierAwsWorkload.getConfiguration(BUCKET_KEY);
    String impersonationUrl =
        CustomCredentialSupplierAwsWorkload.getConfiguration(IMPERSONATION_KEY);

    // Act: Run the authentication sample
    Bucket bucket =
        CustomCredentialSupplierAwsWorkload.authenticateWithAwsCredentials(
            audience, impersonationUrl, bucketName);

    // Verify we got a valid bucket object back from the API
    assertThat(bucket).isNotNull();
    assertThat(bucket.getName()).isEqualTo(bucketName);

    // Verify we can actually access metadata (proving auth worked)
    assertThat(bucket.getLocation()).isNotNull();
  }
}
