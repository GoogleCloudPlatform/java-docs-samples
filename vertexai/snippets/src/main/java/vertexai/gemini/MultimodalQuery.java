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

// [START generativeaionvertexai_gemini_pro_example]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.util.Base64;

public class MultimodalQuery {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";
    String dataImageBase64 = "your-base64-encoded-image";

    String output = multimodalQuery(projectId, location, modelName, dataImageBase64);
    System.out.println(output);
  }


  // Ask the model to recognise the brand associated with the logo image.
  public static String multimodalQuery(String projectId, String location, String modelName,
      String dataImageBase64) throws Exception {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      String output;
      byte[] imageBytes = Base64.getDecoder().decode(dataImageBase64);

      GenerativeModel model = new GenerativeModel(modelName, vertexAI);
      GenerateContentResponse response = model.generateContent(
          ContentMaker.fromMultiModalData(
              "What is this image?",
              PartMaker.fromMimeTypeAndData("image/png", imageBytes)
          ));

      output = ResponseHandler.getText(response);
      return output;
    }
  }
}
// [END generativeaionvertexai_gemini_pro_example]
