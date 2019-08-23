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

// [START asset_quickstart_delete_feed]
// Imports the Google Cloud client library

import com.google.cloud.ServiceOptions;
import com.google.cloud.asset.v1p2beta1.AssetServiceClient;
import com.google.cloud.asset.v1p2beta1.Feed;
import java.util.Arrays;

public class DeleteFeedExample {

  // Delete a feed with full feed name
  // @param args feed name to be deleted.
  public static void main(String... args) throws Exception {
    // Full Feed name, e.g.: "projects/[PROJECT_NUMBER]/feed/[FEED_ID]"
    String feedName = args[0];
    try (AssetServiceClient client = AssetServiceClient.create()) {
      client.deleteFeed(feedName);
    }
  }
}
// [END asset_quickstart_delete_feed]
