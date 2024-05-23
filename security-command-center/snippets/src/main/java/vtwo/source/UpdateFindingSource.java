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

// [START securitycenter_update_finding_source_properties_v2]

import com.google.cloud.securitycenter.v2.Finding;
import com.google.cloud.securitycenter.v2.FindingName;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.UpdateFindingRequest;
import com.google.protobuf.FieldMask;
import com.google.protobuf.Timestamp;
import com.google.protobuf.Value;
import java.io.IOException;
import java.time.Instant;

public class UpdateFindingSource {

  public static void main(String[] args) throws IOException {
    // TODO: Replace the below variables.
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    // Specify the location to list the findings.
    String location = "global";

    // Specify the source-id.
    String sourceId = "{source-id}";

    // Specify the finding-id.
    String findingId = "{finding-id}";

    updateFinding(organizationId, location, sourceId, findingId);
  }

  // Creates or updates a finding.
  public static Finding updateFinding(String organizationId,
      String location, String sourceId, String findingId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      // Instead of using the FindingName, a plain String can also be used. E.g.:
      // String findingName = String.format("organizations/%s/sources/%s/locations/%s/findings/%s",
      // organizationId, sourceId, location, findingId);
      FindingName findingName = FindingName
          .ofOrganizationSourceLocationFindingName(organizationId, sourceId, location, findingId);

      // Use the current time as the finding "event time".
      Instant eventTime = Instant.now();

      // Define source properties values as protobuf "Value" objects.
      Value stringValue = Value.newBuilder().setStringValue("value").build();

      // Set the update mask to specify which properties should be updated.
      // If empty, all mutable fields will be updated.
      // For more info on constructing field mask path, see the proto or:
      // https://cloud.google.com/java/docs/reference/protobuf/latest/com.google.protobuf.FieldMask
      FieldMask updateMask =
          FieldMask.newBuilder()
              .addPaths("event_time")
              .addPaths("source_properties.stringKey")
              .build();

      Finding finding =
          Finding.newBuilder()
              .setName(findingName.toString())
              .setDescription("Updated finding source")
              .setEventTime(
                  Timestamp.newBuilder()
                      .setSeconds(eventTime.getEpochSecond())
                      .setNanos(eventTime.getNano()))
              .putSourceProperties("stringKey", stringValue)
              .build();

      UpdateFindingRequest request =
          UpdateFindingRequest.newBuilder()
              .setFinding(finding)
              .setUpdateMask(updateMask)
              .build();

      // Call the API.
      Finding response = client.updateFinding(request);

      System.out.println("Updated finding source: " + response);
      return response;
    }
  }
}
// [END securitycenter_update_finding_source_properties_v2]
