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

// [START generativeaionvertexai_gemini_system_instruction]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;

public class WithSystemInstruction {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";

    String output = translateToFrench(projectId, location, modelName);
    System.out.println(output);
  }

  // Ask the model to translate from English to French with a system instruction.
  public static String translateToFrench(String projectId, String location, String modelName)
      throws Exception {
    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      String output;

      GenerativeModel model = new GenerativeModel(modelName, vertexAI)
          .withSystemInstruction(ContentMaker.fromString("You are a helpful assistant.\n"
            + "Your mission is to translate text in English to French."));

      GenerateContentResponse response = model.generateContent("User input: I like bagels.\n"
          + "Answer:");
      output = ResponseHandler.getText(response);
      return output;
    }
  }
}
// [END generativeaionvertexai_gemini_system_instruction]