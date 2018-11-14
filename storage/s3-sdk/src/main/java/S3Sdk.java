/*
 * Copyright 2018 Google Inc.
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

/**
 * """This sample shows how to list Google Cloud Storage (GCS) buckets using the AWS S3 SDK with the
 * GCS interoperable XML API.
 *
 * <p>GCS Credentials are passed in using the following environment variables:
 *
 * <ul>
 *   <li>one
 *       <ul>
 *         <li>AWS_ACCESS_KEY_ID
 *         <li>AWS_SECRET_ACCESS_KEY
 *       </ul>
 * </ul>
 *
 * <p>Learn how to get GCS interoperable credentials at
 * https://cloud.google.com/storage/docs/migrating#keys.
 */

// [START storage_s3_sdk_list_buckets]
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;

import java.util.List;

public class S3Sdk {
  public static List<Bucket> listGcsBuckets() {
    // Change the endpoint_url to use the Google Cloud Storage XML API endpoint.
    AmazonS3 interopClient =
        AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    "https://storage.googleapis.com", "auto"))
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

    return buckets;
  }
  // [END storage_s3_sdk_list_buckets]

  public static void main(String[] args) {
    listGcsBuckets();
  }
}

