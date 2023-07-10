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

// [START videostitcher_update_cdn_key_akamai]

import com.google.cloud.video.stitcher.v1.AkamaiCdnKey;
import com.google.cloud.video.stitcher.v1.CdnKey;
import com.google.cloud.video.stitcher.v1.CdnKeyName;
import com.google.cloud.video.stitcher.v1.UpdateCdnKeyRequest;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.protobuf.ByteString;
import com.google.protobuf.FieldMask;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UpdateCdnKeyAkamai {

  private static final int TIMEOUT_IN_MINUTES = 2;

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String cdnKeyId = "my-updated-cdn-key-id";
    String hostname = "updated.example.com";
    String akamaiTokenKey = "my-updated-token-key"; // will be converted to a byte string

    updateCdnKeyAkamai(projectId, location, cdnKeyId, hostname, akamaiTokenKey);
  }

  // updateCdnKeyAkamai u/**/pdates the hostname and key fields for an existing CDN key.
  public static void updateCdnKeyAkamai(
      String projectId, String location, String cdnKeyId, String hostname, String akamaiTokenKey)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      CdnKey cdnKey =
          CdnKey.newBuilder()
              .setName(CdnKeyName.of(projectId, location, cdnKeyId).toString())
              .setHostname(hostname)
              .setAkamaiCdnKey(
                  AkamaiCdnKey.newBuilder()
                      .setTokenKey(ByteString.copyFromUtf8(akamaiTokenKey))
                      .build())
              .build();

      UpdateCdnKeyRequest updateCdnKeyRequest =
          UpdateCdnKeyRequest.newBuilder()
              .setCdnKey(cdnKey)
              // Update the hostname field and token key field. You must set the mask to the fields
              // you want to update.
              .setUpdateMask(
                  FieldMask.newBuilder().addPaths("hostname").addPaths("akamai_cdn_key").build())
              .build();

      CdnKey response = videoStitcherServiceClient.updateCdnKeyAsync(updateCdnKeyRequest)
          .get(TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
      System.out.println("Updated CDN key: " + response.getName());
    }
  }
}
// [END videostitcher_update_cdn_key_akamai]
