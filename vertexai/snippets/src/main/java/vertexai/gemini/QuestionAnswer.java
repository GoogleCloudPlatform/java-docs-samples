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

// [START generativeaionvertexai_non_stream_text_basic]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;

public class QuestionAnswer {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";

    String output = simpleQuestion(projectId, location, modelName);
    System.out.println(output);
  }

  // Asks a question to the specified Vertex AI Gemini model and returns the generated answer.
  public static String simpleQuestion(String projectId, String location, String modelName)
      throws Exception {
    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      String output;
      GenerativeModel model = new GenerativeModel(modelName, vertexAI);
      // Send the question to the model for processing.
      GenerateContentResponse response = model.generateContent("Why is the sky blue?");
      // Extract the generated text from the model's response.
      output = ResponseHandler.getText(response);
      return output;
    }
  }
}
// [END generativeaionvertexai_non_stream_text_basic]
