/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aiplatform;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.IOException;
import java.util.List;
import java.util.OptionalInt;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class PredictTextEmbeddingsSampleTest {
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);
  private static final String APIS_ENDPOINT = "us-central1-aiplatform.googleapis.com:443";
  private static final String PROJECT = System.getenv("UCAIP_PROJECT_ID");

  private static void requireEnvVar(String varName) {
    String errorMessage =
        String.format("Environment variable '%s' is required to perform these tests.", varName);
    assertNotNull(errorMessage, System.getenv(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("UCAIP_PROJECT_ID");
  }

  @Test
  public void testPredictTextEmbeddings() throws IOException {
    List<String> texts =
        List.of("banana bread?", "banana muffin?", "banana?", "recipe?", "muffin recipe?");
    List<List<Float>> embeddings =
        PredictTextEmbeddingsSample.predictTextEmbeddings(
            APIS_ENDPOINT,
            PROJECT,
            "text-embedding-004",
            texts,
            "QUESTION_ANSWERING",
            OptionalInt.of(5));
    assertThat(embeddings.size()).isEqualTo(texts.size());
    for (List<Float> embedding : embeddings) {
      assertThat(embedding.size()).isEqualTo(5);
    }
  }

  @Test
  public void testPredictTextEmbeddingsPreview() throws IOException {
    List<String> texts =
        List.of("banana bread?", "banana muffin?", "banana?", "recipe?", "muffin recipe?");
    List<List<Float>> embeddings =
        PredictTextEmbeddingsSamplePreview.predictTextEmbeddings(
            APIS_ENDPOINT,
            PROJECT,
            "text-embedding-preview-0815",
            texts,
            "CODE_RETRIEVAL_QUERY",
            OptionalInt.of(5));
    assertThat(embeddings.size()).isEqualTo(texts.size());
    for (List<Float> embedding : embeddings) {
      assertThat(embedding.size()).isEqualTo(5);
    }
  }
}
