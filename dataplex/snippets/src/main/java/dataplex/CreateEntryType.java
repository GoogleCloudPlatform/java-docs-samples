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

// [START dataplex_create_entry_type]
import com.google.cloud.dataplex.v1.CatalogServiceClient;
import com.google.cloud.dataplex.v1.EntryType;
import com.google.cloud.dataplex.v1.LocationName;

public class CreateEntryType {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    // Available locations: https://cloud.google.com/dataplex/docs/locations
    String location = "MY_LOCATION";
    String entryTypeId = "MY_ENTRY_TYPE_ID";

    EntryType createdEntryType = createEntryType(projectId, location, entryTypeId);
    System.out.println("Successfully created entry type: " + createdEntryType.getName());
  }

  // Method to create Entry Type located in projectId, location and with entryTypeId
  public static EntryType createEntryType(String projectId, String location, String entryTypeId)
      throws Exception {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      LocationName locationName = LocationName.of(projectId, location);
      EntryType entryType =
          EntryType.newBuilder()
              .setDescription("description of the entry type")
              // Required aspects will need to be attached to every entry created for this entry
              // type.
              // You cannot change required aspects for entry type once it is created.
              .addRequiredAspects(
                  EntryType.AspectInfo.newBuilder()
                      // Example of system aspect type.
                      // It is also possible to specify custom aspect type.
                      .setType("projects/dataplex-types/locations/global/aspectTypes/schema")
                      .build())
              .build();
      return client.createEntryTypeAsync(locationName, entryType, entryTypeId).get();
    }
  }
}
// [END dataplex_create_entry_type]
