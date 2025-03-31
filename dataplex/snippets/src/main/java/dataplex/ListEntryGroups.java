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

// [START dataplex_list_entry_groups]
import com.google.cloud.dataplex.v1.CatalogServiceClient;
import com.google.cloud.dataplex.v1.EntryGroup;
import com.google.cloud.dataplex.v1.LocationName;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;

public class ListEntryGroups {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    // Available locations: https://cloud.google.com/dataplex/docs/locations
    String location = "MY_LOCATION";

    List<EntryGroup> entryGroups = listEntryGroups(projectId, location);
    entryGroups.forEach(
        entryGroup -> System.out.println("Entry group name: " + entryGroup.getName()));
  }

  // Method to list Entry Groups located in projectId and location
  public static List<EntryGroup> listEntryGroups(String projectId, String location)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      LocationName locationName = LocationName.of(projectId, location);
      CatalogServiceClient.ListEntryGroupsPagedResponse listEntryGroupsResponse =
          client.listEntryGroups(locationName);
      // Paging is implicitly handled by .iterateAll(), all results will be returned
      return ImmutableList.copyOf(listEntryGroupsResponse.iterateAll());
    }
  }
}
// [END dataplex_list_entry_groups]
