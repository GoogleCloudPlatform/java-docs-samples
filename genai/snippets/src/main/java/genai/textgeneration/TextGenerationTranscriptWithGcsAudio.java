/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package genai.textgeneration;

// [START googlegenaisdk_textgen_transcript_with_gcs_audio]

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;

public class TextGenerationTranscriptWithGcsAudio {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    generateContent(modelId);
  }

  // Generates transcript with audio input
  public static String generateContent(String modelId) {
    // Client Initialization. Once created, it can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      String prompt =
          "Transcribe the interview, in the format of timecode, speaker, caption.\n"
              + "Use speaker A, speaker B, etc. to identify speakers.";

      // Enable audioTimestamp to generate timestamps for audio-only files.
      GenerateContentConfig contentConfig =
          GenerateContentConfig.builder().audioTimestamp(true).build();

      GenerateContentResponse response =
          client.models.generateContent(
              modelId,
              Content.fromParts(
                  Part.fromUri(
                      "gs://cloud-samples-data/generative-ai/audio/pixel.mp3", "audio/mpeg"),
                  Part.fromText(prompt)),
              contentConfig);

      System.out.print(response.text());
      // Example response:
      // 00:00 - Speaker A: your devices are getting better over time. And so we think about it...
      // 00:14 - Speaker B: Welcome to the Made by Google Podcast, where we meet the people who...
      // 00:41 - Speaker A: So many features. I am a singer, so I actually think recorder...
      return response.text();
    }
  }
}
// [END googlegenaisdk_textgen_transcript_with_gcs_audio]