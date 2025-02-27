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

// [START dataplex_update_entry]
import com.google.cloud.dataplex.v1.Aspect;
import com.google.cloud.dataplex.v1.CatalogServiceClient;
import com.google.cloud.dataplex.v1.Entry;
import com.google.cloud.dataplex.v1.EntryName;
import com.google.cloud.dataplex.v1.EntrySource;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.util.Map;

public class UpdateEntry {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    // Available locations: https://cloud.google.com/dataplex/docs/locations
    String location = "MY_LOCATION";
    String entryGroupId = "MY_ENTRY_GROUP_ID";
    String entryId = "MY_ENTRY_ID";

    Entry createdEntry = updateEntry(projectId, location, entryGroupId, entryId);
    System.out.println("Successfully updated entry: " + createdEntry.getName());
  }

  // Method to update Entry located in projectId, location, entryGroupId and with entryId
  public static Entry updateEntry(
      String projectId, String location, String entryGroupId, String entryId) throws Exception {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      Entry entry =
          Entry.newBuilder()
              .setName(EntryName.of(projectId, location, entryGroupId, entryId).toString())
              .setEntrySource(
                  EntrySource.newBuilder()
                      .setDescription("updated description of the entry")
                      .build())
              .putAllAspects(
                  Map.of(
                      "dataplex-types.global.generic",
                      Aspect.newBuilder()
                          .setAspectType(
                              "projects/dataplex-types/locations/global/aspectTypes/generic")
                          .setData(
                              Struct.newBuilder()
                                  // "Generic" Aspect Type have fields called "type" and "system.
                                  // The values below are a sample of possible options.
                                  .putFields(
                                      "type",
                                      Value.newBuilder()
                                          .setStringValue("updated example value")
                                          .build())
                                  .putFields(
                                      "system",
                                      Value.newBuilder()
                                          .setStringValue("updated example system")
                                          .build())
                                  .build())
                          .build()))
              .build();

      // Update mask specifies which fields will be updated.
      // For more information on update masks, see: https://google.aip.dev/161
      FieldMask updateMask =
          FieldMask.newBuilder().addPaths("aspects").addPaths("entry_source.description").build();
      return client.updateEntry(entry, updateMask);
    }
  }
}
// [END dataplex_update_entry]
