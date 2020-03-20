/*
 * Copyright 2019 Google LLC
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

// [START storage_s3_sdk_list_buckets]
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import java.util.List;

public class ListGcsBuckets {
  public static void listGcsBuckets(String googleAccessKeyId, String googleAccessKeySecret) {

    // String googleAccessKeyId = "your-google-access-key-id";
    // String googleAccessKeySecret = "your-google-access-key-secret";

    // Create a BasicAWSCredentials using Cloud Storage HMAC credentials.
    BasicAWSCredentials googleCreds =
        new BasicAWSCredentials(googleAccessKeyId, googleAccessKeySecret);

    // Create a new client and do the following:
    // 1. Change the endpoint URL to use the Google Cloud Storage XML API endpoint.
    // 2. Use Cloud Storage HMAC Credentials.
    AmazonS3 interopClient =
        AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    "https://storage.googleapis.com", "auto"))
            .withCredentials(new AWSStaticCredentialsProvider(googleCreds))
            .build();

    // Call GCS to list current buckets
    List<Bucket> buckets = interopClient.listBuckets();

    // Print bucket names
    System.out.println("Buckets:");
    for (Bucket bucket : buckets) {
      System.out.println(bucket.getName());
    }

    // Explicitly clean up client resources.
    interopClient.shutdown();
  }
  // [END storage_s3_sdk_list_buckets]
}
