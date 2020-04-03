/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.dialogflow;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.dialogflow.v2beta1.CreateDocumentRequest;
import com.google.cloud.dialogflow.v2beta1.Document;
import com.google.cloud.dialogflow.v2beta1.Document.KnowledgeType;
import com.google.cloud.dialogflow.v2beta1.DocumentsClient;
import com.google.cloud.dialogflow.v2beta1.KnowledgeBase;
import com.google.cloud.dialogflow.v2beta1.KnowledgeBasesClient;
import com.google.cloud.dialogflow.v2beta1.KnowledgeOperationMetadata;
import com.google.cloud.dialogflow.v2beta1.ProjectName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class DeleteDocumentTest {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String KNOWLEDGE_DISPLAY_NAME = UUID.randomUUID().toString();
  private static String DOCUMENT_DISPLAY_NAME = UUID.randomUUID().toString();
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private String knowledgeBaseName;
  private String documentName;

  private static void requireEnvVar(String varName) {
    assertNotNull(
        "Environment variable '%s' is required to perform these tests.".format(varName),
        String.format(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void setUp()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Create a knowledge base for the document
    try (KnowledgeBasesClient client = KnowledgeBasesClient.create()) {
      KnowledgeBase knowledgeBase =
          KnowledgeBase.newBuilder().setDisplayName(KNOWLEDGE_DISPLAY_NAME).build();
      ProjectName projectName = ProjectName.of(PROJECT_ID);
      KnowledgeBase response = client.createKnowledgeBase(projectName, knowledgeBase);
      // Save the full name for deletion
      knowledgeBaseName = response.getName();
    }

    // Create the Document to delete
    try (DocumentsClient documentsClient = DocumentsClient.create()) {
      Document document =
          Document.newBuilder()
              .setDisplayName(DOCUMENT_DISPLAY_NAME)
              .setContentUri("https://cloud.google.com/storage/docs/faq")
              .setMimeType("text/html")
              .addKnowledgeTypes(KnowledgeType.valueOf("FAQ"))
              .build();
      CreateDocumentRequest createDocumentRequest =
          CreateDocumentRequest.newBuilder()
              .setDocument(document)
              .setParent(knowledgeBaseName)
              .build();
      OperationFuture<Document, KnowledgeOperationMetadata> response =
          documentsClient.createDocumentAsync(createDocumentRequest);
      Document createdDocument = response.get(180, TimeUnit.SECONDS);
      documentName = createdDocument.getName();
    }

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testCreateDocument() throws Exception {
    DocumentManagement.deleteDocument(documentName);
    String got = bout.toString();
    assertThat(got).contains("The document has been deleted");
  }
}
