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
package vertexai.gemini.samples;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.preview.ChatSession;
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler;
import com.google.cloud.vertexai.v1beta1.GenerateContentResponse;
import com.google.cloud.vertexai.v1beta1.GenerationConfig;

import java.io.IOException;

public class ChatDiscussion {

    private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
    private static final String LOCATION = "us-central1";
    private static final String MODEL_NAME = "gemini-pro-vision";

    /**
     * Ask three interrelated questions in a row using a <code>ChatSession</code>.
     *
     * @return the answers to the questions
     * @throws IOException if the connection to the model fails
     */
    public static String threeQuestions() throws IOException {
        StringBuilder output = new StringBuilder();

        // Initialize client that will be used to send requests.
        // This client only needs to be created once, and can be reused for multiple requests.
        try (VertexAI vertexAI = new VertexAI(PROJECT_ID, LOCATION)) {
            GenerativeModel model = new GenerativeModel(MODEL_NAME, vertexAI);

            ChatSession chatSession = new ChatSession(model);

            GenerateContentResponse response;

            response = chatSession.sendMessage("What are large language models?");
            output.append(ResponseHandler.getText(response));

            output.append("\n===================\n");
            response = chatSession.sendMessage("How do they work?");
            output.append(ResponseHandler.getText(response));

            output.append("\n===================\n");
            response = chatSession.sendMessage("Can you please name some of them?");
            output.append(ResponseHandler.getText(response));

            return output.toString();
        }
    }
}