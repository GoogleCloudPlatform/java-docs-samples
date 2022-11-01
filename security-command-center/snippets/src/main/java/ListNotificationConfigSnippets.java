/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// [START securitycenter_list_notification_configs]

import com.google.cloud.securitycenter.v1.NotificationConfig;
import com.google.cloud.securitycenter.v1.SecurityCenterClient;
import com.google.cloud.securitycenter.v1.SecurityCenterClient.ListNotificationConfigsPagedResponse;
import com.google.common.collect.ImmutableList;
import java.io.IOException;

public class ListNotificationConfigSnippets {

  public static void main(String[] args) throws IOException {
    // parentId: must be in one of the following formats:
    //    "organizations/{organization_id}"
    //    "projects/{project_id}"
    //    "folders/{folder_id}"
    String parentId = String.format("organizations/%s", "ORG_ID");

    listNotificationConfigs(parentId);
  }

  // List notification configs present in the given parent.
  public static ImmutableList<NotificationConfig> listNotificationConfigs(String parentId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      ListNotificationConfigsPagedResponse response = client.listNotificationConfigs(parentId);

      ImmutableList<NotificationConfig> notificationConfigs =
          ImmutableList.copyOf(response.iterateAll());

      System.out.printf("List notifications response: %s%n", response.getPage().getValues());
      return notificationConfigs;
    }
  }
}
// [END securitycenter_list_notification_configs]