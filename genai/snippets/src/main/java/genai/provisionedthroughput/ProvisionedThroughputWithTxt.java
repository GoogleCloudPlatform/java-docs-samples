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

package genai.provisionedthroughput;

// [START googlegenaisdk_provisionedthroughput_with_txt]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import java.util.Map;

public class ProvisionedThroughputWithTxt {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    generateContent(modelId);
  }

  // Generates content with Provisioned Throughput.
  public static String generateContent(String modelId) {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("us-central1")
            .vertexAI(true)
            .httpOptions(
                HttpOptions.builder()
                    .apiVersion("v1")
                    .headers(
                        // Options:
                        // - "dedicated": Use Provisioned Throughput
                        // - "shared": Use pay-as-you-go
                        // https://cloud.google.com/vertex-ai/generative-ai/docs/use-provisioned-throughput
                        Map.of("X-Vertex-AI-LLM-Request-Type", "shared"))
                    .build())
            .build()) {

      GenerateContentResponse response =
          client.models.generateContent(
              modelId, "How does AI work?", GenerateContentConfig.builder().build());

      System.out.println(response.text());
      // Example response:
      // At its core, **AI (Artificial Intelligence) works by enabling machines to learn,
      // reason, and make decisions in ways that simulate human intelligence.** Instead of being
      // explicitly programmed for every single task...
      return response.text();
    }
  }
}
// [END googlegenaisdk_provisionedthroughput_with_txt]
