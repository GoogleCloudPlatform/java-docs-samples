/*
 * Copyright 2020 Google LLC
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
import com.google.cloud.asset.v1p1beta1.AssetServiceClient.SearchAllIamPoliciesPage;
import com.google.cloud.asset.v1p1beta1.AssetServiceClient.SearchAllIamPoliciesPagedResponse;

// [START asset_quickstart_search_all_iam_policies]
public class SearchAllIamPoliciesExample {

  public static void searchIamPolicies() throws Exception {
    // Search resources within a given project, folder or organization.
    String scope = "projects/123456789";
    // To learn how to construct a query, see
    // https://cloud.google.com/asset-inventory/docs/searching-iam-policies#how_to_construct_a_query.
    String query = "policy:\"user:foo@mycompany.com\"";

    searchAllIamPolicies(scope, query);
  }

  /**
   * Search Iam Policies that matches the given {@code scope}, {@code query}. It only return the IAM
   * Policies, when you have searchAllIamPolicies permission on the given {@code scope}.
   */
  public static void searchAllIamPolicies(String scope, String query) throws Exception {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (AssetServiceClient client = AssetServiceClient.create()) {
      SearchAllIamPoliciesPagedResponse resp = client.searchAllIamPolicies(scope, query);
      SearchAllIamPoliciesPage page = resp.getPage();
      int maxPageNumToTraverse = 3;
      int pageNum = 0;
      while (pageNum < maxPageNumToTraverse) {
        System.out.println("Search results page " + (++pageNum) + ": " + page.toString());
        if (!page.hasNextPage()) {
          break;
        }
        page = page.getNextPage();
      }
    }
  }
}
// [END asset_quickstart_search_all_iam_policies]
