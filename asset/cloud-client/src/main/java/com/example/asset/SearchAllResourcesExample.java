/*
 * Copyright 2018 Google LLC
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

import com.google.cloud.asset.v1p1beta1.AssetServiceClient;
import com.google.cloud.asset.v1p1beta1.AssetServiceClient.SearchAllResourcesPage;
import com.google.cloud.asset.v1p1beta1.AssetServiceClient.SearchAllResourcesPagedResponse;
import java.util.List;

// [START asset_quickstart_search_all_resources]
public class SearchAllResourcesExample {
  /**
   * Search Resources that matches the given {@code scope}, {@code query}, {@code assetTypes}. It
   * only return resources when you have SearchAllResources permission (can be granted by "Cloud
   * Asset Viewer" role) on the given {@code scope}.
   */
  public static void searchAllResources(String scope, String query, List<String> assetTypes)
      throws Exception {
    // String scope = "projects/123456789";
    // String query = "name:\"*abc*\"";
    // List<String> assetTypes =
    //     Arrays.asList(
    //         "cloudresourcemanager.googleapis.com/Project", "compute.googleapis.com/Instance");

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (AssetServiceClient client = AssetServiceClient.create()) {
      SearchAllResourcesPagedResponse resp = client.searchAllResources(scope, query, assetTypes);
      SearchAllResourcesPage page = resp.getPage();
      int maxPageNumToTraverse = 3;
      int pageNum = 0;
      while (pageNum < maxPageNumToTraverse) {
        System.out.println("Search results page " + (++pageNum) + ": " + page.toString());
        if (!page.hasNextPage()) {
          break;
        }
        page = page.getNextPage();
      }
    } catch (Exception e) {
      System.out.println("Error during SearchAllResources: \n" + e.toString());
    }
  }
}
// [END asset_quickstart_search_all_resources]