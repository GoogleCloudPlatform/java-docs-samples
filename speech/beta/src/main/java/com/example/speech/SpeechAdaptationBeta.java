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
// DO NOT EDIT! This is a generated sample ("Request",  "speech_adaptation_beta")
// sample-metadata:
//   title:
//   description: Performs synchronous speech recognition with speech adaptation.
//   usage: gradle run -PmainClass=com.google.cloud.examples.speech.v1p1beta1.SpeechAdaptationBeta
// [--args='[--sample_rate_hertz 44100] [--language_code "en-US"] [--phrase "Brooklyn Bridge"]
// [--boost 20.0] [--uri_path "gs://cloud-samples-data/speech/brooklyn_bridge.mp3"]']

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

public class SpeechAdaptationBeta {
  // [START speech_adaptation_beta]
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
   * Performs synchronous speech recognition with speech adaptation.
   *
   * @param sampleRateHertz Sample rate in Hertz of the audio data sent in all `RecognitionAudio`
   *     messages. Valid values are: 8000-48000.
   * @param languageCode The language of the supplied audio.
   * @param phrase Phrase "hints" help Speech-to-Text API recognize the specified phrases from your
   *     audio data.
   * @param boost Positive value will increase the probability that a specific phrase will be
   *     recognized over other similar sounding phrases.
   * @param uriPath Path to the audio file stored on GCS.
   */
  public static void sampleRecognize(
      int sampleRateHertz, String languageCode, String phrase, float boost, String uriPath) {
    try (SpeechClient speechClient = SpeechClient.create()) {
      // sampleRateHertz = 44100;
      // languageCode = "en-US";
      // phrase = "Brooklyn Bridge";
      // boost = 20.0F;
      // uriPath = "gs://cloud-samples-data/speech/brooklyn_bridge.mp3";
      RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.MP3;
      List<String> phrases = Arrays.asList(phrase);
      SpeechContext speechContextsElement =
          SpeechContext.newBuilder().addAllPhrases(phrases).setBoost(boost).build();
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
  // [END speech_adaptation_beta]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("sample_rate_hertz").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("language_code").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("phrase").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("boost").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("uri_path").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    int sampleRateHertz =
        cl.getOptionValue("sample_rate_hertz") != null
            ? Integer.parseInt(cl.getOptionValue("sample_rate_hertz"))
            : 44100;
    String languageCode = cl.getOptionValue("language_code", "en-US");
    String phrase = cl.getOptionValue("phrase", "Brooklyn Bridge");
    float boost =
        cl.getOptionValue("boost") != null ? Float.parseFloat(cl.getOptionValue("boost")) : 20.0F;
    String uriPath =
        cl.getOptionValue("uri_path", "gs://cloud-samples-data/speech/brooklyn_bridge.mp3");

    sampleRecognize(sampleRateHertz, languageCode, phrase, boost, uriPath);
  }
}
