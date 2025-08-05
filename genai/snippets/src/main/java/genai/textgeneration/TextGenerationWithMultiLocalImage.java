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

// [START googlegenaisdk_textgen_with_multi_local_img]

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TextGenerationWithMultiLocalImage {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    String localImageFilePath1 = "your/local/img1.jpg";
    String localImageFilePath2 = "your/local/img2.jpg";
    generateContent(modelId, localImageFilePath1, localImageFilePath2);
  }

  // Generates text using multiple local images
  public static String generateContent(
      String modelId, String localImageFilePath1, String localImageFilePath2) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      // Read content from local files.
      byte[] localFileImg1Bytes = Files.readAllBytes(Paths.get(localImageFilePath1));
      byte[] localFileImg2Bytes = Files.readAllBytes(Paths.get(localImageFilePath2));

      GenerateContentResponse response =
          client.models.generateContent(
              modelId,
              Content.fromParts(
                  Part.fromText("Generate a list of all the objects contained in both images"),
                  Part.fromBytes(localFileImg1Bytes, "image/jpeg"),
                  Part.fromBytes(localFileImg2Bytes, "image/jpeg")),
              null);

      System.out.print(response.text());
      // Example response:
      // Based on both images, here are the objects contained in both:
      //
      // 1.  **Coffee cups (or mugs)**: Both images feature one or more cups containing a beverage.
      // 2.  **Coffee (or a similar beverage)**: Both images contain a liquid beverage in the cups,
      // appearing to be coffee or a coffee-like drink.
      // 3.  **Table (or a flat surface)**: Both compositions are set on a flat surface, likely a
      // table or countertop.
      return response.text();
    }
  }
}
// [END googlegenaisdk_textgen_with_multi_local_img]
