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

// [START videostitcher_create_cdn_key]

import com.google.cloud.video.stitcher.v1.AkamaiCdnKey;
import com.google.cloud.video.stitcher.v1.CdnKey;
import com.google.cloud.video.stitcher.v1.CreateCdnKeyRequest;
import com.google.cloud.video.stitcher.v1.GoogleCdnKey;
import com.google.cloud.video.stitcher.v1.LocationName;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.protobuf.ByteString;
import java.io.IOException;

public class CreateCdnKey {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String cdnKeyId = "my-cdn-key-id";
    String hostname = "cdn.example.com";
    String gcdnKeyname = "my-gcdn-key";
    String gcdnPrivateKey = "VGhpcyBpcyBhIHRlc3Qgc3RyaW5nLg"; // will be converted to a byte string
    String akamaiTokenKey = "VGhpcyBpcyBhIHRlc3Qgc3RyaW5nLg"; // will be converted to a byte string

    createCdnKey(
        projectId, location, cdnKeyId, hostname, gcdnKeyname, gcdnPrivateKey, akamaiTokenKey);
  }

  // createCdnKey creates an Akamai or Cloud CDN key. If akamaiTokenKey is
  // provided, then create an Akamai key. If akamaiTokenKey is not provided (""),
  // then create a Cloud CDN key.
  public static void createCdnKey(
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
      if (akamaiTokenKey != "") {
        cdnKey =
            CdnKey.newBuilder()
                .setHostname(hostname)
                .setAkamaiCdnKey(
                    AkamaiCdnKey.newBuilder()
                        .setTokenKey(ByteString.copyFromUtf8(akamaiTokenKey))
                        .build())
                .build();
      } else {
        cdnKey =
            CdnKey.newBuilder()
                .setHostname(hostname)
                .setGoogleCdnKey(
                    GoogleCdnKey.newBuilder()
                        .setKeyName(gcdnKeyname)
                        .setPrivateKey(ByteString.copyFromUtf8(gcdnPrivateKey))
                        .build())
                .build();
      }

      CreateCdnKeyRequest createCdnKeyRequest =
          CreateCdnKeyRequest.newBuilder()
              .setParent(LocationName.of(projectId, location).toString())
              .setCdnKeyId(cdnKeyId)
              .setCdnKey(cdnKey)
              .build();

      CdnKey response = videoStitcherServiceClient.createCdnKey(createCdnKeyRequest);
      System.out.println("Created new CDN key: " + response.getName());
    }
  }
}
// [END videostitcher_create_cdn_key]
