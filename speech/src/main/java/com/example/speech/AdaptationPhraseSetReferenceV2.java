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

// [START speech_adaptation_v2_phrase_set_reference]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v2.AutoDetectDecodingConfig;
import com.google.cloud.speech.v2.CreatePhraseSetRequest;
import com.google.cloud.speech.v2.OperationMetadata;
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
import java.util.concurrent.ExecutionException;

public class AdaptationPhraseSetReferenceV2 {
  public static void main(String[] args) throws IOException, InterruptedException,
      ExecutionException {
    String projectId = "my-project-id";
    String recognizerName = "projects/[PROJECT_ID]/locations/global/recognizers/[RECOGNIZER_ID]";
    String phraseSetId = "my-phrase-set-id";
    String audioFilePath = "path/to/audiofile";

    createPersistentPhraseSetV2(projectId, recognizerName, phraseSetId, audioFilePath);
  }

  public static void createPersistentPhraseSetV2(String projectId, String recognizerName,
      String phraseSetId, String audioFilePath) throws IOException, InterruptedException,
      ExecutionException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SpeechClient speechClient = SpeechClient.create()) {
      String parent = String.format("projects/%s/locations/global", projectId);

      // Create a persistent PhraseSet to reference in a recognition request
      Phrase.Builder phrase = Phrase.newBuilder()
          .setValue("Chromecast")
          .setBoost(20);

      PhraseSet.Builder phraseSetBuilder = PhraseSet.newBuilder()
          .addPhrases(phrase);

      CreatePhraseSetRequest createPhraseSetRequest = CreatePhraseSetRequest.newBuilder()
          .setParent(parent)
          .setPhraseSetId(phraseSetId)
          .setPhraseSet(phraseSetBuilder)
          .build();

      OperationFuture<PhraseSet, OperationMetadata> phraseOperation =
          speechClient.createPhraseSetAsync(createPhraseSetRequest);
      PhraseSet phraseSet = phraseOperation.get();

      System.out.printf("Phrase set name: %s\n", phraseSet.getName());

      // Transcribe audio using speech adaptation
      Path path = Paths.get(audioFilePath);
      byte[] data = Files.readAllBytes(path);
      ByteString audioBytes = ByteString.copyFrom(data);

      // Add a reference to the PhraseSet into the recognition request
      AdaptationPhraseSet.Builder adaptationPhraseSet = AdaptationPhraseSet.newBuilder()
          .setPhraseSet(phraseSet.getName());

      SpeechAdaptation.Builder adaptation = SpeechAdaptation.newBuilder()
          .addPhraseSets(adaptationPhraseSet);

      RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
          .setAutoDecodingConfig(AutoDetectDecodingConfig.newBuilder().build())
          .setAdaptation(adaptation)
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
// [END speech_adaptation_v2_phrase_set_reference]
