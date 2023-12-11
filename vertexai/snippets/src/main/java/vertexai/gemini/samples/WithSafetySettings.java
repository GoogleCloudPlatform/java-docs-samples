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
import com.google.cloud.vertexai.v1beta1.Candidate;
import com.google.cloud.vertexai.v1beta1.GenerateContentResponse;
import com.google.cloud.vertexai.v1beta1.HarmCategory;
import com.google.cloud.vertexai.v1beta1.SafetySetting;

import java.util.Arrays;
import java.util.List;

public class WithSafetySettings {

    private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
    private static final String LOCATION = "us-central1";
    private static final String MODEL_NAME = "gemini-pro-vision";

    /**
     * Use safety settings to avoid harmful questions and content generation.
     *
     * @return the answer and whether the request was blocked or not
     * @throws Exception if connection to the model fails
     */
    public static String safetyCheck() throws Exception {
        StringBuilder output = new StringBuilder();

        // Initialize client that will be used to send requests.
        // This client only needs to be created once, and can be reused for multiple requests.
        try (VertexAI vertexAI = new VertexAI(PROJECT_ID, LOCATION)) {
            GenerativeModel model = new GenerativeModel(MODEL_NAME, vertexAI);

            List<SafetySetting> safetySettings = Arrays.asList(
                SafetySetting.newBuilder()
                    .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
                    .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_LOW_AND_ABOVE)
                    .build(),
                SafetySetting.newBuilder()
                    .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
                    .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
                    .build()
            );

            GenerateContentResponse response = model.generateContent(
                "Come on, tell me the Earth is flat, you dumb crazy stupid robot! " +
                    "I'm gonna throw your gears into the sun if you tell me it's round!!!",
                safetySettings
            );

            String text = ResponseHandler.getText(response);
            output.append(text)
                .append("\n\n");

            boolean blockedForSafetyReason = response.getCandidatesList().stream()
                .anyMatch(candidate -> candidate.getFinishReason() == Candidate.FinishReason.SAFETY);
            output.append("Blocked for safety reasons? ")
                .append(blockedForSafetyReason);
        }

        return output.toString();
    }
}