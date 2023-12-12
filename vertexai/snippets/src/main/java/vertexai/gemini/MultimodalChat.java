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

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.preview.ChatSession;
import com.google.cloud.vertexai.generativeai.preview.ContentMaker;
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.PartMaker;
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler;
import java.util.Base64;

public class MultimodalChat {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-ultra-vision";
    String dataImagePngBase64 = "your-image-png-base64-encoded";

    String output = multimodalChat(projectId, location, modelName, dataImagePngBase64);
    System.out.println(output);
  }

  // Conversation asking the model to recognise the brand associated with a logo,
  // then ask which products are made by that brand.
  public static String multimodalChat(String projectId, String location, String modelName,
      String dataImagePngBase64) throws Exception {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      StringBuilder output = new StringBuilder();
      byte[] imageBytes = Base64.getDecoder().decode(dataImagePngBase64);

      GenerativeModel model = new GenerativeModel(modelName, vertexAI);
      ChatSession chatSession = new ChatSession(model);

      GenerateContentResponse response;
      response = chatSession.sendMessage(ContentMaker.fromMultiModalData(
          "What brand does the following logo represent?",
          PartMaker.fromMimeTypeAndData("image/png", imageBytes)
      ));
      output.append("Answer: ")
          .append(ResponseHandler.getText(response))
          .append("\n\n");

      response = chatSession.sendMessage("Give me 3 examples of products from that brand: ");
      output.append("Answer: ")
          .append(ResponseHandler.getText(response));

      return output.toString();
    }
  }
}
