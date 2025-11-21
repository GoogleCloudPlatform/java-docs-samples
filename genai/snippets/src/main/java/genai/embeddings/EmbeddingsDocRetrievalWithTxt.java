/*
 * Copyright 2025 Google LLC
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

package genai.embeddings;

// [START googlegenaisdk_embeddings_docretrieval_with_txt]

import com.google.genai.Client;
import com.google.genai.types.EmbedContentConfig;
import com.google.genai.types.EmbedContentResponse;
import java.util.List;

public class EmbeddingsDocRetrievalWithTxt {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-embedding-001";
    embedContent(modelId);
  }

  // Shows how to embed content with text.
  public static EmbedContentResponse embedContent(String modelId) {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client = Client.builder().location("global").vertexAI(true).build()) {

      EmbedContentResponse response =
          client.models.embedContent(
              modelId,
              List.of(
                  "How do I get a driver's license/learner's permit?",
                  "How long is my driver's license valid for?",
                  "Driver's knowledge test study guide"),
              EmbedContentConfig.builder()
                  .taskType("RETRIEVAL_DOCUMENT")
                  .outputDimensionality(3072)
                  .title("Driver's License")
                  .build());

      System.out.println(response);
      // Example response:
      // embeddings=Optional[[ContentEmbedding{values=Optional[[-0.035855383, 0.008127963, ... ]]
      // statistics=Optional[ContentEmbeddingStatistics{truncated=Optional[false],
      // tokenCount=Optional[11.0]}]}]],
      // metadata=Optional[EmbedContentMetadata{billableCharacterCount=Optional[153]}]}
      return response;
    }
  }
}
// [END googlegenaisdk_embeddings_docretrieval_with_txt]
