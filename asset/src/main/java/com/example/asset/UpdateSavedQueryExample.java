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

// [START asset_quickstart_update_saved_query]
import com.google.cloud.asset.v1.AssetServiceClient;
import com.google.cloud.asset.v1.SavedQuery;
import com.google.cloud.asset.v1.UpdateSavedQueryRequest;
import com.google.protobuf.FieldMask;

public class UpdateSavedQueryExample {

  // Update a savedQuery
  public static void updateSavedQuery(String savedQueryName, String description) throws Exception {
    // String savedQueryName = "SAVED_QUERY_NAME"
    // String description = "SOME_DESCRIPTION"
    SavedQuery savedQuery =
        SavedQuery.newBuilder()
            .setName(savedQueryName)
            .setDescription(description)
            .build();
    UpdateSavedQueryRequest request =
        UpdateSavedQueryRequest.newBuilder()
            .setSavedQuery(savedQuery)
            .setUpdateMask(
                FieldMask.newBuilder()
                    .addPaths("description")
                    .build())
            .build();
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (AssetServiceClient client = AssetServiceClient.create()) {
      SavedQuery response = client.updateSavedQuery(request);
      System.out.println("SavedQuery updated successfully: " + response.getName());
    } catch (Exception e) {
      System.out.println("Error during UpdateSavedQuery: \n" + e.toString());
    }
  }
}
// [END asset_quickstart_update_saved_query]
