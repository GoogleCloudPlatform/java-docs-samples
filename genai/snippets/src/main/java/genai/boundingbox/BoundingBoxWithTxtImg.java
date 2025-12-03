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

package genai.boundingbox;

// [START googlegenaisdk_boundingbox_with_txt_img]

import static com.google.genai.types.Type.Known.ARRAY;
import static com.google.genai.types.Type.Known.INTEGER;
import static com.google.genai.types.Type.Known.OBJECT;
import static com.google.genai.types.Type.Known.STRING;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HarmBlockThreshold;
import com.google.genai.types.HarmCategory;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import com.google.genai.types.SafetySetting;
import com.google.genai.types.Schema;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class BoundingBoxWithTxtImg {

  public static class BoundingBox {
    List<Integer> box2d;
    String label;

    public List<Integer> getBox2d() {
      return box2d;
    }

    public String getLabel() {
      return label;
    }
  }

  // Plot bounding boxes on an image and save it to a file.
  public static void plotBoundingBoxes(String imageUrl, List<BoundingBox> boundingBoxes)
      throws IOException {
    URL url = new URL(imageUrl);
    BufferedImage image = ImageIO.read(url);

    int width = image.getWidth();
    int height = image.getHeight();

    Graphics2D graphics2D = image.createGraphics();
    graphics2D.setStroke(new BasicStroke(4));
    graphics2D.setFont(new Font("Arial", Font.PLAIN, 18));

    // Define a list of colors to cycle through.
    List<Color> colors =
        Arrays.asList(
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.CYAN,
            Color.MAGENTA,
            Color.ORANGE);

    for (int i = 0; i < boundingBoxes.size(); i++) {
      BoundingBox boundingBox = boundingBoxes.get((i));
      List<Integer> box2d = boundingBox.getBox2d();

      // Scale normalized coordinates (0-1000) to image dimensions.
      int absYMin = (int) (box2d.get(0) / 1000.0 * height);
      int absXMin = (int) (box2d.get(1) / 1000.0 * width);
      int absYMax = (int) (box2d.get(2) / 1000.0 * height);
      int absXMax = (int) (box2d.get(3) / 1000.0 * width);

      Color color = colors.get(i % colors.size());
      graphics2D.setColor(color);

      // Draw the rectangle.
      graphics2D.drawRect(absXMin, absYMin, absXMax - absXMin, absYMax - absYMin);

      // Draw the label text.
      if (boundingBox.getLabel() != null && !boundingBox.getLabel().isEmpty()) {
        graphics2D.drawString(boundingBox.getLabel(), absXMin + 8, absYMin + 20);
      }
    }
    graphics2D.dispose();

    // Write the image to a file.
    String outputFilePath = "resources/output/bounding-boxes-socks.jpg";
    ImageIO.write(image, "jpg", new File(outputFilePath));
    System.out.println("Successfully saved image to: " + outputFilePath);
  }

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String model = "gemini-2.5-flash";
    generateContent(model);
  }

  // Shows how to send a multimodal prompt to the model and get a structured JSON response
  // containing bounding box data, and then uses that data to draw the boxes on the original
  // image, saving it to a new file.
  public static String generateContent(String modelId) throws IOException {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .vertexAI(true)
            .build()) {

      String systemInstruction =
          "Return bounding boxes as an array with labels.\n"
              + " Never return masks. Limit to 25 objects.\n"
              + " If an object is present multiple times, give each object a unique label\n"
              + " according to its distinct characteristics (colors, size, position, etc..).";

      // Define the response schema.
      Schema responseSchema =
          Schema.builder()
              .type(ARRAY)
              .items(
                  Schema.builder()
                      .type(OBJECT)
                      .properties(
                          Map.of(
                              "box2d",
                              Schema.builder()
                                  .type(ARRAY)
                                  .items(Schema.builder().type(INTEGER).build())
                                  .build(),
                              "label",
                              Schema.builder().type(STRING).build()))
                      .required("box2d", "label")
                      .build())
              .build();

      // Define the GenerateContentConfig and set the response schema.
      GenerateContentConfig contentConfig =
          GenerateContentConfig.builder()
              .systemInstruction(Content.fromParts(Part.fromText(systemInstruction)))
              .temperature(0.5F)
              .safetySettings(
                  SafetySetting.builder()
                      .category(HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT)
                      .threshold(HarmBlockThreshold.Known.BLOCK_ONLY_HIGH)
                      .build())
              .responseMimeType("application/json")
              .responseSchema(responseSchema)
              .build();

      String imageUri = "https://storage.googleapis.com/generativeai-downloads/images/socks.jpg";
      URL url = new URL(imageUri);

      try (InputStream inputStream = url.openStream()) {
        byte[] imageBytes = inputStream.readAllBytes();

        String prompt =
            "Output the positions of the socks with a face."
                + " Label according to position in the image";

        GenerateContentResponse response =
            client.models.generateContent(
                modelId,
                Content.fromParts(Part.fromBytes(imageBytes, "image/jpeg"), Part.fromText(prompt)),
                contentConfig);

        System.out.println(response.text());
        // Example response:
        // [
        //  {"box2d": [24, 24, 521, 526], "label": "top left light blue cat face sock"},
        //  {"box2d": [238, 627, 649, 863], "label": "top right light blue cat face sock"}
        // ]

        // Use Gson to parse the JSON string into a list of BoundingBox objects.
        Gson gson = new Gson();
        Type boundingBoxListType = new TypeToken<List<BoundingBox>>() {}.getType();
        List<BoundingBox> boundingBoxes = gson.fromJson(response.text(), boundingBoxListType);

        // Plot the bounding boxes on the image.
        if (boundingBoxes != null) {
          plotBoundingBoxes(imageUri, boundingBoxes);
        }

        return response.text();
      }
    }
  }
}
// [END googlegenaisdk_boundingbox_with_txt_img]
