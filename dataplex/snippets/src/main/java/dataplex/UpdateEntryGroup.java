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

// Sample to update Entry Group
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

  public static EntryGroup updateEntryGroup(String projectId, String location, String entryGroupId)
      throws Exception {
    EntryGroup entryGroup =
        EntryGroup.newBuilder()
            .setName(EntryGroupName.of(projectId, location, entryGroupId).toString())
            .setDescription("updated description of the entry group")
            .build();

    // Update mask specifies which fields will be updated.
    // If empty mask is given, all modifiable fields from the request will be used for update.
    // If update mask is specified as "*" it is treated as full update,
    // that means fields not present in the request will be emptied.
    FieldMask updateMask = FieldMask.newBuilder().addPaths("description").build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources,
    // or use "try-with-close" statement to do this automatically.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      return client.updateEntryGroupAsync(entryGroup, updateMask).get();
    }
  }
}
// [END dataplex_update_entry_group]
