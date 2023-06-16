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
import com.google.cloud.contentwarehouse.v1.FetchAclRequest;
import com.google.cloud.contentwarehouse.v1.FetchAclResponse;
import com.google.cloud.contentwarehouse.v1.RequestMetadata;
import com.google.cloud.contentwarehouse.v1.UserInfo;
import com.google.cloud.resourcemanager.v3.Project;
import com.google.cloud.resourcemanager.v3.ProjectName;
import com.google.cloud.resourcemanager.v3.ProjectsClient;
import java.io.IOException;

// [START contentwarehouse_fetch_acl]

public class FetchAcl {
  public static void fetchAcl() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String location = "your-location"; // Format is "us" or "eu".
    String userId = "your-user-id"; // Format is user:<user-id>
    String documentId = "your-documentid";
    fetchAcl(projectId, location, userId, documentId);
  }

  public static void fetchAcl(String projectId, String location, String userId, String documentId) 
      throws IOException {
    String projectNumber = getProjectNumber(projectId); 
    String endpoint = String.format("%s-contentwarehouse.googleapis.com:443", location);

    DocumentServiceSettings documentServiceSettings = 
        DocumentServiceSettings.newBuilder().setEndpoint(endpoint).build();
        
    try (DocumentServiceClient documentServiceClient = 
            DocumentServiceClient.create(documentServiceSettings)) {

      UserInfo userInfo = 
          UserInfo.newBuilder().setId(userId).build();

      DocumentName documentName = 
          DocumentName.ofProjectLocationDocumentName(projectNumber, location, documentId);

      RequestMetadata requestMetadata = 
          RequestMetadata.newBuilder().setUserInfo(userInfo).build();

      FetchAclResponse fetchAclResponse = 
          FetchAclResponse.newBuilder().build();

      if (documentId != null || documentId == "") { 
        /* The full resource name of the document, e.g.:
        projects/{project_number}/locations/{location}/documents/{document_id} */
        FetchAclRequest fetchAclRequest = 
            FetchAclRequest.newBuilder()
                .setRequestMetadata(requestMetadata)
                .setResource(documentName.toString()).build(); 
        fetchAclResponse = documentServiceClient.fetchAcl(fetchAclRequest);
      } else {
        FetchAclRequest fetchAclRequest = 
            FetchAclRequest.newBuilder()
                .setRequestMetadata(requestMetadata)
                .setResource(projectNumber)
                .setProjectOwner(true).build();
        fetchAclResponse = documentServiceClient.fetchAcl(fetchAclRequest);
      }
      System.out.println(fetchAclResponse);
    }
  }

  private static String getProjectNumber(String projectId) throws IOException { 
    try (ProjectsClient projectsClient = ProjectsClient.create()) { 
      ProjectName projectName = ProjectName.of(projectId); 
      Project project = projectsClient.getProject(projectName);
      String projectNumber = project.getName(); // Format returned is projects/xxxxxx
      return projectNumber.substring(projectNumber.lastIndexOf("/") + 1);
    } 
  }
}
// [END contentwarehouse_fetch_acl]

/*
 * def fetch_acl(
    project_number: str, location: str, user_id: str, document_id: str = ""
) -> None:
    """Fetches access control policies on project or document level.

    Args:
        project_number: Google Cloud project number.
        location: Google Cloud project location.
        user_id: user:YOUR_SERVICE_ACCOUNT_ID.
        document_id: Record id in Document AI Warehouse.
    """
    # Create a client
    client = contentwarehouse.DocumentServiceClient()

    # Initialize request argument(s)
    # Fetch document acl if document id is specified
    # else fetch acl on project level
    if document_id:
        request = contentwarehouse.FetchAclRequest(
            # The full resource name of the document, e.g.:
            # projects/{project_number}/locations/{location}/documents/{document_id}
            resource=client.document_path(project_number, location, document_id),
            request_metadata=contentwarehouse.RequestMetadata(
                user_info=contentwarehouse.UserInfo(id=user_id)
            ),
        )
    else:
        request = contentwarehouse.FetchAclRequest(
            # The full resource name of the project, e.g.:
            # projects/{project_number}
            resource=client.common_project_path(project_number),
            project_owner=True,
        )

    # Make Request
    response = client.fetch_acl(request)
    print(response)


# [END contentwarehouse_fetch_acl]
 */