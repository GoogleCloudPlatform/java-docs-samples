/*
 * Copyright 2024 Google LLC
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

package vtwo.notifications;

import com.google.cloud.securitycenter.v2.LocationName;
import com.google.cloud.securitycenter.v2.NotificationConfig;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import java.io.IOException;

public class CreateNotification {

  public static void main(String[] args) throws IOException {
    // parentId: must be in one of the following formats:
    //    "organizations/{organization_id}"
    //    "projects/{project_id}"
    //    "folders/{folder_id}"
    String parentId = "{parent-id}";
    String topicName = "{your-topic}";
    String notificationConfigId = "{your-notification-id}";
    // Specify the location of the notification config.
    String location = "global";

    createNotificationConfig(parentId, location, topicName, notificationConfigId);
  }

  // Crete a notification config.
  // Ensure the ServiceAccount has the "pubsub.topics.setIamPolicy" permission on the new topic.
  public static NotificationConfig createNotificationConfig(
      String parentId, String location, String topicName, String notificationConfigId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      String pubsubTopic = String.format("projects/%s/topics/%s", parentId, topicName);

      NotificationConfig notificationConfig = NotificationConfig.newBuilder()
          .setDescription("Java notification config")
          .setPubsubTopic(pubsubTopic)
          .setStreamingConfig(
              NotificationConfig.StreamingConfig.newBuilder().setFilter("state = \"ACTIVE\"")
                  .build())
          .build();

      NotificationConfig response = client.createNotificationConfig(
          LocationName.of(parentId, location), notificationConfig, notificationConfigId);

      System.out.printf("Notification config was created: %s%n", response);
      return response;
    }
  }
}
// [END securitycenter_create_notification_config]
