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

// [START securitycenter_list_all_findings_v2]

import com.google.cloud.securitycenter.v2.ListFindingsRequest;
import com.google.cloud.securitycenter.v2.ListFindingsResponse.ListFindingsResult;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import java.io.IOException;

public class ListAllFindings {

  public static void main(String[] args) throws IOException {
    // organizationId: The source to list all findings for.
    // You can also use project/ folder as the parent resource.
    String organizationId = "google-cloud-organization-id";

    // Specify the location to list the findings.
    String location = "global";

    // The source id to scope the findings.
    String sourceId = "source-id";

    listAllFindings(organizationId, sourceId, location);
  }

  // List all findings under a given parent resource.
  public static void listAllFindings(String organizationId, String sourceId, String location)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      ListFindingsRequest request =
          ListFindingsRequest.newBuilder()
              // To list findings across all sources, use "-".
              .setParent(
                  String.format("organizations/%s/sources/%s/locations/%s", organizationId,
                      sourceId,
                      location))
              .build();

      for (ListFindingsResult result : client.listFindings(request).iterateAll()) {
        System.out.printf("Finding: %s", result.getFinding().getName());
      }
      System.out.println("\nListing complete.");
    }
  }
}
// [END securitycenter_list_all_findings_v2]
