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

package genai.text_generation;

// [START googlegenaisdk_textgen_with_txt]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import java.io.IOException;
import org.apache.http.HttpException;

public class TextGeneration {

  public static void main(String[] args) throws IOException, HttpException {
    // TODO(Developer): Replace the below variables before running the sample.
    String modelId = "gemini-2.0-flash-001";
    String prompt = "How does AI work?";
    generateContent(modelId, prompt);
  }

  public static String generateContent(String modelId, String prompt)
      throws HttpException, IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client = new Client()) {
      GenerateContentResponse response = client.models.generateContent(modelId, prompt, null);
      System.out.println(response.text());
      // Example response:
      // Okay, let's break down how AI works. It's a broad field, so I'll focus on the ...
      // Here's a simplified overview:
      // ...
      return response.text();
    }
  }
}
// [END googlegenaisdk_textgen_with_txt]
