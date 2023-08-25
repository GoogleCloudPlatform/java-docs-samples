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

// [START contentwarehouse_delete_document_schema]

import com.google.cloud.contentwarehouse.v1.DeleteDocumentSchemaRequest;
import com.google.cloud.contentwarehouse.v1.DocumentSchemaName;
import com.google.cloud.contentwarehouse.v1.DocumentSchemaServiceClient;
import com.google.cloud.contentwarehouse.v1.DocumentSchemaServiceSettings;
import com.google.cloud.resourcemanager.v3.Project;
import com.google.cloud.resourcemanager.v3.ProjectName;
import com.google.cloud.resourcemanager.v3.ProjectsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class DeleteDocumentSchema {

  public static void createDocumentSchema() throws IOException, 
        InterruptedException, ExecutionException, TimeoutException {
    String projectId = "your-project-id";
    String location = "your-region"; // Format is "us" or "eu".
    String documentSchemaId = "your-schema-id";
    deleteDocumentSchema(projectId, location, documentSchemaId);
  }
  
  // Creates a new Document Schema
  public static void deleteDocumentSchema(String projectId, String location,
      String documentSchemaId) throws IOException,
          InterruptedException, ExecutionException, TimeoutException {
    String projectNumber = getProjectNumber(projectId);

    String endpoint = "contentwarehouse.googleapis.com:443";
    if (!"us".equals(location)) {
      endpoint = String.format("%s-%s", location, endpoint);
    }
    DocumentSchemaServiceSettings documentSchemaServiceSettings = 
         DocumentSchemaServiceSettings.newBuilder().setEndpoint(endpoint).build(); 
  
    // Create a Schema Service client
    try (DocumentSchemaServiceClient documentSchemaServiceClient =
        DocumentSchemaServiceClient.create(documentSchemaServiceSettings)) {
      
      /* The full resource name of the location, e.g.: 
       projects/{project_number}/location/{location}/documentSchemas/{document_schema_id} */
      DocumentSchemaName documentSchemaName = 
          DocumentSchemaName.of(projectNumber, location, documentSchemaId);

      /* Create request to delete Document Schema from provided schema ID.
       * More detail on managing Document Schemas: 
       * https://cloud.google.com/document-warehouse/docs/manage-document-schemas */
      DeleteDocumentSchemaRequest deleteDocumentSchemaRequest = 
          DeleteDocumentSchemaRequest.newBuilder()
            .setName(documentSchemaName.toString()).build();

      // Delete Document Schema
      documentSchemaServiceClient.deleteDocumentSchema(deleteDocumentSchemaRequest);

      System.out.println("Document Schema ID " + documentSchemaId + " has been deleted.");

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
// [END contentwarehouse_delete_document_schema]
