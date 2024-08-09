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

// [START generativeaionvertexai_gemini_multiturn_chat]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;

public class ChatDiscussion {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";

    chatDiscussion(projectId, location, modelName);
  }

  // Ask interrelated questions in a row using a ChatSession object.
  public static void chatDiscussion(String projectId, String location, String modelName)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      GenerateContentResponse response;

      GenerativeModel model = new GenerativeModel(modelName, vertexAI);
      // Create a chat session to be used for interactive conversation.
      ChatSession chatSession = new ChatSession(model);

      response = chatSession.sendMessage("Hello.");
      System.out.println(ResponseHandler.getText(response));

      response = chatSession.sendMessage("What are all the colors in a rainbow?");
      System.out.println(ResponseHandler.getText(response));

      response = chatSession.sendMessage("Why does it appear when it rains?");
      System.out.println(ResponseHandler.getText(response));
      System.out.println("Chat Ended.");
    }
  }
}
// [END generativeaionvertexai_gemini_multiturn_chat]
