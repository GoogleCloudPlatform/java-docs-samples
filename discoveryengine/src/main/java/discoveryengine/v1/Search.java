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
import com.google.cloud.discoveryengine.v1.SearchServiceSettings;
import com.google.cloud.discoveryengine.v1.ServingConfigName;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Search {
  public static void main() throws IOException, ExecutionException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "PROJECT_ID";
    // Location of the data store. Options: "global", "us", "eu"
    String location = "global";
    // Collection containing the data store.
    String collectionId = "default_collection";
    // Data store ID.
    String dataStoreId = "DATA_STORE_ID";
    // Serving configuration. Options: "default_search"
    String servingConfigId = "default_search";
    // Search Query for the data store.
    String searchQuery = "Google";
    search(projectId, location, collectionId, dataStoreId, servingConfigId, searchQuery);
  }

  /** Performs a search on a given datastore. */
  public static void search(
      String projectId,
      String location,
      String collectionId,
      String dataStoreId,
      String servingConfigId,
      String searchQuery)
      throws IOException, ExecutionException {
    // For more information, refer to:
    // https://cloud.google.com/generative-ai-app-builder/docs/locations#specify_a_multi-region_for_your_data_store
    String endpoint = (location.equals("global")) 
        ? String.format("discoveryengine.googleapis.com:443", location) 
        : String.format("%s-discoveryengine.googleapis.com:443", location);
    SearchServiceSettings settings =
        SearchServiceSettings.newBuilder().setEndpoint(endpoint).build();
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `searchServiceClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (SearchServiceClient searchServiceClient = SearchServiceClient.create(settings)) {
      SearchRequest request =
          SearchRequest.newBuilder()
              .setServingConfig(
                  ServingConfigName.formatProjectLocationCollectionDataStoreServingConfigName(
                      projectId, location, collectionId, dataStoreId, servingConfigId))
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
