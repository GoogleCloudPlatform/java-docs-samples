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

// [START googlegenaisdk_imggen_mmflash_txt_and_img_with_txt]

import com.google.genai.Client;
import com.google.genai.types.Blob;
import com.google.genai.types.Candidate;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class ImageGenMmFlashTextAndImageWithText {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash-image";
    String outputFile = "resources/output/paella-recipe.md";
    generateContent(modelId, outputFile);
  }

  // Generates text and image with text input
  public static void generateContent(String modelId, String outputFile) throws IOException {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client = Client.builder().location("global").vertexAI(true).build()) {

      GenerateContentResponse response =
          client.models.generateContent(
              modelId,
              Content.fromParts(
                  Part.fromText("Generate an illustrated recipe for a paella."),
                  Part.fromText(
                      "Create images to go alongside the text as you generate the recipe.")),
              GenerateContentConfig.builder().responseModalities("TEXT", "IMAGE").build());

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

        // Get parts of the response
        List<Part> parts =
            response
                .candidates()
                .flatMap(candidates -> candidates.stream().findFirst())
                .flatMap(Candidate::content)
                .flatMap(Content::parts)
                .orElse(new ArrayList<>());

        int index = 1;
        // For each part print text if present, otherwise read image data if present and
        // write it to the output file
        for (Part part : parts) {
          if (part.text().isPresent()) {
            writer.write(part.text().get());
          } else if (part.inlineData().flatMap(Blob::data).isPresent()) {
            BufferedImage image =
                ImageIO.read(new ByteArrayInputStream(part.inlineData().flatMap(Blob::data).get()));
            ImageIO.write(
                image, "png", new File("resources/output/example-image-" + index + ".png"));
            writer.write("![image](example-image-" + index + ".png)");
          }
          index++;
        }

        System.out.println("Content written to: " + outputFile);

        // Example response:
        // A markdown page for a Paella recipe(`paella-recipe.md`) has been generated.
        // It includes detailed steps and several images illustrating the cooking process.
        //
        // Content written to:  resources/output/paella-recipe.md
      }
    }
  }
}
// [END googlegenaisdk_imggen_mmflash_txt_and_img_with_txt]
