/*
 * Copyright 2022 Google LLC
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

// [START securitycenter_create_notification_config]

import com.google.cloud.securitycenter.v1.CreateNotificationConfigRequest;
import com.google.cloud.securitycenter.v1.NotificationConfig;
import com.google.cloud.securitycenter.v1.NotificationConfig.StreamingConfig;
import com.google.cloud.securitycenter.v1.SecurityCenterClient;
import java.io.IOException;

public class CreateNotificationConfigSnippets {

  public static void main(String[] args) throws IOException {
    // parentId: must be in one of the following formats:
    //    "organizations/{organization_id}"
    //    "projects/{project_id}"
    //    "folders/{folder_id}"
    String parentId = String.format("organizations/%s", "ORG_ID");
    String notificationConfigId = "{config-id}";
    String projectId = "{your-project}";
    String topicName = "{your-topic}";

    createNotificationConfig(parentId, notificationConfigId, projectId, topicName);
  }

  // Crete a notification config.
  // Ensure the ServiceAccount has the "pubsub.topics.setIamPolicy" permission on the new topic.
  public static NotificationConfig createNotificationConfig(
      String parentId, String notificationConfigId, String projectId, String topicName)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      // Ensure this ServiceAccount has the "pubsub.topics.setIamPolicy" permission on the topic.
      String pubsubTopic = String.format("projects/%s/topics/%s", projectId, topicName);

      CreateNotificationConfigRequest request =
          CreateNotificationConfigRequest.newBuilder()
              .setParent(parentId)
              .setConfigId(notificationConfigId)
              .setNotificationConfig(
                  NotificationConfig.newBuilder()
                      .setDescription("Java notification config")
                      .setPubsubTopic(pubsubTopic)
                      .setStreamingConfig(
                          StreamingConfig.newBuilder().setFilter("state = \"ACTIVE\"").build())
                      .build())
              .build();

      NotificationConfig response = client.createNotificationConfig(request);
      System.out.printf("Notification config was created: %s%n", response);
      return response;
    }
  }
}
// [END securitycenter_create_notification_config]
