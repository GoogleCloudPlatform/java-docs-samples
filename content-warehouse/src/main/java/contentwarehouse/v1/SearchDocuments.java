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

// [START contentwarehouse_search_documents]
import com.google.cloud.contentwarehouse.v1.DocumentQuery;
import com.google.cloud.contentwarehouse.v1.DocumentServiceClient;
import com.google.cloud.contentwarehouse.v1.DocumentServiceClient.SearchDocumentsPagedResponse;
import com.google.cloud.contentwarehouse.v1.DocumentServiceSettings;
import com.google.cloud.contentwarehouse.v1.FileTypeFilter;
import com.google.cloud.contentwarehouse.v1.FileTypeFilter.FileType;
import com.google.cloud.contentwarehouse.v1.LocationName;
import com.google.cloud.contentwarehouse.v1.RequestMetadata;
import com.google.cloud.contentwarehouse.v1.SearchDocumentsRequest;
import com.google.cloud.contentwarehouse.v1.SearchDocumentsResponse.MatchingDocument;
import com.google.cloud.contentwarehouse.v1.UserInfo;
import com.google.cloud.resourcemanager.v3.Project;
import com.google.cloud.resourcemanager.v3.ProjectName;
import com.google.cloud.resourcemanager.v3.ProjectsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class SearchDocuments {
  public static void main(String[] args) throws IOException, 
        InterruptedException, ExecutionException, TimeoutException { 
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String location = "your-region"; // Format is "us" or "eu".
    String documentQuery = "your-document-query";
    String userId = "your-user-id"; // Format is user:<user-id>

    searchDocuments(projectId, location, documentQuery, userId);
  }

  // Searches all documents for a given Document Query
  public static void searchDocuments(String projectId, String location,
        String documentQuery, String userId) throws IOException, InterruptedException,
          ExecutionException, TimeoutException { 
    String projectNumber = getProjectNumber(projectId);

    String endpoint = "contentwarehouse.googleapis.com:443";
    if (!"us".equals(location)) {
      endpoint = String.format("%s-%s", location, endpoint);
    }

    DocumentServiceSettings documentServiceSettings = 
             DocumentServiceSettings.newBuilder().setEndpoint(endpoint)
             .build(); 

    /*
     * Create the Document Service Client 
     * Initialize client that will be used to send requests. 
     * This client only needs to be created once, and can be reused for multiple requests. 
     */
    try (DocumentServiceClient documentServiceClient = 
            DocumentServiceClient.create(documentServiceSettings)) {  

      /*
       * The full resource name of the location, e.g.:
       * projects/{project_number}/locations/{location} 
       */
      String parent = LocationName.format(projectNumber, location);

      // Define RequestMetadata object for context of the user making the API call
      RequestMetadata requestMetadata = RequestMetadata.newBuilder()
          .setUserInfo(
          UserInfo.newBuilder()
            .setId(userId)
            .build())
            .build();

      // Set file type for filter to 'DOCUMENT'
      FileType documentFileType = FileType.DOCUMENT;

      // Create a file type filter for documents 
      FileTypeFilter fileTypeFilter = FileTypeFilter.newBuilder()
          .setFileType(documentFileType)
          .build();
      
      // Create document query to search all documents for text given at input
      DocumentQuery query = DocumentQuery.newBuilder()
          .setQuery(documentQuery)
          .setFileTypeFilter(fileTypeFilter)
          .build();

      /*
       * Create the request to search all documents for specified query. 
       * Please note the offset in this request is to only return the specified number of results 
       * to avoid hitting the API quota. 
       */
      SearchDocumentsRequest searchDocumentsRequest = SearchDocumentsRequest.newBuilder()
          .setParent(parent)
          .setRequestMetadata(requestMetadata)
          .setOffset(5)
          .setDocumentQuery(query)
          .build();

      // Make the call to search documents with document service client and store the response
      SearchDocumentsPagedResponse searchDocumentsPagedResponse = 
          documentServiceClient.searchDocuments(searchDocumentsRequest);

      // Iterate through response and print search results for documents matching the search query
      for (MatchingDocument matchingDocument :
          searchDocumentsPagedResponse.iterateAll()) {
        System.out.println(
            "Display Name: " + matchingDocument.getDocument().getDisplayName()
            + "Document Name: " + matchingDocument.getDocument().getName()
            + "Document Creation Time: " + matchingDocument.getDocument().getCreateTime().toString()
            + "Search Text Snippet: " + matchingDocument.getSearchTextSnippet());
      }
    }
  }

  private static String getProjectNumber(String projectId) throws IOException { 
    /*
     * Initialize client that will be used to send requests. 
     * This client only needs to be created once, and can be reused for multiple requests.
     */
    try (ProjectsClient projectsClient = ProjectsClient.create()) { 
      ProjectName projectName = ProjectName.of(projectId); 
      Project project = projectsClient.getProject(projectName);
      String projectNumber = project.getName(); // Format returned is projects/xxxxxx
      return projectNumber.substring(projectNumber.lastIndexOf("/") + 1);
    } 
  }
}
// [END contentwarehouse_search_documents]
