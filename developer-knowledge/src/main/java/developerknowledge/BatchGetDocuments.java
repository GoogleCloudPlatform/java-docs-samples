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

// [START developerknowledge_batch_get_documents]
import com.google.developers.knowledge.v1.BatchGetDocumentsRequest;
import com.google.developers.knowledge.v1.BatchGetDocumentsResponse;
import com.google.developers.knowledge.v1.DeveloperKnowledgeClient;
import com.google.developers.knowledge.v1.Document;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BatchGetDocuments {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    List<String> names =
        Arrays.asList(
            "documents/docs.cloud.google.com/storage/docs/creating-buckets",
            "documents/docs.cloud.google.com/storage/docs/deleting-buckets");
    batchGetDocuments(names);
  }

  // Retrieves multiple developer documentation pages in a single request.
  public static BatchGetDocumentsResponse batchGetDocuments(List<String> names) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DeveloperKnowledgeClient client = DeveloperKnowledgeClient.create()) {
      BatchGetDocumentsRequest request =
          BatchGetDocumentsRequest.newBuilder().addAllNames(names).build();

      BatchGetDocumentsResponse response = client.batchGetDocuments(request);

      for (Document doc : response.getDocumentsList()) {
        System.out.println("Title: " + doc.getTitle());
        System.out.println("URI: " + doc.getUri());
        System.out.println("Content Length: " + doc.getContentLengthBytes() + " bytes\n");
      }

      return response;
    }
  }
}
// [END developerknowledge_batch_get_documents]
