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

// [START auth_custom_credential_supplier_aws]
import com.google.auth.oauth2.AwsCredentials;
import com.google.auth.oauth2.AwsSecurityCredentials;
import com.google.auth.oauth2.AwsSecurityCredentialsSupplier;
import com.google.auth.oauth2.ExternalAccountSupplierContext;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
// [END auth_custom_credential_supplier_aws]

/**
 * This sample demonstrates how to use a custom AWS security credentials supplier to authenticate to
 * Google Cloud Storage using AWS Workload Identity Federation.
 */
public class CustomCredentialSupplierAwsWorkload {

    public static void main(String[] args) throws IOException {
        // The audience for the workload identity federation.
        // Format:
        // //iam.googleapis.com/projects/<project-number>/locations/global/workloadIdentityPools/<pool-id>/providers/<provider-id>
        String gcpWorkloadAudience = System.getenv("GCP_WORKLOAD_AUDIENCE");

        // The bucket to fetch data from.
        String gcsBucketName = System.getenv("GCS_BUCKET_NAME");

        // (Optional) The service account impersonation URL.
        String saImpersonationUrl = System.getenv("GCP_SERVICE_ACCOUNT_IMPERSONATION_URL");

        if (gcpWorkloadAudience == null || gcsBucketName == null) {
            System.err.println(
                    "Error: GCP_WORKLOAD_AUDIENCE and GCS_BUCKET_NAME environment variables are required.");
            return;
        }

        System.out.println("Getting metadata for bucket: " + gcsBucketName + "...");
        Bucket bucket =
                authenticateWithAwsCredentials(gcpWorkloadAudience, saImpersonationUrl, gcsBucketName);

        System.out.println(" --- SUCCESS! ---");
        System.out.printf("Bucket Name: %s%n", bucket.getName());
        System.out.printf("Bucket Location: %s%n", bucket.getLocation());
    }

    /**
     * Authenticates using a custom AWS credential supplier and retrieves bucket metadata.
     *
     * @param gcpWorkloadAudience The WIF provider audience.
     * @param saImpersonationUrl Optional service account impersonation URL.
     * @param gcsBucketName The GCS bucket name.
     * @return The Bucket object containing metadata.
     * @throws IOException If authentication fails.
     */
    // [START auth_custom_credential_supplier_aws]
    public static Bucket authenticateWithAwsCredentials(
            String gcpWorkloadAudience, String saImpersonationUrl, String gcsBucketName)
            throws IOException {

        // 1. Instantiate the custom supplier.
        CustomAwsSupplier customSupplier = new CustomAwsSupplier();

        // 2. Configure the AwsCredentials options.
        AwsCredentials.Builder credentialsBuilder =
                AwsCredentials.newBuilder()
                        .setAudience(gcpWorkloadAudience)
                        // This token type indicates that the subject token is an AWS Signature Version 4 signed
                        // request. This is required for AWS Workload Identity Federation.
                        .setSubjectTokenType("urn:ietf:params:aws:token-type:aws4_request")
                        .setAwsSecurityCredentialsSupplier(customSupplier);

        if (saImpersonationUrl != null) {
            credentialsBuilder.setServiceAccountImpersonationUrl(saImpersonationUrl);
        }

        GoogleCredentials credentials = credentialsBuilder.build();

        // 3. Use the credentials to make an authenticated request.
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

        return storage.get(gcsBucketName);
    }

    /**
     * Custom AWS Security Credentials Supplier.
     *
     * <p>This implementation resolves AWS credentials and regions using the default provider chains
     * from the AWS SDK (v2). This supports environment variables, ~/.aws/credentials, and EC2/EKS
     * metadata.
     */
    private static class CustomAwsSupplier implements AwsSecurityCredentialsSupplier {
        private final AwsCredentialsProvider awsCredentialsProvider;
        private String region;

        public CustomAwsSupplier() {
            // The AWS SDK handles memoization and refreshing internally.
            this.awsCredentialsProvider = DefaultCredentialsProvider.create();
        }

        @Override
        public String getRegion(ExternalAccountSupplierContext context) {
            if (this.region == null) {
                Region awsRegion = new DefaultAwsRegionProviderChain().getRegion();
                if (awsRegion == null) {
                    throw new IllegalStateException(
                            "Unable to resolve AWS region. Ensure AWS_REGION is set or configured.");
                }
                this.region = awsRegion.id();
            }
            return this.region;
        }

        @Override
        public AwsSecurityCredentials getCredentials(ExternalAccountSupplierContext context) {
            software.amazon.awssdk.auth.credentials.AwsCredentials credentials =
                    this.awsCredentialsProvider.resolveCredentials();

            if (credentials == null) {
                throw new IllegalStateException("Unable to resolve AWS credentials.");
            }

            String sessionToken = null;
            if (credentials instanceof AwsSessionCredentials) {
                sessionToken = ((AwsSessionCredentials) credentials).sessionToken();
            }

            return new AwsSecurityCredentials(
                    credentials.accessKeyId(), credentials.secretAccessKey(), sessionToken);
        }
    }
    // [END auth_custom_credential_supplier_aws]
}