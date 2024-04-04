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

package vtwo.findings;

// [START securitycenter_create_findings_v2]

import com.google.cloud.securitycenter.v2.CreateFindingRequest;
import com.google.cloud.securitycenter.v2.Finding;
import com.google.cloud.securitycenter.v2.Finding.FindingClass;
import com.google.cloud.securitycenter.v2.Finding.Mute;
import com.google.cloud.securitycenter.v2.Finding.Severity;
import com.google.cloud.securitycenter.v2.Finding.State;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.protobuf.Timestamp;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class CreateFindings {

  public static void main(String[] args) throws IOException {
    // TODO(Developer): Replace the sample resource name
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    // The source id corresponding to the finding.
    String sourceId = "{source-id}";

    // The finding id.
    String findingId = "testfindingv2" + UUID.randomUUID().toString().split("-")[0];

    // Specify the location.
    String location = "global";

    // Specify the category.
    Optional<String> category = Optional.of("MEDIUM_RISK_ONE");

    createFinding(organizationId, findingId, location, sourceId, category);
  }

  // Demonstrates how to create a new finding
  public static Finding createFinding(String organizationId, String findingId, String location,
      String sourceId, Optional<String> category) throws IOException {
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      // SourceName sourceName = SourceName.of(/*organization=*/"123234324",/*source=*"423432321");
      String sourceName = String.format("organizations/%s/sources/%s/locations/%s", organizationId,
          sourceId, location);

      Instant eventTime = Instant.now();
      // The resource this finding applies to. The Cloud Security Command Center UI can link
      // the findings for a resource to the corresponding asset of a resource
      // if there are matches.
      String resourceName = String.format("//cloudresourcemanager.googleapis.com/organizations/%s",
          organizationId);

      // Set up a request to create a finding in a source.
      Finding finding =
          Finding.newBuilder()
              .setParent(sourceName)
              .setState(State.ACTIVE)
              .setSeverity(Severity.LOW)
              .setMute(Mute.UNMUTED)
              .setFindingClass(FindingClass.OBSERVATION)
              .setResourceName(resourceName)
              .setEventTime(
                  Timestamp.newBuilder()
                      .setSeconds(eventTime.getEpochSecond())
                      .setNanos(eventTime.getNano()))
              .setCategory(category.orElse("LOW_RISK_ONE"))
              .build();

      CreateFindingRequest createFindingRequest = CreateFindingRequest.newBuilder()
          .setParent(sourceName)
          .setFindingId(findingId)
          .setFinding(finding)
          .build();

      // Call the API.
      Finding response = client.createFinding(createFindingRequest);

      System.out.println("Created Finding: " + response);
      return response;
    }
  }
}
// [START securitycenter_create_findings_v2]
