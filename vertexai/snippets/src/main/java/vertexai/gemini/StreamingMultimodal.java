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

// [START generativeaionvertexai_stream_multimodality_basic]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;

public class StreamingMultimodal {
  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-google-cloud-project-id";
    String location = "us-central1";
    String modelName = "gemini-1.5-flash-001";

    streamingMultimodal(projectId, location, modelName);
  }

  // Ask a simple question and get the response via streaming.
  public static void streamingMultimodal(String projectId, String location, String modelName)
      throws Exception {
    // Initialize client that will be used to send requests.
    // This client only needs to be created once, and can be reused for multiple requests.
    try (VertexAI vertexAI = new VertexAI(projectId, location)) {
      GenerativeModel model = new GenerativeModel(modelName, vertexAI);

      String videoUri = "gs://cloud-samples-data/video/animals.mp4";
      String imgUri = "gs://cloud-samples-data/generative-ai/image/character.jpg";

      // Stream the result.
      model.generateContentStream(
          ContentMaker.fromMultiModalData(
              PartMaker.fromMimeTypeAndData("video/mp4", videoUri),
              PartMaker.fromMimeTypeAndData("image/jpeg", imgUri),
              "Are this video and image correlated?"
          ))
          .stream()
          .forEach(System.out::println);
    }
  }
}
// [END generativeaionvertexai_stream_multimodality_basic]