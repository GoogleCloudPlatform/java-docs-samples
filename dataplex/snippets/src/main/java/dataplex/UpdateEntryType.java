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

// [START dataplex_update_entry_type]
import com.google.cloud.dataplex.v1.CatalogServiceClient;
import com.google.cloud.dataplex.v1.EntryType;
import com.google.cloud.dataplex.v1.EntryTypeName;
import com.google.protobuf.FieldMask;

public class UpdateEntryType {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    // Available locations: https://cloud.google.com/dataplex/docs/locations
    String location = "MY_LOCATION";
    String entryTypeId = "MY_ENTRY_TYPE_ID";

    EntryType updatedEntryType = updateEntryType(projectId, location, entryTypeId);
    System.out.println("Successfully updated entry type: " + updatedEntryType.getName());
  }

  // Method to update Entry Type located in projectId, location and with entryTypeId
  public static EntryType updateEntryType(String projectId, String location, String entryTypeId)
      throws Exception {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      EntryType entryType =
          EntryType.newBuilder()
              .setName(EntryTypeName.of(projectId, location, entryTypeId).toString())
              .setDescription("updated description of the entry type")
              .build();

      // Update mask specifies which fields will be updated.
      // For more information on update masks, see: https://google.aip.dev/161
      FieldMask updateMask = FieldMask.newBuilder().addPaths("description").build();
      return client.updateEntryTypeAsync(entryType, updateMask).get();
    }
  }
}
// [END dataplex_update_entry_type]
