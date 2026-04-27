/*
 * Copyright 2026 Google LLC
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

package com.example.search;

// [START retail_v2_search_request]

import com.google.cloud.retail.v2.BranchName;
import com.google.cloud.retail.v2.Product;
import com.google.cloud.retail.v2.SearchRequest;
import com.google.cloud.retail.v2.SearchResponse;
import com.google.cloud.retail.v2.SearchResponse.SearchResult;
import com.google.cloud.retail.v2.SearchServiceClient;
import com.google.cloud.retail.v2.SearchServiceClient.SearchPagedResponse;
import com.google.cloud.retail.v2.ServingConfigName;
import java.io.IOException;
import java.util.List;

public class Search {
  public static void main(String[] args) throws IOException {
    String projectId = "my-project-id";
    String placementId = "default_search";
    String visitorId = "my-visitor-id";
    String query = "my search query";
    List<String> categories = List.of("category");

    search(projectId, placementId, visitorId, query, categories);
  }

  /**
   * Search for products using Vertex AI Search for commerce.
   *
   * Performs a search request for a specific placement. Handles both text search (using query)
   * and browse search (using page_categories).
   *
   * @param projectId The Google Cloud project ID.
   * @param placementId The placement name for the search.
   * @param visitorId A unique identifier for the user.
   * @param query The search term for text search.
   * @param categories The categories for browse search.
   */
  public static void search(
      String projectId, String placementId, String visitorId, String query, List<String> categories)
      throws IOException {
    try (SearchServiceClient searchServiceClient = SearchServiceClient.create()) {
      ServingConfigName placementName =
          ServingConfigName.of(projectId, "global", "default_catalog", placementId);
      BranchName branchName =
          BranchName.of(projectId, "global", "default_catalog", "default_branch");
      SearchRequest searchRequest =
          SearchRequest.newBuilder()
              .setPlacement(placementName.toString())
              .setBranch(branchName.toString())
              .setVisitorId(visitorId)
              .setQuery(query)
              .addAllPageCategories(categories)
              .setPageSize(10)
              .build();
      SearchPagedResponse response = searchServiceClient.search(searchRequest);

      SearchResponse searchResponse = response.getPage().getResponse();

      System.out.println("Found " + searchResponse.getResultsCount() + " results in current page");
      for (SearchResult searchResult : searchResponse.getResultsList()) {
        Product product = searchResult.getProduct();
        System.out.println("---- Search Result ----");
        System.out.println("Product Name: " + product.getName());
      }
    }
  }
}
// [END retail_v2_search_request]
