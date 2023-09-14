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

// [START contentwarehouse_get_document]

import com.google.cloud.contentwarehouse.v1.Document;
import com.google.cloud.contentwarehouse.v1.DocumentName;
import com.google.cloud.contentwarehouse.v1.DocumentServiceClient;
import com.google.cloud.contentwarehouse.v1.DocumentServiceSettings;
import com.google.cloud.contentwarehouse.v1.GetDocumentRequest;
import com.google.cloud.contentwarehouse.v1.RequestMetadata;
import com.google.cloud.contentwarehouse.v1.UserInfo;
import com.google.cloud.resourcemanager.v3.Project;
import com.google.cloud.resourcemanager.v3.ProjectName;
import com.google.cloud.resourcemanager.v3.ProjectsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class GetDocument {

  public static void getDocument() throws IOException, 
        InterruptedException, ExecutionException, TimeoutException {
    String projectId = "your-project-id";
    String location = "your-region"; // Format is "us" or "eu".
    String documentId = "your-document-id";
    String userId = "your-user-id"; // Format is user:<user-id>
    getDocument(projectId, location, documentId, userId);
  }
  
  // Retrieves details about existing Document using the document Id
  public static void getDocument(String projectId, String location, 
        String documentId, String userId) throws IOException, 
            InterruptedException, ExecutionException, TimeoutException {
    String projectNumber = getProjectNumber(projectId);

    String endpoint = "contentwarehouse.googleapis.com:443";
    if (!"us".equals(location)) {
      endpoint = String.format("%s-%s", location, endpoint);
    }
    DocumentServiceSettings documentServiceSettings = 
         DocumentServiceSettings.newBuilder().setEndpoint(endpoint).build(); 
  
    // Create a Document Service client
    try (DocumentServiceClient documentServiceClient =
        DocumentServiceClient.create(documentServiceSettings)) {
      /* The full resource name of the location, e.g.: 
       projects/{project_number}/location/{location}/documents/{document_id} */
      DocumentName documentName = 
          DocumentName.of(projectNumber, location, documentId);

      // Define Request Metadata for enforcing access control
      RequestMetadata requestMetadata = RequestMetadata.newBuilder()
            .setUserInfo(
            UserInfo.newBuilder()
              .setId(userId).build()).build();

      // Define request to get details of a specific Document Schema
      GetDocumentRequest getDocumentRequest = 
          GetDocumentRequest.newBuilder()
          .setName(documentName.toString())
          .setRequestMetadata(requestMetadata).build();
        
      // Get details of the Document 
      Document document = documentServiceClient.getDocument(getDocumentRequest);

      System.out.println(document.getName());
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
// [END contentwarehouse_get_document]
