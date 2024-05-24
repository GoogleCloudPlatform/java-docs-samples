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

// [START generativeaionvertexai_gemini_all_modalities]

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.io.IOException;

public class MultimodalAllInput {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";

    multimodalAllInput(projectId, location, modelName);
  }

  // A request containing a text prompt, a video, and a picture.
  public static String multimodalAllInput(String projectId, String location, String modelName)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs
    // to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      String videoUri = "gs://cloud-samples-data/generative-ai/video/behind_the_scenes_pixel.mp4";
      String imageUri = "gs://cloud-samples-data/generative-ai/image/a-man-and-a-dog.png";

      GenerativeModel model = new GenerativeModel(modelName, vertexAI);
      GenerateContentResponse response = model.generateContent(
          ContentMaker.fromMultiModalData(
              PartMaker.fromMimeTypeAndData("video/mp4", videoUri),
              PartMaker.fromMimeTypeAndData("image/png", imageUri),
              "Watch each frame in the video carefully and answer the questions.\n"
                  + "Only base your answers strictly on what information is available in "
                  + "the video attached. Do not make up any information that is not part "
                  + "of the video and do not be too verbose, be to the point.\n\n"
                  + "Questions:\n"
                  + "- When is the moment in the image happening in the video? "
                  + "Provide a timestamp.\n"
                  + "- What is the context of the moment and what does the narrator say about it?"
          ));

      String output = ResponseHandler.getText(response);
      System.out.println(output);

      return output;
    }
  }
}
// [END generativeaionvertexai_gemini_all_modalities]
