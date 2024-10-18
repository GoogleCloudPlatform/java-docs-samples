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

// [START dataplex_get_entry]
import com.google.cloud.dataplex.v1.CatalogServiceClient;
import com.google.cloud.dataplex.v1.Entry;
import com.google.cloud.dataplex.v1.EntryName;
import com.google.cloud.dataplex.v1.EntryView;
import com.google.cloud.dataplex.v1.GetEntryRequest;
import java.io.IOException;

// Sample to get Entry
public class GetEntry {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    String location = "MY_LOCATION";
    String entryGroupId = "MY_ENTRY_GROUP_ID";
    String entryId = "MY_ENTRY_ID";

    Entry entry = getEntry(projectId, location, entryGroupId, entryId);
    System.out.println("Entry retrieved successfully: " + entry.getName());
    entry
        .getAspectsMap()
        .keySet()
        .forEach(aspectKey -> System.out.println("Retrieved aspect for entry: " + aspectKey));
  }

  // When Entry is created in Dataplex for example for BigQuery table,
  // access permissions might differ between Dataplex and source system.
  // Get method checks permissions in Dataplex.
  // Please also refer to LookupEntry code sample, which checks permissions in source system.
  public static Entry getEntry(
      String projectId, String location, String entryGroupId, String entryId) throws IOException {

    GetEntryRequest getEntryRequest =
        GetEntryRequest.newBuilder()
            .setName(EntryName.of(projectId, location, entryGroupId, entryId).toString())
            // View determines which Aspects are returned with the Entry, available options:
            // not set - Defaults to FULL
            // BASIC - Returns entry only, without aspects
            // FULL - Returns all required aspects as well as the keys of all non-required aspects
            // CUSTOM - Returns Aspects matching custom fields in GetEntryRequest (max 100)
            // ALL - Returns all aspects (max 100)
            .setView(EntryView.FULL)
            // Following 2 lines will be ignored, because "View" is set to FULL.
            // Their purpose is to demonstrate how to filter the Aspects returned for Entry
            // when "View" is set to CUSTOM.
            .addAspectTypes("projects/dataplex-types/locations/global/aspectTypes/generic")
            .addPaths("my_path")
            .build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources,
    // or use "try-with-close" statement to do this automatically.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      return client.getEntry(getEntryRequest);
    }
  }
}
// [END dataplex_get_entry]
