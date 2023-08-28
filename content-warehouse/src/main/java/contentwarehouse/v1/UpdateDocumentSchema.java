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

// [START contentwarehouse_update_document_schema]

import com.google.cloud.contentwarehouse.v1.DocumentSchema;
import com.google.cloud.contentwarehouse.v1.DocumentSchemaName;
import com.google.cloud.contentwarehouse.v1.DocumentSchemaServiceClient;
import com.google.cloud.contentwarehouse.v1.DocumentSchemaServiceSettings;
import com.google.cloud.contentwarehouse.v1.PropertyDefinition;
import com.google.cloud.contentwarehouse.v1.TextTypeOptions;
import com.google.cloud.contentwarehouse.v1.UpdateDocumentSchemaRequest;
import com.google.cloud.resourcemanager.v3.Project;
import com.google.cloud.resourcemanager.v3.ProjectName;
import com.google.cloud.resourcemanager.v3.ProjectsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class UpdateDocumentSchema {
  public static void updateDocumentSchema() throws IOException, 
        InterruptedException, ExecutionException, TimeoutException { 
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String location = "your-region"; // Format is "us" or "eu".
    String documentSchemaId = "your-document-schema-id";
    /* The below method call retrieves details about the schema you are about to update.
     * It is important to note that some properties cannot be edited or removed. 
     * For more information on managing document schemas, please see the below documentation.
     * https://cloud.google.com/document-warehouse/docs/manage-document-schemas */
    GetDocumentSchema.getDocumentSchema(projectId, location, documentSchemaId);
    updateDocumentSchema(projectId, location, documentSchemaId);
  }

  // Updates an existing Document Schema
  public static void updateDocumentSchema(String projectId, String location, 
        String documentSchemaId) throws IOException, InterruptedException,
          ExecutionException, TimeoutException { 
    String projectNumber = getProjectNumber(projectId);

    String endpoint = "contentwarehouse.googleapis.com:443";
    if (!"us".equals(location)) {
      endpoint = String.format("%s-%s", location, endpoint);
    }

    DocumentSchemaServiceSettings documentSchemaServiceSettings = 
             DocumentSchemaServiceSettings.newBuilder().setEndpoint(endpoint).build(); 

    /* Create the Schema Service Client 
     * Initialize client that will be used to send requests. 
     * This client only needs to be created once, and can be reused for multiple requests. */
    try (DocumentSchemaServiceClient documentSchemaServiceClient = 
            DocumentSchemaServiceClient.create(documentSchemaServiceSettings)) {
            
      /* The full resource name of the location, e.g.: 
       projects/{project_number}/location/{location}/documentSchemas/{document_schema_id} */
      DocumentSchemaName documentSchemaName = 
          DocumentSchemaName.of(projectNumber, location, documentSchemaId);
            
      // Define the new Schema Property with updated values
      PropertyDefinition propertyDefinition = PropertyDefinition.newBuilder()
          .setName("plaintiff")
          .setDisplayName("Plaintiff")
          .setIsSearchable(true)
          .setIsRepeatable(true)
          .setIsRequired(false)
          .setTextTypeOptions(TextTypeOptions.newBuilder()
          .build())
          .build();

      DocumentSchema updatedDocumentSchema = DocumentSchema.newBuilder()
                    .setDisplayName("Test Doc Schema") 
                    .addPropertyDefinitions(0, propertyDefinition).build();

      // Create the Request to Update the Document Schema
      UpdateDocumentSchemaRequest updateDocumentSchemaRequest = 
            UpdateDocumentSchemaRequest.newBuilder()
            .setName(documentSchemaName.toString())
            .setDocumentSchema(updatedDocumentSchema)
            .build();
            
      // Update Document Schema
      updatedDocumentSchema = 
        documentSchemaServiceClient.updateDocumentSchema(updateDocumentSchemaRequest);
            
      // Read the output of Updated Document Schema Name
      System.out.println(updatedDocumentSchema.getName());
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
// [END contentwarehouse_update_document_schema]
