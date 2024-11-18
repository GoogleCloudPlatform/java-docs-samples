/*
 * Copyright 2024 Google LLC
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

package dataplex;

// [START dataplex_search_entries]
import com.google.cloud.dataplex.v1.CatalogServiceClient;
import com.google.cloud.dataplex.v1.Entry;
import com.google.cloud.dataplex.v1.SearchEntriesRequest;
import com.google.cloud.dataplex.v1.SearchEntriesResponse;
import com.google.cloud.dataplex.v1.SearchEntriesResult;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SearchEntries {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    // How to write query for search: https://cloud.google.com/dataplex/docs/search-syntax
    String query = "MY_QUERY";
    // Maximum number of Entries that will be returned
    int pageSize = 3;
    // By default, keep page token empty
    String pageToken = "";

    SearchEntriesResponse searchEntriesResponse =
        searchEntries(projectId, query, pageSize, pageToken);
    List<Entry> entries =
        searchEntriesResponse.getResultsList().stream()
            // Extract Entries nested inside search results
            .map(SearchEntriesResult::getDataplexEntry)
            .collect(Collectors.toList());
    entries.forEach(entry -> System.out.println("Entry name found in search: " + entry.getName()));
    if (!searchEntriesResponse.getNextPageToken().isEmpty()) {
      System.out.println(
          "Page token of the next page: " + searchEntriesResponse.getNextPageToken());
    }
  }

  // Method to search Entries located in projectId matching query, up to pageSize results, starting
  // from pageToken. Returns SearchEntriesResponse that contains list of Entries and the token of
  // the next page (if applicable)
  public static SearchEntriesResponse searchEntries(
      String projectId, String query, int pageSize, String pageToken) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      SearchEntriesRequest searchEntriesRequest =
          SearchEntriesRequest.newBuilder()
              .setPageSize(pageSize)
              .setPageToken(pageToken)
              // Required field, will by default limit search scope to organization under which the
              // project is located
              .setName(String.format("projects/%s/locations/global", projectId))
              // Optional field, will further limit search scope only to specified project
              .setScope(String.format("projects/%s", projectId))
              .setQuery(query)
              .build();

      CatalogServiceClient.SearchEntriesPagedResponse searchEntriesResponse =
          client.searchEntries(searchEntriesRequest);
      return searchEntriesResponse.getPage().getResponse();
    }
  }
}
// [END dataplex_search_entries]
