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

package com.example.asset;

// [START asset_quickstart_create_feed]
// Imports the Google Cloud client library

import com.google.cloud.ServiceOptions;
import com.google.cloud.asset.v1p2beta1.AssetServiceClient;
import com.google.cloud.asset.v1p2beta1.CreateFeedRequest;
import com.google.cloud.asset.v1p2beta1.FeedOutputConfig;
import com.google.cloud.asset.v1p2beta1.PubsubDestination;
import com.google.cloud.asset.v1p2beta1.Feed;
import com.google.cloud.asset.v1p2beta1.Feed;
import java.util.Arrays;

public class CreateFeedExample {

  // Use the default project Id.
  private static final String projectId = ServiceOptions.getDefaultProjectId();

  /*
   * Get a feed with full feed name
   * @param assetNames used in Feed
   * @params feed identifier
   * @params topic name
   * @param args supplies command-line arguments as an array of String objects.
   */
  public static void main(String... args) throws Exception {
    // Asset names, e.g.: "//storage.googleapis.com/[BUCKET_NAME]"
    String[] assetNames = args[0].split(",");
    String feedId = args[1];
    // topic name, e.g.: "projects/[PROJECT_ID]/topics/[TOPIC_NAME]"    
    String topic = args[2];
    Feed feed = Feed.newBuilder()
      .addAllAssetNames(Arrays.asList(assetNames))
      .setFeedOutputConfig(FeedOutputConfig.newBuilder().setPubsubDestination(PubsubDestination.newBuilder().setTopic(topic).build()).build()).build();
    CreateFeedRequest request = CreateFeedRequest.newBuilder()
      .setParent(String.format("projects/%s", projectId))
      .setFeedId(feedId)
      .setFeed(feed)
      .build();
    try (AssetServiceClient client = AssetServiceClient.create()) {
      Feed response = client.createFeed(request);
      System.out.println(response);
    }
  }
}
// [END asset_quickstart_create_feed]
