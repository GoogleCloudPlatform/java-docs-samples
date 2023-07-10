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

import com.google.cloud.video.stitcher.v1.CdnKey;
import com.google.cloud.video.stitcher.v1.CreateCdnKeyRequest;
import com.google.cloud.video.stitcher.v1.GoogleCdnKey;
import com.google.cloud.video.stitcher.v1.LocationName;
import com.google.cloud.video.stitcher.v1.MediaCdnKey;
import com.google.cloud.video.stitcher.v1.VideoStitcherServiceClient;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateCdnKey {

  private static final int TIMEOUT_IN_MINUTES = 2;

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "us-central1";
    String cdnKeyId = "my-cdn-key-id";
    String hostname = "cdn.example.com";
    String keyName = "my-key";
    // To create a privateKey value for Media CDN, see
    // https://cloud.google.com/video-stitcher/docs/how-to/managing-cdn-keys#create-private-key-media-cdn.
    String privateKey = "my-private-key"; // will be converted to a byte string
    Boolean isMediaCdn = true;

    createCdnKey(projectId, location, cdnKeyId, hostname, keyName, privateKey, isMediaCdn);
  }

  // createCdnKey creates a Media CDN key or a Cloud CDN key. A CDN key is used to retrieve
  // protected media.
  public static void createCdnKey(
      String projectId,
      String location,
      String cdnKeyId,
      String hostname,
      String keyName,
      String privateKey,
      Boolean isMediaCdn)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (VideoStitcherServiceClient videoStitcherServiceClient =
        VideoStitcherServiceClient.create()) {
      CdnKey cdnKey;
      if (isMediaCdn) {
        cdnKey =
            CdnKey.newBuilder()
                .setHostname(hostname)
                .setMediaCdnKey(
                    MediaCdnKey.newBuilder()
                        .setKeyName(keyName)
                        .setPrivateKey(ByteString.copyFromUtf8(privateKey))
                        .build())
                .build();
      } else {
        cdnKey =
            CdnKey.newBuilder()
                .setHostname(hostname)
                .setGoogleCdnKey(
                    GoogleCdnKey.newBuilder()
                        .setKeyName(keyName)
                        .setPrivateKey(ByteString.copyFromUtf8(privateKey))
                        .build())
                .build();
      }

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
// [END videostitcher_create_cdn_key]
