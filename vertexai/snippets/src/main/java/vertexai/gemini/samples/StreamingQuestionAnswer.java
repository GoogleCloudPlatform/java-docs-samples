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
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler;
import com.google.cloud.vertexai.generativeai.preview.ResponseStream;
import com.google.cloud.vertexai.v1beta1.GenerateContentResponse;

public class StreamingQuestionAnswer {

    private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
    private static final String LOCATION = "us-central1";
    private static final String MODEL_NAME = "gemini-pro-vision";

    public static String streamingQuestion() throws Exception {
        StringBuilder output = new StringBuilder();

        try (VertexAI vertexAI = new VertexAI(PROJECT_ID, LOCATION)) {
            GenerativeModel model = new GenerativeModel(MODEL_NAME, vertexAI);

            ResponseStream<GenerateContentResponse> responseStream =
                model.generateContentStream("Why is the sky blue?");

            for (GenerateContentResponse respElement : responseStream) {
                output.append(ResponseHandler.getText(respElement));
            }
        }

        return output.toString();
    }
}