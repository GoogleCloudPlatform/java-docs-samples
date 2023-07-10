/*
 * Copyright 2022 Google LLC
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

package com.example.stitcher;

// [START videostitcher_create_cdn_key_akamai]

import com.google.cloud.video.stitcher.v1.AkamaiCdnKey;
import com.google.cloud.video.stitcher.v1.CdnKey;
import com.google.cloud.video.stitcher.v1.CreateCdnKeyRequest;
import com.google.cloud.video.stitcher.v1.LocationName;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateCdnKeyAkamai {

  private static final int TIMEOUT_IN_MINUTES = 2;

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String cdnKeyId = "my-cdn-key-id";
    String hostname = "cdn.example.com";
    String akamaiTokenKey = "my-token-key"; // will be converted to a byte string

    createCdnKeyAkamai(projectId, location, cdnKeyId, hostname, akamaiTokenKey);
  }

  // createCdnKeyAkamai creates an Akamai CDN key. A CDN key is used to retrieve protected media.
  public static void createCdnKeyAkamai(
      String projectId, String location, String cdnKeyId, String hostname, String akamaiTokenKey)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      CdnKey cdnKey =
          CdnKey.newBuilder()
              .setHostname(hostname)
              .setAkamaiCdnKey(
                  AkamaiCdnKey.newBuilder()
                      .setTokenKey(ByteString.copyFromUtf8(akamaiTokenKey))
                      .build())
              .build();

      CreateCdnKeyRequest createCdnKeyRequest =
          CreateCdnKeyRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .setCdnKeyId(cdnKeyId)
              .setCdnKey(cdnKey)
              .build();

      CdnKey result = videoStitcherServiceClient.createCdnKeyAsync(createCdnKeyRequest)
          .get(TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
      System.out.println("Created new CDN key: " + result.getName());
    }
  }
}
// [END videostitcher_create_cdn_key_akamai]
