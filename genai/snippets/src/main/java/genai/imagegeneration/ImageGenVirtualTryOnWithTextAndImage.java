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

// [START googlegenaisdk_imggen_virtual_try_on_with_txt_img]

import com.google.genai.Client;
import com.google.genai.types.GeneratedImage;
import com.google.genai.types.Image;
import com.google.genai.types.ProductImage;
import com.google.genai.types.RecontextImageResponse;
import com.google.genai.types.RecontextImageSource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

public class ImageGenVirtualTryOnWithTextAndImage {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "virtual-try-on-preview-08-04";
    String outputFile = "resources/output/man_in_sweater.png";
    generateContent(modelId, outputFile);
  }

  // Generates a recontextualized image with image inputs
  public static Image generateContent(String modelId, String outputFile) throws IOException {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client = Client.builder().location("global").vertexAI(true).build()) {

      byte[] personImageBytes = Files.readAllBytes(Paths.get("resources/man.png"));
      Image personImage = Image.builder().imageBytes(personImageBytes).build();

      byte[] productImageBytes = Files.readAllBytes(Paths.get("resources/sweater.jpg"));
      Image productImage = Image.builder().imageBytes(productImageBytes).build();

      RecontextImageResponse recontextImageResponse =
          client.models.recontextImage(
              modelId,
              RecontextImageSource.builder()
                  .personImage(personImage)
                  .productImages(ProductImage.builder().productImage(productImage).build())
                  .build(),
              null);

      Image generatedImage =
          recontextImageResponse
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
      // Created output image using 1637865 bytes
      return generatedImage;
    }
  }
}
// [END googlegenaisdk_imggen_virtual_try_on_with_txt_img]