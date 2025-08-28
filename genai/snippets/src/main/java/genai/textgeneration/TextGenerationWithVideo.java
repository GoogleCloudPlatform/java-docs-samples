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

// [START googlegenaisdk_textgen_with_video]

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;

public class TextGenerationWithVideo {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    String prompt =
        " Analyze the provided video file, including its audio.\n"
            + " Summarize the main points of the video concisely.\n"
            + " Create a chapter breakdown with timestamps for key sections or topics discussed.";
    generateContent(modelId, prompt);
  }

  // Generates text with video input
  public static String generateContent(String modelId, String prompt) {
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
                  Part.fromText(prompt),
                  Part.fromUri(
                      "gs://cloud-samples-data/generative-ai/video/pixel8.mp4", "video/mp4")),
              null);

      System.out.print(response.text());
      // Example response:
      // Here's a breakdown of the video:
      //
      // **Summary:**
      //
      // Saeka Shimada, a photographer in Tokyo, uses the Google Pixel 8 Pro's "Video Boost" feature
      // to ...
      //
      // **Chapter Breakdown with Timestamps:**
      //
      // * **[00:00-00:12] Introduction & Tokyo at Night:** Saeka Shimada introduces herself ...
      return response.text();
    }
  }
}
// [END googlegenaisdk_textgen_with_video]
