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

// [START googlegenaisdk_textgen_with_multi_img]

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TextGenerationWithMultiImage {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    // Content from Google Cloud Storage
    String gcsFileImagePath = "gs://cloud-samples-data/generative-ai/image/scones.jpg";
    String localImageFilePath = "resources/latte.jpg";
    generateContent(modelId, gcsFileImagePath, localImageFilePath);
  }

  // Generates text with multiple images
  public static String generateContent(
      String modelId, String gcsFileImagePath, String localImageFilePath) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder().httpOptions(HttpOptions.builder().apiVersion("v1").build()).build()) {

      // Read content from a local file.
      byte[] localFileImgBytes = Files.readAllBytes(Paths.get(localImageFilePath));

      GenerateContentResponse response =
          client.models.generateContent(
              modelId,
              Content.fromParts(
                  Part.fromText("Generate a list of all the objects contained in both images"),
                  Part.fromBytes(localFileImgBytes, "image/jpeg"),
                  Part.fromUri(gcsFileImagePath, "image/jpeg")),
              null);

      System.out.print(response.text());
      // Example response:
      // Okay, here's the list of objects present in both images:
      //
      // **Image 1 (Scones):**
      //
      // *   Scones
      // *   Plate
      // *   Jam/Preserve
      // *   Cream/Butter
      // *   Table/Surface
      // *   Napkin/Cloth (possibly)
      //
      // **Image 2 (Latte):**
      //
      // *   Latte/Coffee cup
      // *   Saucer
      // *   Spoon
      // *   Table/Surface
      // *   Foam/Latte art
      //
      // **Objects potentially in both (depending on interpretation and specific items):**
      //
      // *   Plate/Saucer (both are serving dishes)
      // *   Table/Surface
      return response.text();
    }
  }
}
// [END googlegenaisdk_textgen_with_multi_img]
