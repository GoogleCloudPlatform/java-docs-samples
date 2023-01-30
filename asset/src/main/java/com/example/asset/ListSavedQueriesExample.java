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

package com.example.asset;

// [START asset_quickstart_list_saved_queries]
import com.google.cloud.asset.v1.AssetServiceClient;
import com.google.cloud.asset.v1.AssetServiceClient.ListSavedQueriesPagedResponse;
import com.google.cloud.asset.v1.ProjectName;

public class ListSavedQueriesExample {
  // List savedQueries in a project.
  public static void listSavedQueries(String projectId) throws Exception {
    // String projectId = "MY_PROJECT_ID"

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (AssetServiceClient client = AssetServiceClient.create()) {
      ListSavedQueriesPagedResponse response = client.listSavedQueries(ProjectName.of(
          projectId).toString());
      System.out.println("Listed savedQueries under: " + projectId);
    } catch (Exception e) {
      System.out.println("Error during ListSavedQueries: \n" + e.toString());
    }
  }
}
// [END asset_quickstart_list_saved_queries]
