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
import com.google.cloud.dialogflow.v2.EntityType;
import com.google.cloud.dialogflow.v2.EntityType.Kind;
import com.google.cloud.dialogflow.v2.EntityTypeName;
import com.google.cloud.dialogflow.v2.EntityTypesClient;
import com.google.cloud.dialogflow.v2.ProjectAgentName;

import java.util.ArrayList;
import java.util.List;
// [END dialogflow_import_libraries]

/**
 * DialogFlow API EntityType sample.
 */
public class EntityTypeManagement {

  /**
   * List entity types
   * @param projectId Project/agent id.
   */
  public static void listEntityTypes(String projectId) throws Exception {
    // Instantiates a client
    try (EntityTypesClient entityTypesClient = EntityTypesClient.create()) {
      // Set the project agent name using the projectID (my-project-id)
      ProjectAgentName parent = ProjectAgentName.of(projectId);

      // Performs the list entity types request
      for (EntityType entityType : entityTypesClient.listEntityTypes(parent).iterateAll()) {
        System.out.format("Entity type name %s\n", entityType.getName());
        System.out.format("Entity type display name: %s\n", entityType.getDisplayName());
        System.out.format("Number of entities: %d\n", entityType.getEntitiesCount());
      }
    }
  }

  // [START dialogflow_create_entity_type]
  /**
   * Create an entity type with the given display name
   * @param displayName The display name of the entity.
   * @param projectId Project/agent id.
   * @param kind The kind of entity.  KIND_MAP (default) or KIND_LIST.
   */
  public static void createEntityType(String displayName, String projectId, String kind)
      throws Exception {
    // Instantiates a client
    try (EntityTypesClient entityTypesClient = EntityTypesClient.create()) {
      // Set the project agent name using the projectID (my-project-id)
      ProjectAgentName parent = ProjectAgentName.of(projectId);

      // Entity types serve as a tool for extracting parameter values from natural language queries.
      EntityType entityType = EntityType.newBuilder()
          .setDisplayName(displayName)
          .setKind(Kind.valueOf(kind))
          .build();

      // Performs the create entity type request
      EntityType response = entityTypesClient.createEntityType(parent, entityType);
      System.out.println("Entity type created: " + response);
    }
  }
  // [END dialogflow_create_entity_type]

  // [START dialogflow_delete_entity_type]
  /**
   * Delete entity type with the given entity type name
   * @param entityTypeId The id of the entity_type.
   * @param projectId Project/agent id.
   */
  public static void deleteEntityType(String entityTypeId, String projectId) throws Exception {
    // Instantiates a client
    try (EntityTypesClient entityTypesClient = EntityTypesClient.create()) {
      // Set the entity type name using the projectID (my-project-id) and entityTypeId (KIND_LIST)
      EntityTypeName name = EntityTypeName.of(projectId, entityTypeId);

      // Performs the delete entity type request
      entityTypesClient.deleteEntityType(name);
    }
  }
  // [END dialogflow_delete_entity_type]

  /**
   * Helper method for testing to get entityTypeId from displayName.
   */
  public static List<String> getEntityTypeIds(String displayName, String projectId)
      throws Exception {
    List<String> entityTypesIds = new ArrayList<>();

    try (EntityTypesClient entityTypesClient = EntityTypesClient.create()) {
      ProjectAgentName parent = ProjectAgentName.of(projectId);
      // Performs the list entity types request
      for (EntityType entityType : entityTypesClient.listEntityTypes(parent).iterateAll()) {
        if (entityType.getDisplayName().equals(displayName)) {
          String[] splitName = entityType.getName().split("/");
          entityTypesIds.add(splitName[splitName.length - 1]);
        }
      }
    }
    return entityTypesIds;
  }

  // [START run_application]
  public static void main(String[] args) throws Exception {
    String method = "";
    String entityTypeId = "";
    String displayName = "";
    String kind = "KIND_MAP";
    String projectId = "";

    try {
      method = args[0];
      String command;
      if (method.equals("list")) {
        command = args[1];
        if (command.equals("--projectId")) {
          projectId = args[2];
        }
      } else if (method.equals("create")) {
        displayName = args[1];

        command = args[2];
        if (command.equals("--projectId")) {
          projectId = args[3];
        }

        if (args.length > 4) {
          command = args[4];
          if (command.equals("--kind")) {
            kind = args[5];
          }
        }
      } else if (method.equals("delete")) {
        entityTypeId = args[1];

        command = args[2];
        if (command.equals("--projectId")) {
          projectId = args[3];
        }
      }
    } catch (Exception e) {
      System.out.println("Usage:");
      System.out.println("mvn exec:java -DEntityTypeManagement "
          + "-Dexec.args='list --projectId PROJECT_ID'\n");

      System.out.println("mvn exec:java -DEntityTypeManagement "
          + "-Dexec.args='create employee --projectId PROJECT_ID'\n");

      System.out.println("mvn exec:java -DEntityTypeManagement "
          + "-Dexec.args='delete ENTITY_TYPE_ID --projectId PROJECT_ID'\n");

      System.out.println("Commands: list");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println("Commands: create");
      System.out.println("\t<displayName> - The display name of the entity.");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println("\nOptional Commands [For create]:");
      System.out.println("\t--kind <kind> - The kind of entity. KIND_MAP (default) or KIND_LIST.");
      System.out.println("Commands: delete");
      System.out.println("\t<entityTypeId> - [The id of the entity_type.");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
    }

    if (method.equals("list")) {
      listEntityTypes(projectId);
    } else if (method.equals("create")) {
      createEntityType(displayName, projectId, kind);
    } else if (method.equals("delete")) {
      deleteEntityType(entityTypeId, projectId);
    }
  }
  // [END run_application]
}
