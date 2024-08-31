/*
 * Copyright 2023 Google LLC
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

package vertexai.gemini;

// [START generativeaionvertexai_gemini_single_turn_multi_image]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MultimodalMultiImage {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";

    multimodalMultiImage(projectId, location, modelName);
  }

  // Generates content from multiple input images.
  public static void multimodalMultiImage(String projectId, String location, String modelName)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      GenerativeModel model = new GenerativeModel(modelName, vertexAI);

      Content content = ContentMaker.fromMultiModalData(
          PartMaker.fromMimeTypeAndData("image/png", readImageFile(
              "https://storage.googleapis.com/cloud-samples-data/vertex-ai/llm/prompts/landmark1.png")),
          "city: Rome, Landmark: the Colosseum",
          PartMaker.fromMimeTypeAndData("image/png", readImageFile(
              "https://storage.googleapis.com/cloud-samples-data/vertex-ai/llm/prompts/landmark2.png")),
          "city: Beijing, Landmark: Forbidden City",
          PartMaker.fromMimeTypeAndData("image/png", readImageFile(
              "https://storage.googleapis.com/cloud-samples-data/vertex-ai/llm/prompts/landmark3.png"))
      );

      GenerateContentResponse response = model.generateContent(content);

      String output = ResponseHandler.getText(response);
      System.out.println(output);
    }
  }

  // Reads the image data from the given URL.
  public static byte[] readImageFile(String url) throws IOException {
    URL urlObj = new URL(url);
    HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
    connection.setRequestMethod("GET");

    int responseCode = connection.getResponseCode();

    if (responseCode == HttpURLConnection.HTTP_OK) {
      InputStream inputStream = connection.getInputStream();
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }

      return outputStream.toByteArray();
    } else {
      throw new RuntimeException("Error fetching file: " + responseCode);
    }
  }
}
// [END generativeaionvertexai_gemini_single_turn_multi_image]
