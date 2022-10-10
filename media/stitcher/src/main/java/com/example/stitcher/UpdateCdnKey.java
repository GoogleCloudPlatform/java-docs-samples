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

// [START videostitcher_update_cdn_key]

import com.google.cloud.video.stitcher.v1.AkamaiCdnKey;
import com.google.cloud.video.stitcher.v1.CdnKey;
import com.google.cloud.video.stitcher.v1.CdnKeyName;
import com.google.cloud.video.stitcher.v1.GoogleCdnKey;
import com.google.cloud.video.stitcher.v1.UpdateCdnKeyRequest;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.protobuf.ByteString;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class UpdateCdnKey {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String cdnKeyId = "my-cdn-key-id2";
    String hostname = "updated.example.com";
    String gcdnKeyname = "my-gcdn-key";
    String gcdnPrivateKey = "VGhpcyBpcyBhIHRlc3Qgc3RyaW5nLg"; // will be converted to a byte string
    String akamaiTokenKey = "VGhpcyBpcyBhIHRlc3Qgc3RyaW5nLg"; // will be converted to a byte string

    updateCdnKey(
        projectId, location, cdnKeyId, hostname, gcdnKeyname, gcdnPrivateKey, akamaiTokenKey);
  }

  // updateCdnKey updates the hostname and key fields for an existing CDN key. If akamaiTokenKey is
  // provided, then update the existing Akamai key fields. If akamaiTokenKey is not provided (""),
  // then update the existing Cloud CDN key fields. Update hostname regardless of key type.
  public static void updateCdnKey(
      String projectId,
      String location,
      String cdnKeyId,
      String hostname,
      String gcdnKeyname,
      String gcdnPrivateKey,
      String akamaiTokenKey)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      CdnKey cdnKey;
      String path;
      if (akamaiTokenKey != "") {
        path = "akamai_cdn_key";
        cdnKey =
            CdnKey.newBuilder()
                .setName(CdnKeyName.of(projectId, location, cdnKeyId).toString())
                .setHostname(hostname)
                .setAkamaiCdnKey(
                    AkamaiCdnKey.newBuilder()
                        .setTokenKey(ByteString.copyFromUtf8(akamaiTokenKey))
                        .build())
                .build();
      } else {
        path = "google_cdn_key";
        cdnKey =
            CdnKey.newBuilder()
                .setName(CdnKeyName.of(projectId, location, cdnKeyId).toString())
                .setHostname(hostname)
                .setGoogleCdnKey(
                    GoogleCdnKey.newBuilder()
                        .setKeyName(gcdnKeyname)
                        .setPrivateKey(ByteString.copyFromUtf8(gcdnPrivateKey))
                        .build())
                .build();
      }

      UpdateCdnKeyRequest updateCdnKeyRequest =
          UpdateCdnKeyRequest.newBuilder()
              .setCdnKey(cdnKey)
              // Update the hostname field and the fields for the specific key type (Cloud CDN
              // or Akamai). You must set the mask to the fields you want to update.
              .setUpdateMask(FieldMask.newBuilder().addPaths("hostname").addPaths(path).build())
              .build();

      CdnKey response = videoStitcherServiceClient.updateCdnKey(updateCdnKeyRequest);
      System.out.println("Updated CDN key: " + response.getName());
    }
  }
}
// [END videostitcher_update_cdn_key]
