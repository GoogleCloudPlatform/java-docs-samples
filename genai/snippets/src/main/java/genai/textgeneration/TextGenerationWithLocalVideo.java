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

// [START googlegenaisdk_textgen_with_local_video]

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TextGenerationWithLocalVideo {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    String localVideoPath = "your-local-video.mp4";
    generateContent(modelId, localVideoPath);
  }

  // Generates text with local video input
  public static String generateContent(String modelId, String localVideoPath) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      // Read content from the local video.
      byte[] videoData = Files.readAllBytes(Paths.get(localVideoPath));

      GenerateContentResponse response =
          client.models.generateContent(
              modelId,
              Content.fromParts(
                  Part.fromBytes(videoData, "video/mp4"),
                  Part.fromText("Write a short and engaging blog post based on this video.")),
              null);

      System.out.print(response.text());
      // Example response:
      // More Than Just a Climb: Finding Your Flow on the Wall
      // There's something captivating about watching a climber in their element. This short clip
      // offers a perfect glimpse into the focused world of indoor climbing, where precision meets
      // power...
      return response.text();
    }
  }
}
// [END googlegenaisdk_textgen_with_local_video]