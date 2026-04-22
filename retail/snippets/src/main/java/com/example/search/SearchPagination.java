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

// [START retail_v2_search_pagination]

import com.google.cloud.retail.v2.BranchName;
import com.google.cloud.retail.v2.Product;
import com.google.cloud.retail.v2.SearchRequest;
import com.google.cloud.retail.v2.SearchResponse.SearchResult;
import com.google.cloud.retail.v2.SearchServiceClient;
import com.google.cloud.retail.v2.SearchServiceClient.SearchPage;
import com.google.cloud.retail.v2.SearchServiceClient.SearchPagedResponse;
import com.google.cloud.retail.v2.ServingConfigName;
import java.io.IOException;

public class SearchPagination {
  public static void main(String[] args) throws IOException {
    String projectId = "my-project-id";
    String visitorId = "my-visitor-id";
    String query = "my search query";
    int pageSize = 10;

    searchWithPagination(projectId, visitorId, query, pageSize);
  }

  public static void searchWithPagination(
      String projectId, String visitorId, String query, int pageSize) throws IOException {
    try (SearchServiceClient searchServiceClient = SearchServiceClient.create()) {
      ServingConfigName servingConfigName =
          ServingConfigName.of(projectId, "global", "default_catalog", "default_search");
      BranchName branchName =
          BranchName.of(projectId, "global", "default_catalog", "default_branch");
      SearchRequest request =
          SearchRequest.newBuilder()
              .setPlacement(servingConfigName.toString())
              .setBranch(branchName.toString())
              .setVisitorId(visitorId)
              .setQuery(query)
              .setPageSize(pageSize)
              .build();
      int currentPage = 0;
      while (true) {
        SearchPagedResponse response = searchServiceClient.search(request);

        SearchPage page = response.getPage();
        currentPage++;
        System.out.println("\nResults of page number " + currentPage + ":");
        System.out.println(
            "Found " + page.getResponse().getResultsCount() + " results in current page");
        for (SearchResult searchResult : page.getResponse().getResultsList()) {
          Product product = searchResult.getProduct();
          System.out.println("---- Search Result ----");
          System.out.println("Product Name: " + product.getName());
        }

        if (page.hasNextPage()) {
          request = request.toBuilder().setPageToken(page.getNextPageToken()).build();
        } else {
          System.out.println("\nNo more available pages.");
          break;
        }
      }
    }
  }
}
// [END retail_v2_search_pagination]
