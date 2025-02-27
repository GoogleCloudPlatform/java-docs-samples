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

// [START dataplex_get_aspect_type]
import com.google.cloud.dataplex.v1.AspectType;
import com.google.cloud.dataplex.v1.AspectTypeName;
import com.google.cloud.dataplex.v1.CatalogServiceClient;
import java.io.IOException;

public class GetAspectType {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "MY_PROJECT_ID";
    // Available locations: https://cloud.google.com/dataplex/docs/locations
    String location = "MY_LOCATION";
    String aspectTypeId = "MY_ASPECT_TYPE_ID";

    AspectType aspectType = getAspectType(projectId, location, aspectTypeId);
    System.out.println("Aspect type retrieved successfully: " + aspectType.getName());
  }

  // Method to retrieve Aspect Type located in projectId, location and with aspectTypeId
  public static AspectType getAspectType(String projectId, String location, String aspectTypeId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (CatalogServiceClient client = CatalogServiceClient.create()) {
      AspectTypeName aspectTypeName = AspectTypeName.of(projectId, location, aspectTypeId);
      return client.getAspectType(aspectTypeName);
    }
  }
}
// [END dataplex_get_aspect_type]
