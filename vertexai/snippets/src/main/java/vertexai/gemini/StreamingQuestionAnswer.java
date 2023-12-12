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
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler;
import com.google.cloud.vertexai.generativeai.preview.ResponseStream;

public class StreamingQuestionAnswer {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-pro-vision";

    String output = streamingQuestion(projectId, location, modelName);
    System.out.println(output);
  }

  // Ask a simple question and get the response via streaming.
  public static String streamingQuestion(String projectId, String location, String modelName)
      throws Exception {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      StringBuilder output = new StringBuilder();
      GenerativeModel model = new GenerativeModel(modelName, vertexAI);

      ResponseStream<GenerateContentResponse> responseStream =
          model.generateContentStream("Why is the sky blue?");

      for (GenerateContentResponse respElement : responseStream) {
        output.append(ResponseHandler.getText(respElement));
      }
      return output.toString();
    }
  }
}
