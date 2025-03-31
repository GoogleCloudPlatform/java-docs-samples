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

// [START generativeaionvertexai_gemini_get_started]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import java.io.IOException;

public class Quickstart {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";

    String output = quickstart(projectId, location, modelName);
    System.out.println(output);
  }

  // Analyzes the provided Multimodal input.
  public static String quickstart(String projectId, String location, String modelName)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      String imageUri = "gs://generativeai-downloads/images/scones.jpg";

      GenerativeModel model = new GenerativeModel(modelName, vertexAI);
      GenerateContentResponse response = model.generateContent(ContentMaker.fromMultiModalData(
          PartMaker.fromMimeTypeAndData("image/png", imageUri),
          "What's in this photo"
      ));

      return response.toString();
    }
  }
}
// [END generativeaionvertexai_gemini_get_started]
