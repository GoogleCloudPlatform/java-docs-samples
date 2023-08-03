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

// [START contentwarehouse_create_document_schema]

import com.google.cloud.contentwarehouse.v1.CreateDocumentSchemaRequest;
import com.google.cloud.contentwarehouse.v1.DocumentSchema;
import com.google.cloud.contentwarehouse.v1.DocumentSchemaServiceClient;
import com.google.cloud.contentwarehouse.v1.DocumentSchemaServiceSettings;
import com.google.cloud.contentwarehouse.v1.LocationName;
import com.google.cloud.contentwarehouse.v1.PropertyDefinition;
import com.google.cloud.contentwarehouse.v1.TextTypeOptions;
import com.google.cloud.resourcemanager.v3.Project;
import com.google.cloud.resourcemanager.v3.ProjectName;
import com.google.cloud.resourcemanager.v3.ProjectsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class CreateDocumentSchema {

  public static void createDocumentSchema() throws IOException, 
        InterruptedException, ExecutionException, TimeoutException {
    String projectId = "your-project-id";
    String location = "your-region"; // Format is "us" or "eu".
    createDocumentSchema(projectId, location);
  }
  
  // Creates a new Document Schema
  public static void createDocumentSchema(String projectId, String location) throws IOException, 
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
      /*  The full resource name of the location, e.g.:
      projects/{project_number}/locations/{location} */
      String parent = LocationName.format(projectNumber, location);

      /* Create Document Schema with Text Type Property Definition
       * More detail on managing Document Schemas: 
       * https://cloud.google.com/document-warehouse/docs/manage-document-schemas */
      DocumentSchema documentSchema = DocumentSchema.newBuilder()
          .setDisplayName("Test Doc Schema")
          .setDescription("Test Doc Schema's Description")
          .addPropertyDefinitions(
            PropertyDefinition.newBuilder()
              .setName("plaintiff")
              .setDisplayName("Plaintiff")
              .setIsSearchable(true)
              .setIsRepeatable(true)
              .setTextTypeOptions(TextTypeOptions.newBuilder().build())
              .build()).build();

      // Define Document Schema request
      CreateDocumentSchemaRequest createDocumentSchemaRequest =
          CreateDocumentSchemaRequest.newBuilder()
            .setParent(parent)
            .setDocumentSchema(documentSchema).build();

      // Create Document Schema
      DocumentSchema documentSchemaResponse =
          documentSchemaServiceClient.createDocumentSchema(createDocumentSchemaRequest); 

      System.out.println(documentSchemaResponse.getName());
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
// [END contentwarehouse_create_document_schema]
