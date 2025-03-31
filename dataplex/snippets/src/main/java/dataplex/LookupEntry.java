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

// [START dataplex_lookup_entry]
import com.google.cloud.dataplex.v1.CatalogServiceClient;
import com.google.cloud.dataplex.v1.Entry;
import com.google.cloud.dataplex.v1.EntryName;
import com.google.cloud.dataplex.v1.EntryView;
import com.google.cloud.dataplex.v1.LookupEntryRequest;
import java.io.IOException;

public class LookupEntry {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    // Available locations: https://cloud.google.com/dataplex/docs/locations
    String location = "MY_LOCATION";
    String entryGroupId = "MY_ENTRY_GROUP_ID";
    String entryId = "MY_ENTRY_ID";

    Entry entry = lookupEntry(projectId, location, entryGroupId, entryId);
    System.out.println("Entry retrieved successfully: " + entry.getName());
    entry
        .getAspectsMap()
        .keySet()
        .forEach(aspectKey -> System.out.println("Retrieved aspect for entry: " + aspectKey));
  }

  // Method to retrieve Entry located in projectId, location, entryGroupId and with entryId
  // When Entry is created in Dataplex for example for BigQuery table,
  // access permissions might differ between Dataplex and source system.
  // "Lookup" method checks permissions in source system.
  // Please also refer how to get an Entry, which checks permissions in Dataplex.
  public static Entry lookupEntry(
      String projectId, String location, String entryGroupId, String entryId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      String projectLocation = String.format("projects/%s/locations/%s", projectId, location);
      LookupEntryRequest lookupEntryRequest =
          LookupEntryRequest.newBuilder()
              // The project to which the request should be attributed
              .setName(projectLocation)
              // The resource name of the Entry
              .setEntry(EntryName.of(projectId, location, entryGroupId, entryId).toString())
              // View determines which Aspects are returned with the Entry.
              // For all available options, see:
              // https://cloud.google.com/sdk/gcloud/reference/dataplex/entries/lookup#--view
              .setView(EntryView.FULL)
              // Following 2 lines will be ignored, because "View" is set to FULL.
              // Their purpose is to demonstrate how to filter the Aspects returned for Entry
              // when "View" is set to CUSTOM.
              .addAspectTypes("projects/dataplex-types/locations/global/aspectTypes/generic")
              .addPaths("my_path")
              .build();
      return client.lookupEntry(lookupEntryRequest);
    }
  }
}
// [END dataplex_lookup_entry]
