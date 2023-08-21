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

package discoveryengine.v1;

// [START genappbuilder_search]

import com.google.cloud.discoveryengine.v1.SearchRequest;
import com.google.cloud.discoveryengine.v1.SearchResponse;
import com.google.cloud.discoveryengine.v1.SearchServiceClient;
import com.google.cloud.discoveryengine.v1.ServingConfigName;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Search {
  public static void main() throws IOException, ExecutionException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "PROJECT_ID";
    // Location of the search engine. Options: "global"
    String location = "global";
    // Collection containing the datastore.
    String collectionId = "default_collection";
    // Datastore/Search Engine ID.
    String searchEngineId = "DATA_STORE_ID";
    // Serving configuration. Options: "default_search"
    String servingConfigId = "default_search";
    // Search Query for the search engine.
    String searchQuery = "Google";
    search(projectId, location, collectionId, searchEngineId, servingConfigId, searchQuery);
  }

  /** Performs a search on a given datastore/search engine. */
  public static void search(
      String projectId,
      String location,
      String collectionId,
      String searchEngineId,
      String servingConfigId,
      String searchQuery)
      throws IOException, ExecutionException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `searchServiceClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (SearchServiceClient searchServiceClient = SearchServiceClient.create()) {
      SearchRequest request =
          SearchRequest.newBuilder()
              .setServingConfig(
                  ServingConfigName.formatProjectLocationCollectionDataStoreServingConfigName(
                      projectId, location, collectionId, searchEngineId, servingConfigId))
              .setQuery(searchQuery)
              .setPageSize(10)
              .build();
      SearchResponse response = searchServiceClient.search(request).getPage().getResponse();
      for (SearchResponse.SearchResult element : response.getResultsList()) {
        System.out.println("Response content: " + element);
      }
    }
  }
}
// [END genappbuilder_search]
