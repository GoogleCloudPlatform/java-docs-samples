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

// [START dataplex_list_entries]
import com.google.cloud.dataplex.v1.CatalogServiceClient;
import com.google.cloud.dataplex.v1.Entry;
import com.google.cloud.dataplex.v1.EntryGroupName;
import com.google.cloud.dataplex.v1.ListEntriesRequest;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;

public class ListEntries {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    String location = "MY_LOCATION";
    String entryGroupId = "MY_ENTRY_GROUP_ID";

    List<Entry> entries = listEntries(projectId, location, entryGroupId);
    entries.forEach(aspectType -> System.out.println("Entry name: " + aspectType.getName()));
  }

  // Method to list Entries
  public static List<Entry> listEntries(String projectId, String location, String entryGroupId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      ListEntriesRequest listEntriesRequest =
          ListEntriesRequest.newBuilder()
              .setParent(EntryGroupName.of(projectId, location, entryGroupId).toString())
              // A filter on the entries to return. Filters are case-sensitive.
              // You can filter the request by the following fields:
              // * entry_type
              // * entry_source.display_name
              // To learn more about filters in general, see:
              // https://cloud.google.com/sdk/gcloud/reference/topic/filters
              .setFilter("entry_type=projects/dataplex-types/locations/global/entryTypes/generic")
              .build();
      CatalogServiceClient.ListEntriesPagedResponse listEntriesResponse =
          client.listEntries(listEntriesRequest);
      // Paging is implicitly handled by .iterateAll(), all results will be returned
      return ImmutableList.copyOf(listEntriesResponse.iterateAll());
    }
  }
}
// [END dataplex_list_entries]
