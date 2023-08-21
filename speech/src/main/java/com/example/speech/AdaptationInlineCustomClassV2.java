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

package com.example.speech;

// [START speech_adaptation_v2_inline_custom_class]
import com.google.cloud.speech.v2.AutoDetectDecodingConfig;
import com.google.cloud.speech.v2.CustomClass;
import com.google.cloud.speech.v2.PhraseSet;
import com.google.cloud.speech.v2.PhraseSet.Phrase;
import com.google.cloud.speech.v2.RecognitionConfig;
import com.google.cloud.speech.v2.RecognizeRequest;
import com.google.cloud.speech.v2.RecognizeResponse;
import com.google.cloud.speech.v2.SpeechAdaptation;
import com.google.cloud.speech.v2.SpeechAdaptation.AdaptationPhraseSet;
import com.google.cloud.speech.v2.SpeechClient;
import com.google.cloud.speech.v2.SpeechRecognitionAlternative;
import com.google.cloud.speech.v2.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class AdaptationInlineCustomClassV2 {
  public static void main(String[] args) throws IOException {
    String recognizerName = "projects/[PROJECT_ID]/locations/global/recognizers/[RECOGNIZER_ID]";
    String audioFilePath = "path/to/audioFile";

    buildInlineCustomClassV2(recognizerName, audioFilePath);
  }

  public static void buildInlineCustomClassV2(String recognizerName, String audioFilePath)
      throws IOException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SpeechClient speechClient = SpeechClient.create()) {
      
      // Create an inline phrase set to produce a more accurate transcript.
      CustomClass.ClassItem classItem = CustomClass.ClassItem.newBuilder()
          .setValue("Chromecast")
          .build();

      CustomClass customClass = CustomClass.newBuilder()
          .setName("chromecast")
          .addItems(classItem)
          .build();

      Phrase phrase = Phrase.newBuilder()
          .setBoost(20)
          .setValue("Chromecast")
          .build();

      PhraseSet phraseSet = PhraseSet.newBuilder()
          .addPhrases(phrase)
          .build();

      AdaptationPhraseSet adaptation = AdaptationPhraseSet.newBuilder()
          .setInlinePhraseSet(phraseSet)
          .build();
      
      SpeechAdaptation speechAdaptation = SpeechAdaptation.newBuilder()
          .addPhraseSets(adaptation)
          .addCustomClasses(customClass)
          .build();
      
      // Transcribe audio using speech adaptation
      Path path = Paths.get(audioFilePath);
      byte[] data = Files.readAllBytes(path);
      ByteString audioBytes = ByteString.copyFrom(data);

      RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
          .setAutoDecodingConfig(AutoDetectDecodingConfig.newBuilder().build())
          .setAdaptation(speechAdaptation)
          .build();

      RecognizeRequest request = RecognizeRequest.newBuilder()
          .setConfig(recognitionConfig)
          .setRecognizer(recognizerName)
          .setContent(audioBytes)
          .build();

      RecognizeResponse response = speechClient.recognize(request);
      List<SpeechRecognitionResult> results = response.getResultsList();

      for (SpeechRecognitionResult result : results) {
        // There can be several alternative transcripts for a given chunk of speech. Just use the
        // first (most likely) one here.
        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
        System.out.printf("Transcription: %s%n", alternative.getTranscript());
      }
    }
  }
}
// [END speech_adaptation_v2_inline_custom_class]