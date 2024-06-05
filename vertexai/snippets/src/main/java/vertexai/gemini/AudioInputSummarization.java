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

// [START generativeaionvertexai_gemini_audio_summarization]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;

public class AudioInputSummarization {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";

    summarizeAudio(projectId, location, modelName);
  }

  // Analyzes the given audio input.
  public static String summarizeAudio(String projectId, String location, String modelName)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      String audioUri = "gs://cloud-samples-data/generative-ai/audio/pixel.mp3";

      GenerativeModel model = new GenerativeModel(modelName, vertexAI);
      GenerateContentResponse response = model.generateContent(
          ContentMaker.fromMultiModalData(
              "Please provide a summary for the audio.\n"
                  + "Provide chapter titles with timestamps, be concise and short, "
                  + "no need to provide chapter summaries.\n"
                  + "Do not make up any information that is not part of the audio "
                  + "and do not be verbose.",
              PartMaker.fromMimeTypeAndData("audio/mp3", audioUri)
          ));

      String output = ResponseHandler.getText(response);
      System.out.println(output);

      return output;
    }
  }
}
// [END generativeaionvertexai_gemini_audio_summarization]
