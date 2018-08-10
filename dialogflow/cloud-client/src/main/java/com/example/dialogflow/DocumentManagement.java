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

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.dialogflow.v2beta1.CreateDocumentRequest;
import com.google.cloud.dialogflow.v2beta1.Document;
import com.google.cloud.dialogflow.v2beta1.Document.KnowledgeType;
import com.google.cloud.dialogflow.v2beta1.DocumentName;
import com.google.cloud.dialogflow.v2beta1.DocumentsClient;
import com.google.cloud.dialogflow.v2beta1.KnowledgeBaseName;
import com.google.cloud.dialogflow.v2beta1.KnowledgeOperationMetadata;

public class DocumentManagement {

  //  [START dialogflow_list_document]

  /**
   * @param projectId Project/Agent id.
   * @param knowledgeBaseId Knowledge Base id.
   * @throws Exception
   */
  public static void listDocuments(String projectId, String knowledgeBaseId) throws Exception {
    // Instantiates a client
    try (DocumentsClient documentsClient = DocumentsClient.create()) {
      KnowledgeBaseName knowledgeBaseName = KnowledgeBaseName.of(projectId, knowledgeBaseId);
      for (Document document : documentsClient.listDocuments(knowledgeBaseName).iterateAll()) {
        System.out.format(" - Display Name: %s\n", document.getDisplayName());
        System.out.format(" - Knowledge ID: %s\n", document.getName());
        System.out.format(" - MIME Type: %s\n", document.getMimeType());
        System.out.format(" - Knowledge Types:\n");
        for (KnowledgeType knowledgeTypeId : document.getKnowledgeTypesList()) {
          System.out.format("  - %s \n", knowledgeTypeId.getValueDescriptor());
        }
        System.out.format(" - Source: %s \n", document.getContentUri());
      }
    }
  }

  //  [END dialogflow_list_document]

  // [START dialogflow_create_document]

  /**
   * @param projectId Project/Agent id.
   * @param knowledgeBaseId Knowledge Base id.
   * @param displayName display name of the Document.
   * @param mimeType MIME type of the Document. e.g. text/csv, text/html
   * @param knowledgeType Knowledge Type of the Document. e.g. FAQ, EXTRACTIVE_QA
   * @param contentUri Uri of the Document. e.g. gs://path/mydoc.csv, http://mypage.com/faq.html
   * @throws Exception
   */
  public static void createDocument(
      String projectId,
      String knowledgeBaseId,
      String displayName,
      String mimeType,
      String knowledgeType,
      String contentUri)
      throws Exception {
    // Instantiates a client
    try (DocumentsClient documentsClient = DocumentsClient.create()) {
      Document document =
          Document.newBuilder()
              .setDisplayName(displayName)
              .setContentUri(contentUri)
              .setMimeType(mimeType)
              .addKnowledgeTypes(KnowledgeType.valueOf(knowledgeType))
              .build();
      KnowledgeBaseName parent = KnowledgeBaseName.of(projectId, knowledgeBaseId);
      CreateDocumentRequest createDocumentRequest =
          CreateDocumentRequest.newBuilder()
              .setDocument(document)
              .setParent(parent.toString())
              .build();
      OperationFuture<Document, KnowledgeOperationMetadata> response =
          documentsClient.createDocumentAsync(createDocumentRequest);
      System.out.format("Created Document:\n");
      System.out.format(" - Display Name: %s\n", response.get().getDisplayName());
      System.out.format(" - Knowledge ID: %s\n", response.get().getName());
      System.out.format(" - MIME Type: %s\n", response.get().getMimeType());
      System.out.format(" - Knowledge Types:\n");
      for (KnowledgeType knowledgeTypeId : document.getKnowledgeTypesList()) {
        System.out.format("  - %s \n", knowledgeTypeId.getValueDescriptor());
      }
      System.out.format(" - Source: %s \n", document.getContentUri());
    }
  }
  // [END dialogflow_create_document]

  // [START dialogflow_get_document]

  /**
   * @param projectId Project/Agent id.
   * @param knowledgeBaseId Knowledge Base id.
   * @param documentId Document Id.
   * @throws Exception
   */
  public static void getDocument(String projectId, String knowledgeBaseId, String documentId)
      throws Exception {
    // Instantiates a client
    try (DocumentsClient documentsClient = DocumentsClient.create()) {
      DocumentName documentName = DocumentName.of(projectId, knowledgeBaseId, documentId);
      Document response = documentsClient.getDocument(documentName);
      System.out.format("Got Document: \n");
      System.out.format(" - Display Name: %s\n", response.getDisplayName());
      System.out.format(" - Knowledge ID: %s\n", response.getName());
      System.out.format(" - MIME Type: %s\n", response.getMimeType());
      System.out.format(" - Knowledge Types:\n");
      for (KnowledgeType knowledgeTypeId : response.getKnowledgeTypesList()) {
        System.out.format("  - %s \n", knowledgeTypeId.getValueDescriptor());
      }
      System.out.format(" - Source: %s \n", response.getContentUri());
    }
  }
  // [END dialogflow_get_document]

  // [START dialogflow_delete_document]

