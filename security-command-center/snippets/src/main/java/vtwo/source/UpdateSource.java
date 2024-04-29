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

package vtwo.source;

// [START securitycenter_update_source_v2]

import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.Source;
import com.google.cloud.securitycenter.v2.SourceName;
import com.google.cloud.securitycenter.v2.UpdateSourceRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;

public class UpdateSource {

  public static void main(String[] args) throws IOException {
    // TODO: Replace the below variables.
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    // Specify the source-id.
    String sourceId = "{source-id}";

    updateSource(organizationId, sourceId);
  }

  // Demonstrates how to update a source.
  public static Source updateSource(String organizationId, String sourceId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      // Start setting up a request to get a source.
      SourceName sourceName = SourceName.ofOrganizationSourceName(organizationId, sourceId);
      Source source = Source.newBuilder()
          .setDisplayName("Updated Display Name")
          .setName(sourceName.toString())
          .build();

      // Set the update mask to specify which properties should be updated.
      // If empty, all mutable fields will be updated.
      // For more info on constructing field mask path, see the proto or:
      // https://cloud.google.com/java/docs/reference/protobuf/latest/com.google.protobuf.FieldMask
      FieldMask updateMask = FieldMask.newBuilder()
          .addPaths("display_name")
          .build();

      UpdateSourceRequest request = UpdateSourceRequest.newBuilder()
          .setSource(source)
          .setUpdateMask(updateMask)
          .build();

      // Call the API.
      Source response = client.updateSource(request);

      System.out.println("Updated Source: " + response);
      return response;
    }
  }
}
// [END securitycenter_update_source_v2]
