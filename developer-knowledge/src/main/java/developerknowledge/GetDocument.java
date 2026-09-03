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

// [START developerknowledge_get_document]
import com.google.developers.knowledge.v1.DeveloperKnowledgeClient;
import com.google.developers.knowledge.v1.Document;
import com.google.developers.knowledge.v1.GetDocumentRequest;
import java.io.IOException;

public class GetDocument {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String name = "documents/docs.cloud.google.com/storage/docs/creating-buckets";
    getDocument(name);
  }

  // Retrieves a single developer documentation page by its resource name.
  public static Document getDocument(String name) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DeveloperKnowledgeClient client = DeveloperKnowledgeClient.create()) {
      GetDocumentRequest request = GetDocumentRequest.newBuilder().setName(name).build();

      Document document = client.getDocument(request);

      System.out.println("Title: " + document.getTitle());
      System.out.println("URI: " + document.getUri());
      System.out.println("Data Source: " + document.getDataSource());
      System.out.println("Content Length: " + document.getContentLengthBytes() + " bytes");
      String preview = document.getContent();
      if (preview.length() > 150) {
        preview = preview.substring(0, 150) + "...";
      }
      System.out.println("Content Preview: " + preview + "\n");

      return document;
    }
  }
}
// [END developerknowledge_get_document]
