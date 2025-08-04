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

package genai.gemini.textgeneration;

// [START googlegenaisdk_textgen_with_youtube_video]

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

public class TextGenerationWithYoutubeVideo {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    generateContent(modelId);
  }

  // Generates text with YouTube video
  public static String generateContent(String modelId) {
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
              Content.fromParts(
                  Part.fromText("Write a short and engaging blog post based on this video."),
                  Part.fromUri("https://www.youtube.com/watch?v=3KtWfp0UopM", "video/mp4")),
              null);

      System.out.print(response.text());
      // Example response:
      // 25 Years of Curiosity: A Google Anniversary Dive into What the World Searched For
      //
      // Remember a time before instant answers were just a click away? 25 years ago, Google
      // launched, unleashing a wave of curiosity that has since charted the collective interests,
      // anxieties, and celebrations of humanity...
      return response.text();
    }
  }
}
// [END googlegenaisdk_textgen_with_youtube_video]
