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
    String modelName = "gemini-1.0-pro-002";
    // Does the returned sentiment score match the reviewer's movie rating?
    String textPrompt =
        "Give a score from 1 - 10 to suggest if the following movie review is"
            + " negative or positive (1 is most negative, 10 is most positive, 5 will be"
            + " neutral). Include an explanation.\n"
            + " The movie takes some time to build, but that is part of its beauty. By the"
            + " time you are hooked, this tale of friendship and hope is thrilling and"
            + " affecting, until the very last scene. You will find yourself rooting for"
            + " the hero every step of the way. This is the sharpest, most original"
            + " animated film I have seen in years. I would give it 8 out of 10 stars.";

    String output = textInput(projectId, location, modelName, textPrompt);
    System.out.println(output);
  }

  // Passes the provided text input to the Gemini model and returns the text-only response.
  // For the specified textPrompt, the model returns a sentiment score for the given movie review.
  public static String textInput(
      String projectId, String location, String modelName, String textPrompt) throws IOException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      String output;
      GenerativeModel model = new GenerativeModel(modelName, vertexAI);

      GenerateContentResponse response = model.generateContent(textPrompt);
      output = ResponseHandler.getText(response);
      return output;
    }
  }
}
// [END generativeaionvertexai_gemini_generate_from_text_input]
