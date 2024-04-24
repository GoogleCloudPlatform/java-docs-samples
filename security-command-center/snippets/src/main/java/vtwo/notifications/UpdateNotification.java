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

// [START securitycenter_update_notification_config]

package vtwo.notifications;

import com.google.cloud.securitycenter.v2.NotificationConfig;
import com.google.cloud.securitycenter.v2.NotificationConfig.StreamingConfig;
import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.protobuf.FieldMask;
import java.io.IOException;
import java.util.UUID;

public class UpdateNotification {

  public static void main(String[] args) throws IOException {
    // parentId: must be in one of the following formats:
    //    "organizations/{organization_id}"
    //    "projects/{project_id}"
    //    "folders/{folder_id}"
    String parentId = "{parent-id}";
    String topicName = "{your-topic}";
    String notificationConfigId = "{your-notification-id}";
    // Specify the location to list the findings.
    String location = "global";

    updateNotificationConfig(parentId, location, topicName, notificationConfigId);
  }

  // Update an existing notification config.
  // If updating a Pubsub Topic, ensure the ServiceAccount has the
  // "pubsub.topics.setIamPolicy" permission on the new topic.
  public static NotificationConfig updateNotificationConfig(
      String parentId, String location, String topicName, String notificationConfigId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecurityCenterClient client = SecurityCenterClient.create()) {

      String notificationConfigName =
          String.format("projects/%s/locations/%s/notificationConfigs/%s",
              parentId,
              location,
              notificationConfigId);

      String pubsubTopic =
          String.format("projects/%s/topics/%s",
              parentId,
              topicName);

      NotificationConfig configToUpdate =
          NotificationConfig.newBuilder()
              .setName(notificationConfigName)
              .setDescription("updated description")
              .setPubsubTopic(pubsubTopic)
              .setStreamingConfig(StreamingConfig.newBuilder().setFilter("state = \"ACTIVE\""))
              .build();

      FieldMask fieldMask =
          FieldMask.newBuilder()
              .addPaths("description")
              .addPaths("pubsub_topic")
              .addPaths("streaming_config.filter")
              .build();

      NotificationConfig updatedConfig = client.updateNotificationConfig(configToUpdate, fieldMask);

      System.out.printf("Notification config: %s%n", updatedConfig);
      return updatedConfig;
    }
  }
}
// [END securitycenter_update_notification_config]
