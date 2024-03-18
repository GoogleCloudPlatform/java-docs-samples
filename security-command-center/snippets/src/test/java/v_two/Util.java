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

package v_two;

import com.google.cloud.securitycenter.v2.CreateFindingRequest;
import com.google.cloud.securitycenter.v2.CreateSourceRequest;
import com.google.cloud.securitycenter.v2.Finding;
import com.google.cloud.securitycenter.v2.Finding.FindingClass;
import com.google.cloud.securitycenter.v2.Finding.Mute;
import com.google.cloud.securitycenter.v2.Finding.Severity;
import com.google.cloud.securitycenter.v2.Finding.State;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.Source;
import com.google.protobuf.Timestamp;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

public class Util {

  public static Source createSource(String organizationId) throws IOException {
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      Source source =
          Source.newBuilder()
              .setDisplayName("Custom display name")
              .setDescription("A source that does X")
              .build();

      CreateSourceRequest createSourceRequest =
          CreateSourceRequest.newBuilder()
              .setParent(String.format("organizations/%s", organizationId))
              .setSource(source)
              .build();

      Source response = client.createSource(createSourceRequest);
      System.out.println("Created source : " + response.getName());
      return response;
    }
  }

  public static Finding createFinding(String sourceName, String findingId, String location,
      Optional<String> category) throws IOException {
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      Instant eventTime = Instant.now();
      // The resource this finding applies to. The Cloud Security Command Center UI can link
      // the findings for a resource to the corresponding asset of a resource
      // if there are matches.
      // TODO(Developer): Replace the sample resource name
      String resourceName = "//cloudresourcemanager.googleapis.com/organizations/11232";

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
          .setParent(String.format("%s/locations/%s", sourceName, location))
          .setFinding(finding)
          .setFindingId(findingId)
          .build();

      Finding response = client.createFinding(createFindingRequest);

      System.out.println("Created Finding: " + response);
      return response;
    }
  }
}