  /**
   * @param projectId Project/Agent id.
   * @param knowledgeBaseId Knowledge Base id.
   * @param documentId Document Id.
   * @throws Exception
   */
  public static void deleteDocument(String projectId, String knowledgeBaseId, String documentId)
      throws Exception {
    // Instantiates a client
    try (DocumentsClient documentsClient = DocumentsClient.create()) {
      DocumentName documentName = DocumentName.of(projectId, knowledgeBaseId, documentId);
      documentsClient.deleteDocumentAsync(documentName).getInitialFuture().get();
      System.out.format("The document has been deleted.");
    }
  }

  // [START run_application]
  public static void main(String[] args) throws Exception {
    String method = "";
    String knowledgeBaseId = "";
    String displayName = "";
    String mimeType = "";
    String knowledgeType = "";
    String contentUri = "";
    String projectId = "";
    String documentId = "";

    try {
      method = args[0];
      String command = args[1];
      if (method.equals("list")) {
        if (command.equals("--projectId")) {
          projectId = args[2];
        }
        command = args[3];
        if (command.equals("--knowledgeBaseId")) {
          knowledgeBaseId = args[4];
        }
      } else if (method.equals("create")) {
        if (command.equals("--projectId")) {
          projectId = args[2];
        }
        command = args[3];
        if (command.equals("--knowledgeBaseId")) {
          knowledgeBaseId = args[4];
        }
        command = args[5];
        if (command.equals("--displayName")) {
          displayName = args[6];
        }
        command = args[7];
        if (command.equals("--mimeType")) {
          mimeType = args[8];
        }
        command = args[9];
        if (command.equals("--knowledgeType")) {
          knowledgeType = args[10];
        }
        command = args[11];
        if (command.equals("--contentUri")) {
          contentUri = args[12];
        }
      } else if (method.equals("get")) {
        if (command.equals("--projectId")) {
          projectId = args[2];
        }
        command = args[3];
        if (command.equals("--knowledgeBaseId")) {
          knowledgeBaseId = args[4];
        }
        command = args[5];
        if (command.equals("--documentId")) {
          documentId = args[6];
        }
      } else if (method.equals("delete")) {
        if (command.equals("--projectId")) {
          projectId = args[2];
        }
        command = args[3];
        if (command.equals("--knowledgeBaseId")) {
          knowledgeBaseId = args[4];
        }
        command = args[5];
        if (command.equals("--documentId")) {
          documentId = args[6];
        }
      }
    } catch (Exception e) {
      System.out.println("Usage:");
      System.out.println(
          "mvn exec:java -DDocumentManagement "
              + "-Dexec.args='list --projectId PROJECT_ID "
              + "--knowledgeBaseId MTc5NjM0NTE1Mzg2OTczNTUyNjQ'\n");

      System.out.print(
          "mvn exec:java -DDocumentManagement "
              + "-Dexec.args='create --projectId PROJECT_ID "
              + "--knowledgeBaseId MTc5NjM0NTE1Mzg2OTczNTUyNjQ"
              + "--displayName test_doc'\n"
              + "--mimeType text/html'\n"
              + "--knowledgeType FAQ'\n"
              + "--contentUri https://cloud.google.com/storage/docs/faq'\n");

      System.out.print(
          "mvn exec:java -DDocumentManagement "
              + "-Dexec.args='get --projectId PROJECT_ID "
              + "--knowledgeBaseId MTc5NjM0NTE1Mzg2OTczNTUyNjQ'\n"
              + "--documentId MTE2OTcxMTQ4Njk2NzYzNzYwNjQ");

      System.out.print(
          "mvn exec:java -DDocumentManagement "
              + "-Dexec.args='delete --projectId PROJECT_ID "
              + "--knowledgeBaseId MTc5NjM0NTE1Mzg2OTczNTUyNjQ'\n"
              + "--documentId MTE2OTcxMTQ4Njk2NzYzNzYwNjQ");

      System.out.println("Commands: list");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");

      System.out.println("\nCommands:  create");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println(
          "\t--knowledgeBaseId <knowledgeBaseId> - [For create] The id of the "
              + "Knowledge Base to add the Document");
      System.out.println(
          "\t--displayName <displayName> - [For create] The display name of the Document");
      System.out.println("\t--mimeType <mimeType> - [For create] The MIME Type of the Document");
      System.out.println(
          "\t--knowledgeType <knowledgeType> - [For create] The Knowledge Type of the "
              + "Document");
      System.out.println("\t--contentUri <contentUri> - [For create] The Uri of the  Document");

      System.out.println("\nCommands:  get | delete");
      System.out.println("\t--projectId <projectId> - Project/Agent Id");
      System.out.println(
          "\t--knowledgeBaseId <knowledgeBaseId> - [For get | delete] The id of the "
              + "Knowledge Base to add the Document");
      System.out.println("\t--documentId <documentId> - [For get | delete] The id of the Document");
    }

    if (method.equals("list")) {
      listDocuments(projectId, knowledgeBaseId);
    } else if (method.equals("create")) {
      createDocument(projectId, knowledgeBaseId, displayName, mimeType, knowledgeType, contentUri);
    } else if (method.equals("get")) {
      getDocument(projectId, knowledgeBaseId, documentId);
    } else if (method.equals("delete")) {
      deleteDocument(projectId, knowledgeBaseId, documentId);
    }
  }
  // [END run_application]

}
