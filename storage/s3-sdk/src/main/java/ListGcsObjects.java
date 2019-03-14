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

// Solution found in: https://stackoverflow.com/questions/47264520/unable-to-listobjects-from-gcs-bucket

// [START storage_s3_sdk_list_objects]
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.handlers.RequestHandler2;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.ObjectListing;

import com.amazonaws.Request;

import java.util.List;
import java.util.Map;

public class ListGcsObjects {
  public static ObjectListing listGcsObjects(String googleAccessKeyId, 
      String googleAccessKeySecret, String bucketName) {
    // Create a BasicAWSCredentials using Cloud Storage HMAC credentials.
    BasicAWSCredentials googleCreds = new BasicAWSCredentials(googleAccessKeyId, 
        googleAccessKeySecret);

    // Create a new client and do the following:
    // 1. Change the endpoint URL to use the Google Cloud Storage XML API endpoint.
    // 2. Use Cloud Storage HMAC Credentials.
    // 3. Remove "encoding-type" when using listObjects() using withRequestHandlers().
    AmazonS3 interopClient =
        AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    "https://storage.googleapis.com", "auto"))
            .withCredentials(new AWSStaticCredentialsProvider(googleCreds))
            .withRequestHandlers(new RemoveEncodingType())
            .build();

    // Call GCS to list current objects
    ObjectListing objects = interopClient.listObjects(bucketName);

    // Print objects names
    System.out.println("Objects:");
    for (S3ObjectSummary object : objects.getObjectSummaries()) {
      System.out.println(object.getKey());
    }

    // Explicitly clean up client resources.
    interopClient.shutdown();

    return objects;
  }

  // Cloud Storage does not support encoding-type and must be removed
  // from a ListObjectsRequest made by the client. 
  // The following class removes encoding-type and is used whenever
  // the listObjects() is called using S3 SDK.
  private static class RemoveEncodingType extends RequestHandler2 {
    @Override
    public void beforeRequest(Request<?> request) {
      if (request.getOriginalRequest() instanceof ListObjectsRequest) {
          request.getParameters().remove("encoding-type");
      }
    }
  }
  // [END storage_s3_sdk_list_objects]
}

