/*
  Copyright 2017, Google, Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package com.example.pubsub;

// [START cloudiotcore_add_service_account]
// Imports the Google Cloud client library
import com.google.cloud.Identity;
import com.google.cloud.Role;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;
import com.google.pubsub.v1.TopicName;

public class AddCloudIotService {

  public static void main(String... args) throws Exception {
    if (args.length < 2) {
      System.out.printf("Usage:\n    java %s <topicId> <projectId>\n\n",
              AddCloudIotService.class.getCanonicalName());
      return;
    }

    String topicId = args[0];
    String projectId = args[1]; // Could make optional, use: ServiceOptions.getDefaultProjectId();

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      String topicName = TopicName.create(projectId, topicId).toString();
      Policy policy = topicAdminClient.getIamPolicy(topicName);
      // add role -> members binding
      Binding binding =
              Binding.newBuilder()
                      .setRole(Role.editor().toString())
                      .addMembers(Identity.serviceAccount("cloud-iot@system.gserviceaccount.com")
                              .toString())
                      .build();
      // Add Cloud IoT Core service account.
      Policy updatedPolicy = Policy.newBuilder(policy).addBindings(binding).build();
      topicAdminClient.setIamPolicy(topicName, updatedPolicy);
    }
  }
}
// [END cloudiotcore_add_service_account]
