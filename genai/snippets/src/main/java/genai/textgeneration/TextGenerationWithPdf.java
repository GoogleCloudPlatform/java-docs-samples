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

package genai.textgeneration;

// [START googlegenaisdk_textgen_with_pdf]

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;

public class TextGenerationWithPdf {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    generateContent(modelId);
  }

  // Generates text with PDF
  public static String generateContent(String modelId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      String prompt =
          "You are a highly skilled document summarization specialist.\n"
              + " Your task is to provide a concise executive summary of no more than 300 words.\n"
              + "Please summarize the given document for a general audience";

      GenerateContentResponse response =
          client.models.generateContent(
              modelId,
              Content.fromParts(
                  Part.fromText(prompt),
                  Part.fromUri(
                      "gs://cloud-samples-data/generative-ai/pdf/1706.03762v7.pdf",
                      "application/pdf")),
              null);

      System.out.print(response.text());
      // Example response:
      // The document introduces the Transformer, a novel neural network architecture designed for
      // sequence transduction tasks, such as machine translation. Unlike previous dominant models
      // that rely on complex recurrent or convolutional neural networks, the Transformer proposes a
      // simpler, more parallelizable design based *solely* on attention mechanisms, entirely
      // dispensing with recurrence and convolutions...

      return response.text();
    }
  }
}
// [END googlegenaisdk_textgen_with_pdf]
