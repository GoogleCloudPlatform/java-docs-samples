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

package genai.contentcache;

// [START googlegenaisdk_contentcache_use_with_txt]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;

public class ContentCacheUseWithText {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    // E.g cacheName = "projects/111111111111/locations/global/cachedContents/1111111111111111111"
    String cacheName = "your-cache-name";
    contentCacheUseWithText(modelId, cacheName);
  }

  // Shows how to generate text using cached content
  public static String contentCacheUseWithText(String modelId, String cacheName) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      GenerateContentResponse response =
          client.models.generateContent(
              modelId,
              "Summarize the pdfs",
              GenerateContentConfig.builder().cachedContent(cacheName).build());

      System.out.println(response.text());
      // Example response
      // The Gemini family of multimodal models from Google DeepMind demonstrates remarkable
      // capabilities across various
      // modalities, including image, audio, video, and text....
      return response.text();
    }
  }
}
// [END googlegenaisdk_contentcache_use_with_txt]
