/*
 * Copyright 2023 Google LLC
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

package genappbuilder.v1beta;

// [START genappbuilder_search]

import com.google.cloud.discoveryengine.v1beta.SearchRequest;
import com.google.cloud.discoveryengine.v1beta.SearchResponse;
import com.google.cloud.discoveryengine.v1beta.SearchServiceClient;
import com.google.cloud.discoveryengine.v1beta.ServingConfigName;

public class Search {
  public static void search() throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "PROJECT_ID";
    String location = "global";
    String collectionId = "default_collection";
    String searchEngineId = "DATA_STORE_ID";
    String servingConfigId = "default_search";
    String searchQuery = "Google";
    search();
  }

  public static void search(
      String projectId,
      String location,
      String collectionId,
      String searchEngineId,
      String servingConfigId,
      String searchQuery)
      throws Exception {
    try (SearchServiceClient searchServiceClient = SearchServiceClient.create()) {
      SearchRequest request =
          SearchRequest.newBuilder()
              .setServingConfig(
                  ServingConfigName.of(
                          projectId, location, collectionId, searchEngineId, servingConfigId)
                      .toString())
              .setQuery(searchQuery)
              .setPageSize(10)
              .build();
      SearchResponse response = searchClient.search(request).getPage().getResponse();
      for (SearchResponse.SearchResult element : response.getResultList()) {
        System.out.println("Response content: " + element);
      }
    }
  }
}
// [END genappbuilder_search]
