/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// DO NOT EDIT! This is a generated sample ("Request",  "speech_contexts_classes_beta")
// sample-metadata:
//   title:
//   description: Performs synchronous speech recognition with static context classes.
//   usage: gradle run
// -PmainClass=com.google.cloud.examples.speech.v1p1beta1.SpeechContextsClassesBeta
// [--args='[--sample_rate_hertz 24000] [--language_code "en-US"] [--phrase "$TIME"] [--uri_path
// "gs://cloud-samples-data/speech/time.mp3"]']

package com.example.speech;

import com.google.cloud.speech.v1p1beta1.RecognitionAudio;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.RecognizeRequest;
import com.google.cloud.speech.v1p1beta1.RecognizeResponse;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechContext;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionResult;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class SpeechContextsClassesBeta {
  // [START speech_contexts_classes_beta]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.cloud.speech.v1p1beta1.RecognitionAudio;
   * import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
   * import com.google.cloud.speech.v1p1beta1.RecognizeRequest;
   * import com.google.cloud.speech.v1p1beta1.RecognizeResponse;
   * import com.google.cloud.speech.v1p1beta1.SpeechClient;
   * import com.google.cloud.speech.v1p1beta1.SpeechContext;
   * import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
   * import com.google.cloud.speech.v1p1beta1.SpeechRecognitionResult;
   * import java.util.Arrays;
   * import java.util.List;
   */

  /**
   * Performs synchronous speech recognition with static context classes.
   *
   * @param sampleRateHertz Sample rate in Hertz of the audio data sent in all `RecognitionAudio`
   *     messages. Valid values are: 8000-48000.
   * @param languageCode The language of the supplied audio.
   * @param phrase Phrase "hints" help Speech-to-Text API recognize the specified phrases from your
   *     audio data. In this sample we are using a static class phrase ($TIME). Classes represent
   *     groups of words that represent common concepts that occur in natural language. We recommend
   *     checking out the docs page for more info on static classes.
   * @param uriPath Path to the audio file stored on GCS.
   */
  public static void sampleRecognize(
      int sampleRateHertz, String languageCode, String phrase, String uriPath) {
    try (SpeechClient speechClient = SpeechClient.create()) {
      // sampleRateHertz = 24000;
      // languageCode = "en-US";
      // phrase = "$TIME";
      // uriPath = "gs://cloud-samples-data/speech/time.mp3";
      RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.MP3;
      List<String> phrases = Arrays.asList(phrase);
      SpeechContext speechContextsElement =
          SpeechContext.newBuilder().addAllPhrases(phrases).build();
      List<SpeechContext> speechContexts = Arrays.asList(speechContextsElement);
      RecognitionConfig config =
          RecognitionConfig.newBuilder()
              .setEncoding(encoding)
              .setSampleRateHertz(sampleRateHertz)
              .setLanguageCode(languageCode)
              .addAllSpeechContexts(speechContexts)
              .build();
      RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(uriPath).build();
      RecognizeRequest request =
          RecognizeRequest.newBuilder().setConfig(config).setAudio(audio).build();
      RecognizeResponse response = speechClient.recognize(request);
      for (SpeechRecognitionResult result : response.getResultsList()) {
        // First alternative is the most probable result
        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
        System.out.printf("Transcript: %s\n", alternative.getTranscript());
      }
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END speech_contexts_classes_beta]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("sample_rate_hertz").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("language_code").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("phrase").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("uri_path").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    int sampleRateHertz =
        cl.getOptionValue("sample_rate_hertz") != null
            ? Integer.parseInt(cl.getOptionValue("sample_rate_hertz"))
            : 24000;
    String languageCode = cl.getOptionValue("language_code", "en-US");
    String phrase = cl.getOptionValue("phrase", "$TIME");
    String uriPath = cl.getOptionValue("uri_path", "gs://cloud-samples-data/speech/time.mp3");

    sampleRecognize(sampleRateHertz, languageCode, phrase, uriPath);
  }
}
