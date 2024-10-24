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

// [START dataplex_create_entry]
import com.google.cloud.dataplex.v1.Aspect;
import com.google.cloud.dataplex.v1.CatalogServiceClient;
import com.google.cloud.dataplex.v1.Entry;
import com.google.cloud.dataplex.v1.EntryGroupName;
import com.google.cloud.dataplex.v1.EntrySource;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.util.Map;

public class CreateEntry {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    String location = "MY_LOCATION";
    String entryGroupId = "MY_ENTRY_GROUP_ID";
    String entryId = "MY_ENTRY_ID";

    Entry createdEntry = createEntry(projectId, location, entryGroupId, entryId);
    System.out.println("Successfully created entry: " + createdEntry.getName());
  }

  // Method to create Entry
  public static Entry createEntry(
      String projectId, String location, String entryGroupId, String entryId) throws Exception {
    EntryGroupName entryGroupName = EntryGroupName.of(projectId, location, entryGroupId);
    Entry entry =
        Entry.newBuilder()
            // Example of system Entry Type.
            // It is also possible to specify custom Entry Type.
            .setEntryType("projects/dataplex-types/locations/global/entryTypes/generic")
            .setEntrySource(
                EntrySource.newBuilder().setDescription("description of the entry").build())
            .putAllAspects(
                Map.of(
                    "dataplex-types.global.generic",
                    Aspect.newBuilder()
                        // This is required Aspect Type for "generic" Entry Type.
                        // For custom Aspect Type required Entry Type would be different.
                        .setAspectType(
                            "projects/dataplex-types/locations/global/aspectTypes/generic")
                        .setData(
                            Struct.newBuilder()
                                // "Generic" Aspect Type have fields called "type" and "system.
                                // The values below are a sample of possible options.
                                .putFields(
                                    "type",
                                    Value.newBuilder().setStringValue("example value").build())
                                .putFields(
                                    "system",
                                    Value.newBuilder().setStringValue("example system").build())
                                .build())
                        .build()))
            .build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      return client.createEntry(entryGroupName, entry, entryId);
    }
  }
}
// [END dataplex_create_entry]
