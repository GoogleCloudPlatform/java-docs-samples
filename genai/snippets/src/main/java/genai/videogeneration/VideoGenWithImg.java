/*
 * Copyright 2025 Google LLC
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

package genai.videogeneration;

// [START googlegenaisdk_videogen_with_img]

import com.google.genai.Client;
import com.google.genai.types.GenerateVideosConfig;
import com.google.genai.types.GenerateVideosOperation;
import com.google.genai.types.GenerateVideosResponse;
import com.google.genai.types.GeneratedVideo;
import com.google.genai.types.GetOperationConfig;
import com.google.genai.types.Image;
import com.google.genai.types.Video;
import java.util.concurrent.TimeUnit;

public class VideoGenWithImg {

  public static void main(String[] args) throws InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "veo-3.0-generate-preview";
    String outputGcsUri = "gs://your-bucket/your-prefix";
    generateContent(modelId, outputGcsUri);
  }

  // Generates a video with an image and a text prompt.
  public static String generateContent(String modelId, String outputGcsUri)
      throws InterruptedException {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client = Client.builder().location("global").vertexAI(true).build()) {

      GenerateVideosOperation operation =
          client.models.generateVideos(
              modelId,
              "Extreme close-up of a cluster of vibrant wildflowers"
                  + " swaying gently in a sun-drenched meadow.",
              Image.builder()
                  .gcsUri("gs://cloud-samples-data/generative-ai/image/flowers.png")
                  .mimeType("image/png")
                  .build(),
              GenerateVideosConfig.builder()
                  .aspectRatio("16:9")
                  .outputGcsUri(outputGcsUri)
                  .build());

      while (!operation.done().orElse(false)) {
        TimeUnit.SECONDS.sleep(15);
        operation =
            client.operations.getVideosOperation(operation, GetOperationConfig.builder().build());
      }

      String generatedVideoUri =
          operation
              .response()
              .flatMap(GenerateVideosResponse::generatedVideos)
              .flatMap(videos -> videos.stream().findFirst())
              .flatMap(GeneratedVideo::video)
              .flatMap(Video::uri)
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "Could not get the URI from the generated video"));

      System.out.println("Generated video URI: " + generatedVideoUri);
      // Example response:
      // Generated video URI: gs://your-bucket/your-prefix/generated-video-123.mp4
      return generatedVideoUri;
    }
  }
}
// [END googlegenaisdk_videogen_with_img]
