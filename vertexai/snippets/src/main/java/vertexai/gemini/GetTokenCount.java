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

package vertexai.gemini;

// [START generativeaionvertexai_gemini_token_count]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.CountTokensResponse;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import java.io.IOException;

public class GetTokenCount {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";

    getTokenCount(projectId, location, modelName);
  }

  // Gets the number of tokens for the prompt and the model's response.
  public static int getTokenCount(String projectId, String location, String modelName)
      throws IOException {
    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      GenerativeModel model = new GenerativeModel(modelName, vertexAI);

      String textPrompt = "Why is the sky blue?";
      CountTokensResponse response = model.countTokens(textPrompt);

      int promptTokenCount = response.getTotalTokens();
      int promptCharCount = response.getTotalBillableCharacters();

      System.out.println("Prompt token Count: " + promptTokenCount);
      System.out.println("Prompt billable character count: " + promptCharCount);

      GenerateContentResponse contentResponse = model.generateContent(textPrompt);

      int tokenCount = contentResponse.getUsageMetadata().getPromptTokenCount();
      int candidateTokenCount = contentResponse.getUsageMetadata().getCandidatesTokenCount();
      int totalTokenCount = contentResponse.getUsageMetadata().getTotalTokenCount();

      System.out.println("Prompt token Count: " + tokenCount);
      System.out.println("Candidate Token Count: " + candidateTokenCount);
      System.out.println("Total token Count: " + totalTokenCount);

      return promptTokenCount;
    }
  }
}
// [END generativeaionvertexai_gemini_token_count]
