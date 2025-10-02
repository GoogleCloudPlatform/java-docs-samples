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

package genai.imagegeneration;

// [START googlegenaisdk_imggen_mmflash_with_txt]

import com.google.genai.Client;
import com.google.genai.types.Blob;
import com.google.genai.types.Candidate;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.SafetySetting;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class ImageGenMmFlashWithText {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash-image";
    String outputFile = "resources/output/example-image-eiffel-tower.png";
    generateContent(modelId, outputFile);
  }

  // Generates an image with text input
  public static void generateContent(String modelId, String outputFile) throws IOException {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client = Client.builder().location("global").vertexAI(true).build()) {

      GenerateContentConfig contentConfig =
          GenerateContentConfig.builder()
              .responseModalities("TEXT", "IMAGE")
              .candidateCount(1)
              .safetySettings(
                  SafetySetting.builder()
                      .method("PROBABILITY")
                      .category("HARM_CATEGORY_DANGEROUS_CONTENT")
                      .threshold("BLOCK_MEDIUM_AND_ABOVE")
                      .build())
              .build();

      GenerateContentResponse response =
          client.models.generateContent(
              modelId,
              "Generate an image of the Eiffel tower with fireworks in the background.",
              contentConfig);

      // Get parts of the response
      List<Part> parts =
          response
              .candidates()
              .flatMap(candidates -> candidates.stream().findFirst())
              .flatMap(Candidate::content)
              .flatMap(Content::parts)
              .orElse(new ArrayList<>());

      // For each part print text if present, otherwise read image data if present and
      // write it to the output file
      for (Part part : parts) {
        if (part.text().isPresent()) {
          System.out.println(part.text().get());
        } else if (part.inlineData().flatMap(Blob::data).isPresent()) {
          BufferedImage image =
              ImageIO.read(new ByteArrayInputStream(part.inlineData().flatMap(Blob::data).get()));
          ImageIO.write(image, "png", new File(outputFile));
        }
      }

      System.out.println("Content written to: " + outputFile);
      // Example response:
      // Here is the Eiffel Tower with fireworks in the background...
      //
      // Content written to: resources/output/example-image-eiffel-tower.png
    }
  }
}
// [END googlegenaisdk_imggen_mmflash_with_txt]
