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

// [START dataplex_delete_aspect_type]
import com.google.cloud.dataplex.v1.AspectTypeName;
import com.google.cloud.dataplex.v1.CatalogServiceClient;

public class DeleteAspectType {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    // Available locations: https://cloud.google.com/dataplex/docs/locations
    String location = "MY_LOCATION";
    String aspectTypeId = "MY_ASPECT_TYPE_ID";

    deleteAspectType(projectId, location, aspectTypeId);
    System.out.println("Successfully deleted aspect type");
  }

  // Method to delete Aspect Type located in projectId, location and with aspectTypeId
  public static void deleteAspectType(String projectId, String location, String aspectTypeId)
      throws Exception {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      AspectTypeName aspectTypeName = AspectTypeName.of(projectId, location, aspectTypeId);
      client.deleteAspectTypeAsync(aspectTypeName).get();
    }
  }
}
// [END dataplex_delete_aspect_type]
