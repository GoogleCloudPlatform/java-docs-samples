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

package com.example.dialogflow;

// [START dialogflow_import_libraries]
// Imports the Google Cloud client library
import com.google.cloud.dialogflow.v2.Context;
import com.google.cloud.dialogflow.v2.Intent;
import com.google.cloud.dialogflow.v2.Intent.Message;
import com.google.cloud.dialogflow.v2.Intent.Message.Text;
import com.google.cloud.dialogflow.v2.Intent.TrainingPhrase;
import com.google.cloud.dialogflow.v2.Intent.TrainingPhrase.Part;
import com.google.cloud.dialogflow.v2.IntentName;
import com.google.cloud.dialogflow.v2.IntentsClient;
import com.google.cloud.dialogflow.v2.ProjectAgentName;

import java.util.ArrayList;
import java.util.List;
// [END dialogflow_import_libraries]

/**
 * DialogFlow API Intent sample.
 */
public class IntentManagement {

  // [START dialogflow_list_intents]
  /**
   * List intents
   * @param projectId Project/Agent Id.
   */
  public static void listIntents(String projectId) throws Exception {
    // Instantiates a client
    try (IntentsClient intentsClient = IntentsClient.create()) {
      // Set the project agent name using the projectID (my-project-id)
      ProjectAgentName parent = ProjectAgentName.of(projectId);

      // Performs the list intents request
      for (Intent intent : intentsClient.listIntents(parent).iterateAll()) {
        System.out.println("====================");
        System.out.format("Intent name: '%s'\n", intent.getName());
        System.out.format("Intent display name: '%s'\n", intent.getDisplayName());
        System.out.format("Action: '%s'\n", intent.getAction());
        System.out.format("Root followup intent: '%s'\n", intent.getRootFollowupIntentName());
        System.out.format("Parent followup intent: '%s'\n", intent.getParentFollowupIntentName());

        System.out.format("Input contexts:\n");
        for (String inputContextName : intent.getInputContextNamesList()) {
          System.out.format("\tName: %s\n", inputContextName);
        }

        System.out.format("Output contexts:\n");
        for (Context outputContext : intent.getOutputContextsList()) {
          System.out.format("\tName: %s\n", outputContext.getName());
        }
      }
    }
  }
  // [END dialogflow_list_intents]

  // [START dialogflow_create_intent]
  /**
   * Create an intent of the given intent type
   * @param displayName The display name of the intent.
   * @param projectId Project/Agent Id.
   * @param trainingPhrasesParts Training phrases.
   * @param messageTexts Message texts for the agent's response when the intent is detected.
   */
  public static void createIntent(String displayName, String projectId,
      List<String> trainingPhrasesParts, List<String> messageTexts)
      throws Exception {
    // Instantiates a client
    try (IntentsClient intentsClient = IntentsClient.create()) {
      // Set the project agent name using the projectID (my-project-id)
      ProjectAgentName parent = ProjectAgentName.of(projectId);

      // Build the trainingPhrases from the trainingPhrasesParts
      List<TrainingPhrase> trainingPhrases = new ArrayList<>();
      for (String trainingPhrase : trainingPhrasesParts) {
        trainingPhrases.add(
            TrainingPhrase.newBuilder().addParts(
                    Part.newBuilder().setText(trainingPhrase).build())
                .build());
      }

      // Build the message texts for the agent's response
      Message message = Message.newBuilder()
          .setText(
              Text.newBuilder()
                  .addAllText(messageTexts).build()
          ).build();

      // Build the intent
      Intent intent = Intent.newBuilder()
          .setDisplayName(displayName)
          .addMessages(message)
          .addAllTrainingPhrases(trainingPhrases)
          .build();

      // Performs the create intent request
      Intent response = intentsClient.createIntent(parent, intent);
      System.out.format("Intent created: %s\n", response);
    }
  }
  // [END dialogflow_create_intent]

