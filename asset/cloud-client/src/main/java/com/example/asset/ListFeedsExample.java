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

// [START asset_quickstart_list_feeds]
// Imports the Google Cloud client library

import com.google.cloud.ServiceOptions;
import com.google.cloud.asset.v1p2beta1.AssetServiceClient;
import com.google.cloud.asset.v1p2beta1.ListFeedsResponse;
import java.util.Arrays;

public class ListFeedsExample {

  // Use the default project Id.
  private static final String projectId = ServiceOptions.getDefaultProjectId();

  // Export assets for a project.
  public static void main(String... args) throws Exception {
    try (AssetServiceClient client = AssetServiceClient.create()) {
      ListFeedsResponse response = client.listFeeds(String.format("projects/%s", projectId));
      System.out.println(response);
    }
  }
}
// [END asset_quickstart_list_feeds]
