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

import com.google.cloud.contentwarehouse.v1.DocumentName;
import com.google.cloud.contentwarehouse.v1.DocumentServiceClient;
import com.google.cloud.contentwarehouse.v1.DocumentServiceSettings;
import com.google.cloud.contentwarehouse.v1.RequestMetadata;
import com.google.cloud.contentwarehouse.v1.SetAclRequest;
import com.google.cloud.contentwarehouse.v1.SetAclResponse;
import com.google.cloud.contentwarehouse.v1.UserInfo;
import com.google.cloud.resourcemanager.v3.Project;
import com.google.cloud.resourcemanager.v3.ProjectName;
import com.google.cloud.resourcemanager.v3.ProjectsClient;
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class SetAcl {
  public static void setAcl() throws IOException,
          InterruptedException, ExecutionException, TimeoutException {
    /* TODO(developer): Replace these variables before running the samples
    * Find ACL roles here https://cloud.google.com/document-warehouse/docs/roles-and-permissions */
    String projectId = "your-project-id"; 
    String location = "your-region"; // Format is "us" or "eu"
    String userId = "your-userid"; // Format is user:xxxx
    ArrayList<String> members = new ArrayList<>(); // Format is ["user:xxxx", "user:xxxx"]
    String role = "your-role";        
  }

  public static void setAcl(String projectId, String location,
        ArrayList<String> members, String role, String userId,  String documentId) 
        throws IOException, InterruptedException, ExecutionException, TimeoutException { 
    String projectNumber = getProjectNumber(projectId);
    String endpoint = "contentwarehouse.googleapis.com:443";
    if (!"us".equals(location)) {
      endpoint = String.format("%s-%s", location, endpoint);
    }
    DocumentServiceSettings documentServiceSettings =
        DocumentServiceSettings.newBuilder().setEndpoint(endpoint).build();
        
    // Define Request Metadata for enforcing access control
    RequestMetadata requestMetadata = RequestMetadata.newBuilder()
        .setUserInfo(
        UserInfo.newBuilder()
            .setId(userId).build()).build();
        
    /*  Define IAM binding. This will add a list of members to be bound to a role.
    *   Please see the below documentation for IAM predefined roles for content warehouse.
    *   https://cloud.google.com/document-warehouse/docs/roles-and-permissions */ 
    Binding binding = Binding.newBuilder()
        .addAllMembers(members)
        .setRole(role).build();

    // Add IAM binding to an IAM policy. 
    Policy aclPolicy = Policy.newBuilder().addBindings(binding).build();
        
    SetAclResponse setAclResponse = SetAclResponse.newBuilder().build();

    try (DocumentServiceClient documentServiceClient = 
            DocumentServiceClient.create(documentServiceSettings)) {
      if (!documentId.isEmpty()) { 
        /* The full resource name of the document, e.g.: 
        projects/{project_number/locations/{location/documents/{document_id} */
        DocumentName documentName = 
            DocumentName.ofProjectLocationDocumentName(projectNumber, location, documentId);
        SetAclRequest setAclRequest = SetAclRequest.newBuilder()
            .setResource(documentName.toString())
            .setRequestMetadata(requestMetadata)
            .setPolicy(aclPolicy).build();

        setAclResponse = documentServiceClient.setAcl(setAclRequest);

      } else { 
        SetAclRequest setAclRequest = SetAclRequest.newBuilder()
            .setResource(String.format("%s/%s", "projects", projectNumber))
            .setRequestMetadata(requestMetadata)
            .setPolicy(aclPolicy)
            .setProjectOwner(true).build();

        setAclResponse = documentServiceClient.setAcl(setAclRequest); 
      }
      System.out.println(setAclResponse);
    }
  }

  private static String getProjectNumber(String projectId) throws IOException { 
    /* Initialize client that will be used to send requests. This client only needs to be
     created once, and can be reused for multiple requests. */
    try (ProjectsClient projectsClient = ProjectsClient.create()) { 
      ProjectName projectName = ProjectName.of(projectId); 
      Project project = projectsClient.getProject(projectName);
      String projectNumber = project.getName(); // Format returned is projects/xxxxxx
      return projectNumber.substring(projectNumber.lastIndexOf("/") + 1);
    } 
  }
}
