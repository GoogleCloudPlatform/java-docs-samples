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

// [START googlegenaisdk_imggen_with_txt]

import com.google.genai.Client;
import com.google.genai.types.GenerateImagesConfig;
import com.google.genai.types.GenerateImagesResponse;
import com.google.genai.types.GeneratedImage;
import com.google.genai.types.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageGenWithText {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "imagen-4.0-generate-001";
    String outputFile = "resources/output/dog_newspaper.png";
    generateImage(modelId, outputFile);
  }

  // Generates an image with text input
  public static Image generateImage(String modelId, String outputFile) throws IOException {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client = Client.builder().location("global").vertexAI(true).build()) {

      GenerateImagesResponse response =
          client.models.generateImages(
              modelId,
              "A dog reading a newspaper",
              GenerateImagesConfig.builder().imageSize("2K").outputMimeType("image/png").build());

      Image generatedImage =
          response
              .generatedImages()
              .flatMap(generatedImages -> generatedImages.stream().findFirst())
              .flatMap(GeneratedImage::image)
              .orElseThrow();

      // Read image data and write it to the output file
      if (generatedImage.imageBytes().isPresent()) {
        BufferedImage image =
            ImageIO.read(new ByteArrayInputStream(generatedImage.imageBytes().get()));
        ImageIO.write(image, "png", new File(outputFile));

        System.out.printf(
            "Created output image using %s bytes\n", generatedImage.imageBytes().get().length);
      }

      // Example response:
      // Created output image using 1633112 bytes
      return generatedImage;
    }
  }
}
// [END googlegenaisdk_imggen_with_txt]