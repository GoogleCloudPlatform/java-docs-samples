/*
 * Copyright 2026 Google LLC
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

package developerknowledge;

// [START developerknowledge_search_document_chunks]
import com.google.developers.knowledge.v1.DeveloperKnowledgeClient;
import com.google.developers.knowledge.v1.DeveloperKnowledgeClient.SearchDocumentChunksPagedResponse;
import com.google.developers.knowledge.v1.DocumentChunk;
import com.google.developers.knowledge.v1.SearchDocumentChunksRequest;
import java.io.IOException;

public class SearchDocumentChunks {

  public static void main(String[] args) throws IOException {
    String query = args.length > 0 ? args[0] : "How to create a Cloud Storage bucket";
    int pageSize = args.length > 1 ? Integer.parseInt(args[1]) : 5;
    searchDocumentChunks(query, pageSize);
  }

  /**
   * Searches developer documentation chunks for a given query.
   *
   * @param query The search query string.
   * @param pageSize The maximum number of chunks to return.
   */
  public static SearchDocumentChunksPagedResponse searchDocumentChunks(
      String query, int pageSize) throws IOException {
    try (DeveloperKnowledgeClient client = DeveloperKnowledgeClient.create()) {
      SearchDocumentChunksRequest request =
          SearchDocumentChunksRequest.newBuilder()
              .setQuery(query)
              .setPageSize(pageSize)
              .build();

      SearchDocumentChunksPagedResponse response = client.searchDocumentChunks(request);

      for (DocumentChunk chunk : response.getPage().getValues()) {
        System.out.println("Parent Document: " + chunk.getParent());
        System.out.println("Chunk ID: " + chunk.getId());
        String preview = chunk.getContent();
        if (preview.length() > 100) {
          preview = preview.substring(0, 100) + "...";
        }
        System.out.println("Content: " + preview + "\n");
      }

      return response;
    }
  }
}
// [END developerknowledge_search_document_chunks]
