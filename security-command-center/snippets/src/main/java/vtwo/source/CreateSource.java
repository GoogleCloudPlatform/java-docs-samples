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

// [START securitycenter_create_source_v2]

import com.google.cloud.securitycenter.v2.CreateSourceRequest;
import com.google.cloud.securitycenter.v2.OrganizationName;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.Source;
import java.io.IOException;

public class CreateSource {

  public static void main(String[] args) throws IOException {
    // TODO: Replace the sample resource name
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    createSource(organizationId);
  }

  /**
   * Creates a new "source" in the Security Command Center.
   */
  public static Source createSource(String organizationId) throws IOException {
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      // Start setting up a request to create a source in an organization.
      OrganizationName organizationName = OrganizationName.of(organizationId);

      Source source =
          Source.newBuilder()
              .setDisplayName("Custom display name")
              .setDescription("A source that does X")
              .build();

      CreateSourceRequest createSourceRequest =
          CreateSourceRequest.newBuilder()
              .setParent(organizationName.toString())
              .setSource(source)
              .build();

      // The source is not visible in the Security Command Center dashboard
      // until it generates findings.
      Source response = client.createSource(createSourceRequest);
      return response;
    }
  }
}
// [END securitycenter_create_source_v2]
