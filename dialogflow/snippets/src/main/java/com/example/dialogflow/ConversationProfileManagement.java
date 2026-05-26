/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.dialogflow;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.dialogflow.v2.ConversationProfile;
import com.google.cloud.dialogflow.v2.ConversationProfilesClient;
import com.google.cloud.dialogflow.v2.CreateConversationProfileRequest;
import com.google.cloud.dialogflow.v2.LocationName;
import java.io.IOException;

public class ConversationProfileManagement {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";
    String location = "my-location";

    // Set display name of the new conversation profile
    String conversationProfileDisplayName = "my-conversation-profile-display-name";

    // Create a conversation profile
    createConversationProfile(projectId, conversationProfileDisplayName, location);
  }

  // Create a basic conversation profile.
  public static void createConversationProfile(
      String projectId,
      String displayName,
      String location) throws ApiException, IOException {
    try (ConversationProfilesClient conversationProfilesClient =
        ConversationProfilesClient.create()) {
      LocationName locationName = LocationName.of(projectId, location);
      ConversationProfile targetConversationProfile =
          ConversationProfile.newBuilder()
              .setDisplayName(displayName)
              .setLanguageCode("en-US")
              .build();

      ConversationProfile createdConversationProfile =
          conversationProfilesClient.createConversationProfile(
              CreateConversationProfileRequest.newBuilder()
                  .setParent(locationName.toString())
                  .setConversationProfile(targetConversationProfile)
                  .build());
      System.out.println("====================");
      System.out.println("Conversation Profile created:\n");
      System.out.format("Display name: %s\n", createdConversationProfile.getDisplayName());
      System.out.format("Name: %s\n", createdConversationProfile.getName());
    }
  }
}
