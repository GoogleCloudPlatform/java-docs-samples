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

import static com.google.common.truth.Truth.assertThat;

import com.google.developers.knowledge.v1.AnswerQueryResponse;
import com.google.developers.knowledge.v1.BatchGetDocumentsResponse;
import com.google.developers.knowledge.v1.DeveloperKnowledgeClient.SearchDocumentChunksPagedResponse;
import com.google.developers.knowledge.v1.Document;
import com.google.developers.knowledge.v1.DocumentChunk;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SnippetsIT {

  @Test
  public void testSearchDocumentChunks() throws IOException {
    SearchDocumentChunksPagedResponse response =
        SearchDocumentChunks.searchDocumentChunks("Cloud Storage bucket creation", 3);
    assertThat(response).isNotNull();
    assertThat(response.getPage().getValues()).isNotEmpty();
    DocumentChunk firstChunk = response.getPage().getValues().iterator().next();
    assertThat(firstChunk.getParent()).startsWith("documents/");
    assertThat(firstChunk.getContent()).isNotEmpty();
  }

  @Test
  public void testGetDocument() throws IOException {
    String name = "documents/docs.cloud.google.com/storage/docs/creating-buckets";
    Document doc = GetDocument.getDocument(name);
    assertThat(doc).isNotNull();
    assertThat(doc.getName()).isEqualTo(name);
    assertThat(doc.getTitle()).isNotEmpty();
    assertThat(doc.getContent()).isNotEmpty();
  }

  @Test
  public void testBatchGetDocuments() throws IOException {
    List<String> names =
        Arrays.asList(
            "documents/docs.cloud.google.com/storage/docs/creating-buckets",
            "documents/docs.cloud.google.com/storage/docs/deleting-buckets");
    BatchGetDocumentsResponse response = BatchGetDocuments.batchGetDocuments(names);
    assertThat(response).isNotNull();
    assertThat(response.getDocumentsList()).hasSize(2);
  }

  @Test
  public void testAnswerQuery() throws IOException {
    AnswerQueryResponse response =
        AnswerQuery.answerQuery("How to create a Cloud Storage bucket");
    assertThat(response).isNotNull();
    assertThat(response.getAnswer().getAnswerText()).isNotEmpty();
  }
}