  // [START dialogflow_delete_intent]
  /**
   * Delete intent with the given intent type and intent value
   * @param intentId The id of the intent.
   * @param projectId Project/Agent Id.
   */
  public static void deleteIntent(String intentId, String projectId) throws Exception {
    // Instantiates a client
    try (IntentsClient intentsClient = IntentsClient.create()) {
      IntentName name = IntentName.of(projectId, intentId);
      // Performs the delete intent request
      intentsClient.deleteIntent(name);
    }
  }
  // [END dialogflow_delete_intent]

  /**
   * Helper method for testing to get intentIds from displayName.
   */
  public static List<String> getIntentIds(String displayName, String projectId) throws Exception {
    List<String> intentIds = new ArrayList<>();

    // Instantiates a client
    try (IntentsClient intentsClient = IntentsClient.create()) {
      ProjectAgentName parent = ProjectAgentName.of(projectId);
      for (Intent intent : intentsClient.listIntents(parent).iterateAll()) {
        if (intent.getDisplayName().equals(displayName)) {
          String[] splitName = intent.getName().split("/");
          intentIds.add(splitName[splitName.length - 1]);
        }
      }
    }

    return intentIds;
  }

  public static void main(String[] args) throws Exception {
    String method = "";
    String displayName = "";
    String intentId = "";
    List<String> messageTexts = new ArrayList<>();
    List<String> trainingPhrasesParts = new ArrayList<>();
    String projectId = "";

    try {
      method = args[0];
      String command = args[1];
      if (method.equals("list")) {
        if (command.equals("--projectId")) {
          projectId = args[2];
        }
      } else if (method.equals("create")) {
        displayName = args[1];
        command = args[2];
        if (command.equals("--projectId")) {
          projectId = args[3];
        }

        for (int i = 4; i < args.length; i += 2) {
          if (args[i].equals("--messageTexts")) {
            while ((i + 1) < args.length && !args[(i + 1)].contains("--")) {
              messageTexts.add(args[++i]);
            }
          } else if (args[i].equals("--trainingPhrasesParts")) {
            while ((i + 1) < args.length && !args[(i + 1)].contains("--")) {
              trainingPhrasesParts.add(args[++i]);
            }
          }
        }
      } else if (method.equals("delete")) {
        intentId = args[1];
        command = args[2];
        if (command.equals("--projectId")) {
          projectId = args[3];
        }
      }
    } catch (Exception e) {
      System.out.println("Usage:");
      System.out.println("mvn exec:java -DIntentManagement "
          + "-Dexec.args='list --projectId PROJECT_ID'\n");

      System.out.println("mvn exec:java -DIntentManagement "
          + "-Dexec.args='create \"room.cancellation - yes\" --projectId PROJECT_ID "
          + "--trainingPhrasesParts \"cancel\" \"cancellation\""
          + "--messageTexts \"Are you sure you want to cancel?\" \"Cancelled.\"'\n");

      System.out.println("mvn exec:java -DIntentManagement "
          + "-Dexec.args='delete INTENT_ID --projectId PROJECT_ID'\n");

      System.out.println("Commands: list");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");

      System.out.println("Commands: create");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println("\t<displayName> - [For create] The display name of the intent.");
      System.out.println("\nOptional Commands [For create]:");
      System.out.println("\t--trainingPhrasesParts <trainingPhrasesParts> - Training phrases.");
      System.out.println("\t--messageTexts <messageTexts> - Message texts for the agent's "
          + "response when the intent is detected.");

      System.out.println("Commands: delete");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println("\t<intentId> - [For delete] The id of the intent.");
    }

    if (method.equals("list")) {
      listIntents(projectId);
    } else if (method.equals("create")) {
      createIntent(displayName, projectId, trainingPhrasesParts, messageTexts);
    } else if (method.equals("delete")) {
      deleteIntent(intentId, projectId);
    }
  }
}
