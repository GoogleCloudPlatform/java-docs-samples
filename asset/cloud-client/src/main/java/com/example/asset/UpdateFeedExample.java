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

// [START asset_quickstart_update_feed]
// Imports the Google Cloud client library

import com.google.cloud.ServiceOptions;
import com.google.cloud.asset.v1p2beta1.AssetServiceClient;
import com.google.cloud.asset.v1p2beta1.UpdateFeedRequest;
import com.google.cloud.asset.v1p2beta1.FeedOutputConfig;
import com.google.cloud.asset.v1p2beta1.PubsubDestination;
import com.google.cloud.asset.v1p2beta1.Feed;
import com.google.cloud.asset.v1p2beta1.Feed;
import com.google.protobuf.FieldMask;
import java.util.Arrays;

public class UpdateFeedExample {

  /*
   * Get a feed with full feed name
   * @params full feed name to update
   * @params topic name to update
   * @param args supplies command-line arguments as an array of String objects.
   */
  public static void main(String... args) throws Exception {
    String feedName = args[0];
    // topic name to update, e.g.: "projects/[PROJECT_ID]/topics/[TOPIC_NAME]"    
    String topic = args[1];
    Feed feed = Feed.newBuilder()
      .setName(feedName)
      .setFeedOutputConfig(FeedOutputConfig.newBuilder().setPubsubDestination(PubsubDestination.newBuilder().setTopic(topic).build()).build()).build();
    UpdateFeedRequest request = UpdateFeedRequest.newBuilder()
      .setFeed(feed)
      .setUpdateMask(FieldMask.newBuilder().addPaths("feed_output_config.pubsub_destination.topic").build())
      .build();
    try (AssetServiceClient client = AssetServiceClient.create()) {
      Feed response = client.updateFeed(request);
      System.out.println(response);
    }
  }
}
// [END asset_quickstart_update_feed]
