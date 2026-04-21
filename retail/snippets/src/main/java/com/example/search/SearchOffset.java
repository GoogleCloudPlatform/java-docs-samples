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

// [START retail_v2_search_offset]

import com.google.cloud.retail.v2.BranchName;
import com.google.cloud.retail.v2.Product;
import com.google.cloud.retail.v2.SearchRequest;
import com.google.cloud.retail.v2.SearchResponse;
import com.google.cloud.retail.v2.SearchResponse.SearchResult;
import com.google.cloud.retail.v2.SearchServiceClient;
import com.google.cloud.retail.v2.SearchServiceClient.SearchPagedResponse;
import com.google.cloud.retail.v2.ServingConfigName;
import java.io.IOException;

public class SearchOffset {
  public static void main(String[] args) throws IOException {
    String projectId = "my-project-id";
    String visitorId = "my-visitor-id";
    String query = "my search query";
    int offset = 10;

    searchWithOffset(projectId, visitorId, query, offset);
  }

  public static void searchWithOffset(String projectId, String visitorId, String query, int offset)
      throws IOException {
    try (SearchServiceClient searchServiceClient = SearchServiceClient.create()) {
      ServingConfigName servingConfigName =
          ServingConfigName.of(projectId, "global", "default_catalog", "default_search");
      BranchName branchName =
          BranchName.of(projectId, "global", "default_catalog", "default_branch");
      SearchRequest searchRequest =
          SearchRequest.newBuilder()
              .setPlacement(servingConfigName.toString())
              .setBranch(branchName.toString())
              .setVisitorId(visitorId)
              .setQuery(query)
              .setPageSize(10)
              .setOffset(offset)
              .build();
      SearchPagedResponse response = searchServiceClient.search(searchRequest);

      SearchResponse searchResponse = response.getPage().getResponse();

      System.out.println("Found " + searchResponse.getResultsCount() + " results");
      for (SearchResult searchResult : searchResponse.getResultsList()) {
        Product product = searchResult.getProduct();
        System.out.println("---- Search Result ----");
        System.out.println("Product Name: " + product.getName());
      }
    }
  }
}
// [END retail_v2_search_offset]
