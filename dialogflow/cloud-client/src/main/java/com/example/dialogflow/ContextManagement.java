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
import com.google.cloud.dialogflow.v2.ContextName;
import com.google.cloud.dialogflow.v2.ContextsClient;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.protobuf.Value;

import java.util.Map.Entry;
// [END dialogflow_import_libraries]

/**
 * DialogFlow API Context sample.
 */
public class ContextManagement {

  /**
   * Lists contexts
   * @param sessionId Identifier of the DetectIntent session.
   * @param projectId Project/Agent Id.
   */
  public static void listContexts(String sessionId, String projectId) throws Exception {
    // Instantiates a client
    try (ContextsClient contextsClient = ContextsClient.create()) {
      // Set the session name using the sessionId (UUID) and projectId (my-project-id)
      SessionName session = SessionName.of(projectId, sessionId);

      // Performs the list contexts request
      System.out.format("Contexts for session %s:\n", session.toString());
      for (Context context : contextsClient.listContexts(session).iterateAll()) {
        System.out.format("Context name: %s\n", context.getName());
        System.out.format("Lifespan Count: %d\n", context.getLifespanCount());
        System.out.format("Fields:\n");
        for (Entry<String, Value> entry : context.getParameters().getFieldsMap().entrySet()) {
          if (entry.getValue().hasStructValue()) {
            System.out.format("\t%s: %s\n", entry.getKey(), entry.getValue());
          }
        }
      }
    }
  }

  // [START dialogflow_create_context]
  /**
   * Create an entity type with the given display name
   * @param contextId  The Id of the context.
   * @param sessionId Identifier of the DetectIntent session.
   * @param lifespanCount The lifespan count of the context.
   * @param projectId Project/Agent Id.
   */
  public static void createContext(String contextId, String sessionId, String projectId,
      int lifespanCount) throws Exception {
    // Instantiates a client
    try (ContextsClient contextsClient = ContextsClient.create()) {
      // Set the session name using the sessionId (UUID) and projectID (my-project-id)
      SessionName session = SessionName.of(projectId, sessionId);

      // Create the context name with the projectId, sessionId, and contextId
      ContextName contextName = ContextName.newBuilder()
          .setProject(projectId)
          .setSession(sessionId)
          .setContext(contextId)
          .build();

      // Create the context with the context name and lifespan count
      Context context = Context.newBuilder()
          .setName(contextName.toString()) // The unique identifier of the context
          .setLifespanCount(lifespanCount) // Number of query requests before the context expires.
          .build();

      // Performs the create context request
      Context response = contextsClient.createContext(session, context);
      System.out.format("Context created: %s\n", response);
    }
  }
  // [END dialogflow_create_context]

  // [START dialogflow_delete_context]
  /**
   * Delete entity type with the given entity type name
   * @param contextId The Id of the context.
   * @param sessionId Identifier of the DetectIntent session.
   * @param projectId Project/Agent Id.
   */
  public static void deleteContext(String contextId, String sessionId, String projectId)
      throws Exception {
    // Instantiates a client
    try (ContextsClient contextsClient = ContextsClient.create()) {
      // Create the context name with the projectId, sessionId, and contextId
      ContextName contextName = ContextName.of(projectId, sessionId, contextId);
      // Performs the delete context request
      contextsClient.deleteContext(contextName);
    }
  }
  // [END dialogflow_delete_context]

  // [START run_application]
  public static void main(String[] args) throws Exception {
    String method = "";
    String sessionId = "";
    String contextId = "";
    int lifeSpanCount = 1;
    String projectId = "";

    try {
      method = args[0];
      String command = args[1];
      if (method.equals("list")) {
        if (command.equals("--sessionId")) {
          sessionId = args[2];
        }

        command = args[3];
        if (command.equals("--projectId")) {
          projectId = args[4];
        }
      } else if (method.equals("create") || method.equals("delete")) {
        if (command.equals("--sessionId")) {
          sessionId = args[2];
        }

        command = args[3];
        if (command.equals("--projectId")) {
          projectId = args[4];
        }

        command = args[5];
        if (command.equals("--contextId")) {
          contextId = args[6];
        }

        if (method.equals("create") && args.length > 7) {
          command = args[7];
          if (command.equals("--lifespanCount")) {
            lifeSpanCount = Integer.valueOf(args[8]);
          }
        }
      }
    } catch (Exception e) {
      System.out.println("Usage:");
      System.out.println("mvn exec:java -DContextManagement "
          + "-Dexec.args='list --sessionId SESSION_ID --projectId PROJECT_ID'\n");

      System.out.println("mvn exec:java -DContextManagement "
          + "-Dexec.args='create --sessionId SESSION_ID --projectId PROJECT_ID "
          + "--contextId CONTEXT_ID'\n");

      System.out.println("mvn exec:java -DContextManagement "
          + "-Dexec.args='delete --sessionId SESSION_ID --projectId PROJECT_ID "
          + "--contextId CONTEXT_ID'\n");

      System.out.println("Commands: list | delete");
      System.out.println("\t--sessionId <sessionId> - Identifier of the DetectIntent session");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println("\nCommands: create");
      System.out.println("\t--sessionId <sessionId> - Identifier of the DetectIntent session");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println("\t--contextId <contextId> - The Id of the context");
      System.out.println("\nOptional Commands [For create]:");
      System.out.println("\t--lifespanCount <lifespanCount> - The lifespan count of the context "
          + "(Defaults to 1.)");
    }

    if (method.equals("list")) {
      listContexts(sessionId, projectId);
    } else if (method.equals("create")) {
      createContext(contextId, sessionId, projectId, lifeSpanCount);
    } else if (method.equals("delete")) {
      deleteContext(contextId, sessionId, projectId);
    }
  }
  // [END run_application]
}
