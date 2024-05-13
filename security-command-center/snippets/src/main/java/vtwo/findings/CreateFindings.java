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
import com.google.cloud.securitycenter.v2.SourceName;
import com.google.protobuf.Timestamp;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class CreateFindings {

  public static void main(String[] args) throws IOException {
    // TODO: Replace the sample resource name
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    // Specify the location to list the findings.
    String location = "global";

    // The source id corresponding to the finding.
    String sourceId = "{source-id}";

    // The finding id.
    String findingId = "testfindingv2" + UUID.randomUUID().toString().split("-")[0];

    // Specify the category.
    Optional<String> category = Optional.of("MEDIUM_RISK_ONE");

    createFinding(organizationId, location, findingId, sourceId, category);
  }

  /**
   * Creates a security finding within a specific source in the Security Command Center.
   */
  public static Finding createFinding(String organizationId, String location, String findingId,
      String sourceId, Optional<String> category) throws IOException {
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      // Optionally SourceName or String can be used.
      // String sourceName = String.format("organizations/%s/sources/%s", organizationId, sourceId);
      SourceName sourceName = SourceName.of(organizationId, sourceId);

      Instant eventTime = Instant.now();
      // The resource this finding applies to. The Cloud Security Command Center UI can link the
      // findings for a resource to the corresponding asset of a resource if there are matches.
      String resourceName = String.format("//cloudresourcemanager.googleapis.com/organizations/%s",
          organizationId);

      // Set up a request to create a finding in a source.
      String parent = String.format("%s/locations/%s", sourceName.toString(), location);
      Finding finding = Finding.newBuilder()
          .setParent(parent)
          .setState(State.ACTIVE)
          .setSeverity(Severity.LOW)
          .setMute(Mute.UNMUTED)
          .setFindingClass(FindingClass.OBSERVATION)
          .setResourceName(resourceName)
          .setEventTime(Timestamp.newBuilder()
              .setSeconds(eventTime.getEpochSecond())
              .setNanos(eventTime.getNano()))
          .setCategory(category.orElse("LOW_RISK_ONE"))
          .build();

      CreateFindingRequest createFindingRequest = CreateFindingRequest.newBuilder()
          .setParent(parent)
          .setFindingId(findingId)
          .setFinding(finding).build();

      // Call the API.
      Finding response = client.createFinding(createFindingRequest);
      return response;
    }
  }
}
// [END securitycenter_create_findings_v2]
