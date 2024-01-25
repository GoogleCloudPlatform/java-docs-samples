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

package v2.findings;

// [START securitycenter_list_all_findings_v2]

import com.google.cloud.securitycenter.v2.ListFindingsRequest;
import com.google.cloud.securitycenter.v2.ListFindingsResponse.ListFindingsResult;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import java.io.IOException;

public class ListAllFindings {

  public static void main(String[] args) throws IOException {
    // projectId: The source to list all findings for.
    // You can also use organization/ folder as the parent resource.
    String projectId = "google-cloud-project-id";

    // Specify the DRZ location to list the findings.
    // Available locations: "us", "eu", "global".
    String location = "global";

    listAllFindings(projectId, location);
  }

  // List all findings under a given parent resource.
  public static void listAllFindings(String projectId, String location) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      ListFindingsRequest request =
          ListFindingsRequest.newBuilder()
              // "-" Indicates listing across all sources.
              .setParent(
                  String.format("projects/%s/sources/%s/locations/%s", projectId, "-",
                      location))
              .build();

      for (ListFindingsResult result : client.listFindings(request).iterateAll()) {
        System.out.printf("Finding: %s", result.getFinding().getName());
      }
      System.out.println("Listing complete.");
    }
  }
}
// [END securitycenter_list_all_findings_v2]
