/*
 * Copyright 2024 Google LLC
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

// [START generativeaionvertexai_gemini_generate_from_text_input]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;

public class TextInput {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-2.0-flash-001";
    String textPrompt =
        "What's a good name for a flower shop that specializes in selling bouquets of"
            + " dried flowers?";

    String output = textInput(projectId, location, modelName, textPrompt);
    System.out.println(output);
  }

  // Passes the provided text input to the Gemini model and returns the text-only response.
  // For the specified textPrompt, the model returns a list of possible store names.
  public static String textInput(
      String projectId, String location, String modelName, String textPrompt) throws IOException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      GenerativeModel model = new GenerativeModel(modelName, vertexAI);

      GenerateContentResponse response = model.generateContent(textPrompt);
      String output = ResponseHandler.getText(response);
      return output;
    }
  }
}
// [END generativeaionvertexai_gemini_generate_from_text_input]
