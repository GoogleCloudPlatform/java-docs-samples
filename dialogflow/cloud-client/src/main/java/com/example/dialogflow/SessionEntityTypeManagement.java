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
import com.google.cloud.dialogflow.v2.EntityType.Entity;
import com.google.cloud.dialogflow.v2.SessionEntityType;
import com.google.cloud.dialogflow.v2.SessionEntityType.EntityOverrideMode;
import com.google.cloud.dialogflow.v2.SessionEntityTypeName;
import com.google.cloud.dialogflow.v2.SessionEntityTypesClient;
import com.google.cloud.dialogflow.v2.SessionName;

import java.util.ArrayList;
import java.util.List;
// [END dialogflow_import_libraries]

/**
 * DialogFlow API SessionEntityType sample.
 */
public class SessionEntityTypeManagement {

  /**
   * List session entity types
   * @param projectId Project/Agent Id.
   * @param sessionId Identifier of the DetectIntent session.
   */
  public static void listSessionEntityTypes(String projectId, String sessionId) throws Exception {
    // Instantiates a client
    try (SessionEntityTypesClient sessionEntityTypesClient = SessionEntityTypesClient.create()) {
      // Set the session name using the sessionId (UUID) and projectID (my-project-id)
      SessionName session = SessionName.of(projectId, sessionId);

      System.out.format("SessionEntityTypes for session %s:\n", session.toString());
      // Performs the list session entity types request
      for (SessionEntityType sessionEntityType :
          sessionEntityTypesClient.listSessionEntityTypes(session).iterateAll()) {
        System.out.format("\tSessionEntityType name: %s\n", sessionEntityType.getName());
        System.out.format("\tNumber of entities: %d\n", sessionEntityType.getEntitiesCount());
      }
    }
  }

  // [START dialogflow_create_session_entity_type]
  /**
   * Create an entity type with the given display name
   * @param projectId Project/Agent Id.
   * @param sessionId Identifier of the DetectIntent session.
   * @param entityValues The entity values of the session entity type.
   * @param entityTypeDisplayName DISPLAY NAME of the entity type to be overridden in the session.
   * @param entityOverrideMode ENTITY_OVERRIDE_MODE_OVERRIDE (default) or
   *                           ENTITY_OVERRIDE_MODE_SUPPLEMENT
   */
  public static void createSessionEntityType(String projectId, String sessionId,
      List<String> entityValues, String entityTypeDisplayName,int entityOverrideMode)
      throws Exception {
    // Instantiates a client
    try (SessionEntityTypesClient sessionEntityTypesClient = SessionEntityTypesClient.create()) {
      // Set the session name using the sessionId (UUID) and projectID (my-project-id)
      SessionName session = SessionName.of(projectId, sessionId);
      SessionEntityTypeName name = SessionEntityTypeName.of(projectId, sessionId,
          entityTypeDisplayName);

      List<Entity> entities = new ArrayList<>();
      for (String entityValue : entityValues) {
        entities.add(Entity.newBuilder()
            .setValue(entityValue)
            .addSynonyms(entityValue)
            .build());
      }

      // Extends or replaces a developer entity type at the user session level (we refer to the
      // entity types defined at the agent level as "developer entity types").
      SessionEntityType sessionEntityType = SessionEntityType.newBuilder()
          .setName(name.toString())
          .addAllEntities(entities)
          .setEntityOverrideMode(EntityOverrideMode.forNumber(entityOverrideMode))
          .build();

      // Performs the create session entity type request
      SessionEntityType response = sessionEntityTypesClient.createSessionEntityType(session,
          sessionEntityType);

      System.out.format("SessionEntityType created: %s\n", response);
    }
  }
  // [END dialogflow_create_session_entity_type]

