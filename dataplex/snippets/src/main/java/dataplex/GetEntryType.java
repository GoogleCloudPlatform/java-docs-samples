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

// [START dataplex_get_entry_type]
import com.google.cloud.dataplex.v1.CatalogServiceClient;
import com.google.cloud.dataplex.v1.EntryType;
import com.google.cloud.dataplex.v1.EntryTypeName;
import java.io.IOException;

public class GetEntryType {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    // Available locations: https://cloud.google.com/dataplex/docs/locations
    String location = "MY_LOCATION";
    String entryTypeId = "MY_ENTRY_TYPE_ID";

    EntryType entryType = getEntryType(projectId, location, entryTypeId);
    System.out.println("Entry type retrieved successfully: " + entryType.getName());
  }

  // Method to retrieve Entry Type located in projectId, location and with entryTypeId
  public static EntryType getEntryType(String projectId, String location, String entryTypeId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      EntryTypeName entryTypeName = EntryTypeName.of(projectId, location, entryTypeId);
      return client.getEntryType(entryTypeName);
    }
  }
}
// [END dataplex_get_entry_type]
