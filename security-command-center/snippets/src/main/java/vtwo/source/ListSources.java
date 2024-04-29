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

// [START securitycenter_list_sources_v2]

import com.google.cloud.securitycenter.v2.OrganizationLocationName;
import com.google.cloud.securitycenter.v2.OrganizationName;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SecurityCenterClient.ListSourcesPagedResponse;
import com.google.cloud.securitycenter.v2.Source;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListSources {

  public static void main(String[] args) throws IOException {
    // TODO: Replace the below variables.
    // organizationId: Google Cloud Organization id.
    String organizationId = "{google-cloud-organization-id}";

    listSources(organizationId);
  }

  // Demonstrates how to list all security sources in an organization.
  public static List<Source> listSources(String organizationId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      // Start setting up a request to get a source.
      OrganizationName parent = OrganizationName.of(organizationId);

      // Call the API.
      List<Source> sourcesList = new ArrayList<>();
      ListSourcesPagedResponse response = client.listSources(parent);
      response.iterateAll().forEach(sourcesList::add);

      for (Source source : sourcesList) {
        System.out.println("List sources: " + source);
      }
      return sourcesList;
    }
  }
}
// [END securitycenter_list_sources_v2]
