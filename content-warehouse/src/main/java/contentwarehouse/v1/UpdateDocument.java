/*
 * Copyright 2023 Google LLC
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

package contentwarehouse.v1;

// [START contentwarehouse_update_document]
import com.google.cloud.contentwarehouse.v1.Document;
import com.google.cloud.contentwarehouse.v1.DocumentName;
import com.google.cloud.contentwarehouse.v1.DocumentServiceClient;
import com.google.cloud.contentwarehouse.v1.DocumentServiceSettings;
import com.google.cloud.contentwarehouse.v1.GetDocumentRequest;
import com.google.cloud.contentwarehouse.v1.RequestMetadata;
import com.google.cloud.contentwarehouse.v1.UpdateDocumentRequest;
import com.google.cloud.contentwarehouse.v1.UpdateDocumentResponse;
import com.google.cloud.contentwarehouse.v1.UserInfo;
import com.google.cloud.resourcemanager.v3.Project;
import com.google.cloud.resourcemanager.v3.ProjectName;
import com.google.cloud.resourcemanager.v3.ProjectsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class UpdateDocument {
  public static void updateDocument() throws IOException, 
        InterruptedException, ExecutionException, TimeoutException { 
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String location = "your-region"; // Format is "us" or "eu".
    String documentId = "your-document-id";
    String userId = "your-user-id"; // Format is user:<user-id>
    /* The below method call retrieves details about the document you are about to update.
    * It is important to note that some properties cannot be edited or removed. 
    * For more information on managing documents, please see the below documentation.
    * https://cloud.google.com/document-warehouse/docs/manage-documents */
    GetDocument.getDocument(projectId, location, documentId, userId);
    updateDocument(projectId, location, documentId, userId);
  }

  // Updates an existing Document
  public static void updateDocument(String projectId, String location,
        String documentId, String userId) throws IOException, InterruptedException,
          ExecutionException, TimeoutException { 
    String projectNumber = getProjectNumber(projectId);

    String endpoint = "contentwarehouse.googleapis.com:443";
    if (!"us".equals(location)) {
      endpoint = String.format("%s-%s", location, endpoint);
    }

    DocumentServiceSettings documentServiceSettings = 
             DocumentServiceSettings.newBuilder().setEndpoint(endpoint).build(); 

    /* Create the Document Service Client 
     * Initialize client that will be used to send requests. 
     * This client only needs to be created once, and can be reused for multiple requests. */
    try (DocumentServiceClient documentServiceClient = 
            DocumentServiceClient.create(documentServiceSettings)) {
            
      /* The full resource name of the location, e.g.: 
       projects/{project_number}/location/{location}/documentSchemas/{document_schema_id} */
      DocumentName documentName = 
          DocumentName.of(projectNumber, location, documentId);

      // Define RequestMetadata object for context of the user making the API call
      RequestMetadata requestMetadata = RequestMetadata.newBuilder()
          .setUserInfo(
          UserInfo.newBuilder()
            .setId(userId).build()).build();
      
      // Get the document to retreive the document schema associated with the object
      GetDocumentRequest getDocumentRequest = GetDocumentRequest.newBuilder()
          .setName(documentName.toString())
          .setRequestMetadata(requestMetadata)
          .build(); 
      
      // Execute the request and store response in a document object
      Document document = documentServiceClient.getDocument(getDocumentRequest);

      // Define the updates to the document that will be passed in the request
      Document updatedDocument = Document.newBuilder()
          .setDisplayName("Updated Document Display Name")
          .setDocumentSchemaName(document.getDocumentSchemaName()).build();

      // Create the request to Update the Document
      UpdateDocumentRequest updateDocumentRequest = 
            UpdateDocumentRequest.newBuilder()
              .setName(documentName.toString())
              .setDocument(updatedDocument)
              .setRequestMetadata(requestMetadata)
              .build();
            
      // Update Document and receive response
      UpdateDocumentResponse updateDocumentResponse = 
          documentServiceClient.updateDocument(updateDocumentRequest);
            
      // Read the output of Updated Document Name
      System.out.println(updateDocumentResponse.getDocument().getDisplayName());
    }
  }

  private static String getProjectNumber(String projectId) throws IOException { 
    /* Initialize client that will be used to send requests. 
    * This client only needs to be created once, and can be reused for multiple requests. */
    try (ProjectsClient projectsClient = ProjectsClient.create()) { 
      ProjectName projectName = ProjectName.of(projectId); 
      Project project = projectsClient.getProject(projectName);
      String projectNumber = project.getName(); // Format returned is projects/xxxxxx
      return projectNumber.substring(projectNumber.lastIndexOf("/") + 1);
    } 
  }
}
// [END contentwarehouse_update_document]
