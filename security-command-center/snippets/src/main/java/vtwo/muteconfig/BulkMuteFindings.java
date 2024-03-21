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

package vtwo.muteconfig;

// [START securitycenter_bulk_mute_v2]

import com.google.cloud.securitycenter.v2.BulkMuteFindingsRequest;
import com.google.cloud.securitycenter.v2.BulkMuteFindingsResponse;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class BulkMuteFindings {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO: Replace the variables within {}
    // projectId: Google Cloud Project id.
    String projectId = "google-cloud-project-id";

    // Specify the location of the mute configs.
    String location = "global";

    // muteRule: Expression that identifies findings that should be muted.
    // Can also refer to an organization/ folder.
    // eg: "resource.project_display_name=\"PROJECT_ID\""
    String muteRule = "resource.project_display_name=\"" + projectId + "\"";

    bulkMute(projectId, location, muteRule);
  }

  // Kicks off a long-running operation (LRO) to bulk mute findings for a parent based on a filter.
  // The parent can be either an organization, folder, or project. The findings
  // matched by the filter will be muted after the LRO is done.
  public static void bulkMute(String projectId, String location, String muteRule)
      throws IOException, ExecutionException, InterruptedException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      BulkMuteFindingsRequest bulkMuteFindingsRequest =
          BulkMuteFindingsRequest.newBuilder()
              // The parent can also be one of:
              //  * "organizations/{org_id}/locations/{location}"
              //  * "folder/{folder_id}/locations/{location}"
              .setParent(String.format("projects/%s/locations/%s", projectId, location))
              // To create mute rules, see:
              // https://cloud.google.com/security-command-center/docs/how-to-mute-findings#create_mute_rules
              .setFilter(muteRule)
              .build();

      // ExecutionException is thrown if the below call fails.
      BulkMuteFindingsResponse response =
          client.bulkMuteFindingsAsync(bulkMuteFindingsRequest).get();
      System.out.println("Bulk mute findings completed successfully! " + response);
    }
  }
}
// [END securitycenter_bulk_mute_v2]