  // [START dialogflow_delete_session_entity_type]
  /**
   * Delete entity type with the given entity type name
   * @param projectId Project/Agent Id.
   * @param sessionId Identifier of the DetectIntent session.
   * @param entityTypeDisplayName DISPLAY NAME of the entity type to be overridden in the session.
   */
  public static void deleteSessionEntityType( String projectId, String sessionId,
      String entityTypeDisplayName) throws Exception {
    // Instantiates a client
    try (SessionEntityTypesClient sessionEntityTypesClient = SessionEntityTypesClient.create()) {
      SessionEntityTypeName name = SessionEntityTypeName.of(projectId, sessionId,
          entityTypeDisplayName);

      // Performs the delete session entity type request
      sessionEntityTypesClient.deleteSessionEntityType(name);
    }
  }
  // [END dialogflow_delete_session_entity_type]

  // [START run_application]
  public static void main(String[] args) throws Exception {
    String method = "";
    String sessionId = "";
    List<String> entityValues = new ArrayList<>();
    String entityTypeDisplayName = "";
    int entityOverrideMode = 1;
    String projectId = "";

    try {
      method = args[0];
      String command = args[1];
      if (command.equals("--projectId")) {
        projectId = args[2];
      }
      command = args[3];
      if (command.equals("--sessionId")) {
        sessionId = args[4];
      }

      if (method.equals("create")) {
        command = args[5];
        if (command.equals("--entityTypeDisplayName")) {
          entityTypeDisplayName = args[6];
        }

        for (int i = 7; i < args.length; i += 2) {
          if (args[i].equals("--entityOverrideMode")) {
            entityOverrideMode = Integer.valueOf(args[i + 1]);
          } else if (args[i].equals("--entityValues")) {
            while ((i + 1) < args.length && !args[(i + 1)].contains("--")) {
              entityValues.add(args[++i]);
            }
          }
        }
      } else if (method.equals("delete")) {
        command = args[5];
        if (command.equals("--entityTypeDisplayName")) {
          entityTypeDisplayName = args[6];
        }
      }
    } catch (Exception e) {
      System.out.println("Usage:");
      System.out.println("mvn exec:java -DSessionEntityTypeManagement "
          + "-Dexec.args='list --projectId PROJECT_ID --sessionId SESSION_ID'\n");

      System.out.println("mvn exec:java -DSessionEntityTypeManagement "
          + "-Dexec.args='create create  --projectId PROJECT_ID --sessionId SESSION_ID "
          + "--entityTypeDisplayName room --entityValues C D E F'\n");

      System.out.println("mvn exec:java -DSessionEntityTypeManagement "
          + "-Dexec.args='delete --projectId PROJECT_ID --sessionId SESSION_ID "
          + "--entityTypeDisplayName room'\n");

      System.out.println("Commands: list | create | delete");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println("\t--sessionId <sessionId> - Identifier of the DetectIntent session");

      System.out.println("Commands: create");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println("\t--sessionId <sessionId> - Identifier of the DetectIntent session");
      System.out.println("\t--entityTypeDisplayName <entityTypeDisplayName> - "
          + "The DISPLAY NAME of the entity type to be overridden in the session.");
      System.out.println("\nOptional Commands [For create]:");
      System.out.println("\t--entityOverrideMode <entityOverrideMode> - "
          + "ENTITY_OVERRIDE_MODE_OVERRIDE (default) or ENTITY_OVERRIDE_MODE_SUPPLEMENT");
      System.out.println("\t--entityValues <entityValues> - "
          + "The entity values of the session entity type.");

      System.out.println("Commands: delete");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println("\t--sessionId <sessionId> - Identifier of the DetectIntent session");
      System.out.println("\t--entityTypeDisplayName <entityTypeDisplayName> - "
          + "The DISPLAY NAME of the entity type to be overridden in the session.");
    }

    if (method.equals("list")) {
      listSessionEntityTypes(projectId, sessionId);
    } else if (method.equals("create")) {
      createSessionEntityType(projectId, sessionId, entityValues, entityTypeDisplayName,
          entityOverrideMode);
    } else if (method.equals("delete")) {
      deleteSessionEntityType(projectId, sessionId, entityTypeDisplayName);
    }
  }
  // [END run_application]
}
