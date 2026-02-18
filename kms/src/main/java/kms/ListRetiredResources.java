/*
 * Copyright 2026 Google LLC
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

package kms;

// [START kms_list_retired_resources]
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.kms.v1.LocationName;
import com.google.cloud.kms.v1.RetiredResource;
import java.io.IOException;

public class ListRetiredResources {

  public void listRetiredResources() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "us-east1";
    listRetiredResources(projectId, locationId);
  }

  // List retired resources in a specific project and location.
  public void listRetiredResources(String projectId, String locationId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests. After
    // completing all of your requests, call the "close" method on the client to
    // safely clean up any remaining background resources.
    try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
      // Build the location name from the project and location.
      LocationName locationName = LocationName.of(projectId, locationId);

      // List the retired resources.
      for (RetiredResource resource : client.listRetiredResources(locationName).iterateAll()) {
        System.out.printf("Retired resource: %s%n", resource.getName());
      }
    }
  }
}
// [END kms_list_retired_resources]
