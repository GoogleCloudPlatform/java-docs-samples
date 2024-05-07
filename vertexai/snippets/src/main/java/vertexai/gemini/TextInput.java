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

/*
 * laishers. "A New Era in Cartoon History." Review of Steamboat Willie.
 * 4 January 2001. https://www.imdb.com/review/rw0005574/?ref_=rw_urv
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
    // Movie review from https://www.imdb.com/review/rw0005574/?ref_=rw_urv.
    // Does the returned sentiment score match the reviewer's movie rating?
    String textPrompt =
        "Give a score from 1 - 10 to suggest if the following movie review is negative or positive"
            + " (1 is most negative, 10 is most positive, 5 will be neutral). Include an"
            + " explanation.\n"
            + "This era was not just the dawn of sound in cartoons, but of a cartoon character"
            + " which would go down in history as the world's most famous mouse. Yes, Mickey makes"
            + " his debut here, in this cheery tale of life on board a steamboat. The animation is"
            + " good for it's time, and the plot - though a little simple - is quite jolly. A true"
            + " classic, and if you ever manage to get it on video, you won't regret it.";

    String output = textInput(projectId, location, modelName, textPrompt);
    System.out.println(output);
  }

  // Analyzes the provided text input.
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
