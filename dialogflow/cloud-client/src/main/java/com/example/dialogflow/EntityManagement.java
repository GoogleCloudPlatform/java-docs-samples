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
import com.google.cloud.dialogflow.v2.EntityType.Entity;
import com.google.cloud.dialogflow.v2.EntityTypeName;
import com.google.cloud.dialogflow.v2.EntityTypesClient;
import com.google.protobuf.Empty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// [END dialogflow_import_libraries]

/**
 * DialogFlow API Entity sample.
 */
public class EntityManagement {

  /**
   * List entities
   * @param projectId Project/agent id.
   * @param entityTypeId The id of the entity_type.
   */
  public static void listEntities(String projectId, String entityTypeId) throws Exception {
    // Instantiates a client
    try (EntityTypesClient entityTypesClient = EntityTypesClient.create()) {
      // Set the entity type name using the projectID (my-project-id) and entityTypeId (KIND_LIST)
      EntityTypeName name = EntityTypeName.of(projectId, entityTypeId);

      // Performs the get entity type request
      EntityType entityType = entityTypesClient.getEntityType(name);
      for (Entity entity : entityType.getEntitiesList()) {
        System.out.format("Entity value: %s\n", entity.getValue());
        System.out.format("Entity synonyms: %s\n", entity.getSynonymsList().toString());
      }
    }
  }

  // [START dialogflow_create_entity]
  /**
   * Create an entity of the given entity type
   * @param projectId Project/agent id.
   * @param entityTypeId The id of the entity_type.
   * @param entityValue The entity value to be added.
   * @param synonyms The synonyms that will map to the provided entity value.
   */
  public static void createEntity(String projectId, String entityTypeId, String entityValue,
      List<String> synonyms) throws Exception {
    // Note: synonyms must be exactly [entityValue] if the
    // entityTypeId's kind is KIND_LIST
    if (synonyms.size() == 0) {
      synonyms.add(entityValue);
    }

    // Instantiates a client
    try (EntityTypesClient entityTypesClient = EntityTypesClient.create()) {
      // Set the entity type name using the projectID (my-project-id) and entityTypeId (KINDS_LIST)
      EntityTypeName name = EntityTypeName.of(projectId, entityTypeId);

      // Build the entity
      Entity entity = Entity.newBuilder()
          .setValue(entityValue)
          .addAllSynonyms(synonyms)
          .build();

      // Performs the create entity type request
      Empty response = entityTypesClient.batchCreateEntitiesAsync(name,
          Arrays.asList(entity)).get();
      System.out.println("Entity created: " + response);
    }


  }
  // [END dialogflow_create_entity]

  // [START dialogflow_delete_entity]
  /**
   * Delete entity with the given entity type and entity value
   * @param projectId Project/agent id.
   * @param entityTypeId The id of the entity_type.
   * @param entityValue The value of the entity to delete.
   */
  public static void deleteEntity(String projectId, String entityTypeId, String entityValue)
      throws Exception {
    // Instantiates a client
    try (EntityTypesClient entityTypesClient = EntityTypesClient.create()) {
      // Set the entity type name using the projectID (my-project-id) and entityTypeId (KINDS_LIST)
      EntityTypeName name = EntityTypeName.of(projectId, entityTypeId);

      // Performs the delete entity type request
      entityTypesClient.batchDeleteEntitiesAsync(name, Arrays.asList(entityValue))
          .getInitialFuture().get();
    }
  }
  // [END dialogflow_delete_entity]

  // [START run_application]
  public static void main(String[] args) throws Exception {
    String method = "";
    String entityTypeId = "";
    String entityValue = "";
    List<String> synonyms = new ArrayList<>();
    String projectId = "";

    try {
      method = args[0];
      String command = args[1];
      if (method.equals("list")) {
        if (command.equals("--projectId")) {
          projectId = args[2];
        }

        command = args[3];
        if (command.equals("--entityTypeId")) {
          entityTypeId = args[4];
        }
      } else if (method.equals("create") || method.equals("delete")) {
        entityValue = command;
        command = args[2];
        if (command.equals("--projectId")) {
          projectId = args[3];
        }

        command = args[4];
        if (command.equals("--entityTypeId")) {
          entityTypeId = args[5];
        }

        if (method.equals("create") && args.length > 5) {
          command = args[6];
          if (command.equals("--synonyms")) {
            for (int i = 7; i < args.length; i++) {
              synonyms.add(args[i]);
            }
          }
        }
      }
    } catch (Exception e) {
      System.out.println("Usage:");
      System.out.println("mvn exec:java -DEntityManagement "
          + "-Dexec.args='list --projectId PROJECT_ID "
          + "--entityTypeId e57238e2-e692-44ea-9216-6be1b2332e2a'\n");

      System.out.print("mvn exec:java -DEntityManagement "
          + "-Dexec.args='create new_room --projectId PROJECT_ID "
          + "--entityTypeId e57238e2-e692-44ea-9216-6be1b2332e2a"
          + "--synonyms basement cellar'\n");

      System.out.print("mvn exec:java -DEntityManagement "
          + "-Dexec.args='delete new_room --projectId PROJECT_ID "
          + "--entityTypeId e57238e2-e692-44ea-9216-6be1b2332e2a'\n");

      System.out.println("Commands: list");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println("\nCommands:  create | delete");
      System.out.println("\t<entityValue> - The entity value to be added.");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println("\t--entityTypeId <entityTypeId> - [For create | delete] The id of the "
          + "entityType to which to add an entity.");
      System.out.println("\nOptional Commands [For create]:");
      System.out.println("\t--synonyms <synonyms> - The synonyms that will map to the provided "
          + "entity value.");
    }

    if (method.equals("list")) {
      listEntities(projectId, entityTypeId);
    } else if (method.equals("create")) {
      createEntity(projectId, entityTypeId, entityValue, synonyms);
    } else if (method.equals("delete")) {
      deleteEntity(projectId, entityTypeId, entityValue);
    }
  }
  // [END run_application]
}
