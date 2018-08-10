/*
  Copyright 2018, Google, Inc.

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

import com.google.cloud.dialogflow.v2beta1.KnowledgeBase;
import com.google.cloud.dialogflow.v2beta1.KnowledgeBaseName;
import com.google.cloud.dialogflow.v2beta1.KnowledgeBasesClient;
import com.google.cloud.dialogflow.v2beta1.ProjectName;

public class KnowledgebaseManagement {

  // [START dialogflow_list_knowledge_base]
  /**
   * List Knowledge bases
   *
   * @param projectId Project/agent id.
   */
  public static void listKnowledgeBases(String projectId) throws Exception {
    // Instantiates a client
    try (KnowledgeBasesClient knowledgeBasesClient = KnowledgeBasesClient.create()) {
      // Set the entity type name using the projectID (my-project-id) and entityTypeId (KIND_LIST)
      ProjectName projectName = ProjectName.of(projectId);
      for (KnowledgeBase knowledgeBase :
          knowledgeBasesClient.listKnowledgeBases(projectName).iterateAll()) {
        System.out.format(" - Display Name: %s\n", knowledgeBase.getDisplayName());
        System.out.format(" - Knowledge ID: %s\n", knowledgeBase.getName());
      }
    }
  }
  // [END dialogflow_list_knowledge_base]

  // [START dialogflow_create_knowledge_base]
  /**
   * Create a Knowledge base
   *
   * @param projectId Project/agent id.
   * @param displayName Name of the knowledge base.
   */
  public static void createKnowledgeBase(String projectId, String displayName) throws Exception {
    // Instantiates a client
    try (KnowledgeBasesClient knowledgeBasesClient = KnowledgeBasesClient.create()) {

      KnowledgeBase knowledgeBase = KnowledgeBase.newBuilder().setDisplayName(displayName).build();
      ProjectName projectName = ProjectName.of(projectId);
      KnowledgeBase response = knowledgeBasesClient.createKnowledgeBase(projectName, knowledgeBase);
      System.out.format("Knowledgebase created:\n");
      System.out.format("Display Name: %s \n", response.getDisplayName());
      System.out.format("Knowledge ID: %s \n", response.getName());
    }
  }
  // [END dialogflow_create_knowledge_base]

  // [START dialogflow_get_knowledge_base]

  /**
   * @param knowledgeBaseId Knowledge base id.
   * @param projectId Project/agent id.
   * @throws Exception
   */
  public static void getKnowledgeBase(String projectId, String knowledgeBaseId) throws Exception {

    // Instantiates a client
    try (KnowledgeBasesClient knowledgeBasesClient = KnowledgeBasesClient.create()) {
      KnowledgeBaseName knowledgeBaseName = KnowledgeBaseName.of(projectId, knowledgeBaseId);
      KnowledgeBase response = knowledgeBasesClient.getKnowledgeBase(knowledgeBaseName);
      System.out.format("Got Knowledge Base:\n");
      System.out.format(" - Display Name: %s\n", response.getDisplayName());
      System.out.format(" - Knowledge ID: %s\n", response.getName());
    }
  }
  // [END dialogflow_get_knowledge_base]
  // [START dialogflow_delete_knowledge_base]

  /**
   * @param knowledgeBaseId Knowledge base id.
   * @param projectId Project/agent id.
   * @throws Exception
   */
  public static void deleteKnowledgeBase(String projectId, String knowledgeBaseId)
      throws Exception {
    // Instantiates a client
    try (KnowledgeBasesClient knowledgeBasesClient = KnowledgeBasesClient.create()) {
      KnowledgeBaseName knowledgeBaseName = KnowledgeBaseName.of(projectId, knowledgeBaseId);
      knowledgeBasesClient.deleteKnowledgeBase(knowledgeBaseName);
      System.out.format("KnowledgeBase has been deleted.\n");
    }
  }
  // [END dialogflow_delete_knowledge_base]

  // [START run_application]
  public static void main(String[] args) throws Exception {
    String method = "";
    String displayName = "";
    String knowledgeBaseId = "";
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

      } else if (method.equals("get")) {
        knowledgeBaseId = args[1];
        command = args[2];
        if (command.equals("--projectId")) {
          projectId = args[3];
        }
      } else if (method.equals("delete")) {
        knowledgeBaseId = args[1];
        command = args[2];
        if (command.equals("--projectId")) {
          projectId = args[3];
        }
      }

    } catch (Exception e) {
      System.out.println("Usage:");
      System.out.println(
          "mvn exec:java -DKnowledgeManagement "
              + "-Dexec.args='list --projectId PROJECT_ID'\n"
              + "-Dexec.args='create test_create --projectId PROJECT_ID'\n"
              + "-Dexec.args='get KNOWLEDGE_BASE_ID --projectId PROJECT_ID'\n"
              + "-Dexec.args='delete KNOWLEDGE_BASE_ID --projectId PROJECT_ID'\n");

      System.out.println("Commands: list");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");

      System.out.println("Commands: create");
      System.out.println("\t<displayName> - [For create] The display name of the knowledge base.");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");

      System.out.println("Commands: get");
      System.out.println("\t<knowledgeBaseId> - [For get] The id of the knowledge base.");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");

      System.out.println("Commands: delete");
      System.out.println("\t<knowledgeBaseId> - [For delete] The id of the knowledge base.");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
    }

    if (method.equals("list")) {
      listKnowledgeBases(projectId);
    } else if (method.equals("create")) {
      createKnowledgeBase(projectId,displayName);
    } else if (method.equals("get")) {
      getKnowledgeBase(projectId,knowledgeBaseId);
    } else if (method.equals("delete")) {
      deleteKnowledgeBase(projectId, knowledgeBaseId);
    }
  }
}
