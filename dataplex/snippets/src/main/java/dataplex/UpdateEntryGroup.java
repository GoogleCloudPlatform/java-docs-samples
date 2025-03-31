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

// [START dataplex_update_entry_group]
import com.google.cloud.dataplex.v1.CatalogServiceClient;
import com.google.cloud.dataplex.v1.EntryGroup;
import com.google.cloud.dataplex.v1.EntryGroupName;
import com.google.protobuf.FieldMask;

public class UpdateEntryGroup {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    // Available locations: https://cloud.google.com/dataplex/docs/locations
    String location = "MY_LOCATION";
    String entryGroupId = "MY_ENTRY_GROUP_ID";

    EntryGroup updatedEntryGroup = updateEntryGroup(projectId, location, entryGroupId);
    System.out.println("Successfully updated entry group: " + updatedEntryGroup.getName());
  }

  // Method to update Entry Group located in projectId, location and with entryGroupId
  public static EntryGroup updateEntryGroup(String projectId, String location, String entryGroupId)
      throws Exception {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      EntryGroup entryGroup =
          EntryGroup.newBuilder()
              .setName(EntryGroupName.of(projectId, location, entryGroupId).toString())
              .setDescription("updated description of the entry group")
              .build();

      // Update mask specifies which fields will be updated.
      // For more information on update masks, see: https://google.aip.dev/161
      FieldMask updateMask = FieldMask.newBuilder().addPaths("description").build();
      return client.updateEntryGroupAsync(entryGroup, updateMask).get();
    }
  }
}
// [END dataplex_update_entry_group]
