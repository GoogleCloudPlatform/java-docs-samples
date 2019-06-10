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
// DO NOT EDIT! This is a generated sample ("Request",  "speech_quickstart_beta")
// sample-metadata:
//   title:
//   description: Performs synchronous speech recognition on an audio file.
//   usage: gradle run -PmainClass=com.google.cloud.examples.speech.v1p1beta1.SpeechQuickstartBeta
// [--args='[--sample_rate_hertz 44100] [--language_code "en-US"] [--uri_path
// "gs://cloud-samples-data/speech/brooklyn_bridge.mp3"]']

package com.example.speech;

import com.google.cloud.speech.v1p1beta1.RecognitionAudio;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.RecognizeRequest;
import com.google.cloud.speech.v1p1beta1.RecognizeResponse;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionResult;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class SpeechQuickstartBeta {
  // [START speech_quickstart_beta]
  /*
   * Please include the following imports to run this sample.
   *
   * import com.google.cloud.speech.v1p1beta1.RecognitionAudio;
   * import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
   * import com.google.cloud.speech.v1p1beta1.RecognizeRequest;
   * import com.google.cloud.speech.v1p1beta1.RecognizeResponse;
   * import com.google.cloud.speech.v1p1beta1.SpeechClient;
   * import com.google.cloud.speech.v1p1beta1.SpeechRecognitionResult;
   */

  /**
   * Performs synchronous speech recognition on an audio file.
   *
   * @param sampleRateHertz Sample rate in Hertz of the audio data sent in all `RecognitionAudio`
   *     messages. Valid values are: 8000-48000.
   * @param languageCode The language of the supplied audio.
   * @param uriPath Path to the audio file stored on GCS.
   */
  public static void sampleRecognize(int sampleRateHertz, String languageCode, String uriPath) {
    try (SpeechClient speechClient = SpeechClient.create()) {
      // sampleRateHertz = 44100;
      // languageCode = "en-US";
      // uriPath = "gs://cloud-samples-data/speech/brooklyn_bridge.mp3";
      RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.MP3;
      RecognitionConfig config =
          RecognitionConfig.newBuilder()
              .setEncoding(encoding)
              .setSampleRateHertz(sampleRateHertz)
              .setLanguageCode(languageCode)
              .build();
      RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(uriPath).build();
      RecognizeRequest request =
          RecognizeRequest.newBuilder().setConfig(config).setAudio(audio).build();
      RecognizeResponse response = speechClient.recognize(request);
      for (SpeechRecognitionResult result : response.getResultsList()) {
        String transcript = result.getAlternativesList().get(0).getTranscript();
        System.out.printf("Transcript: %s\n", transcript);
      }
    } catch (Exception exception) {
      System.err.println("Failed to create the client due to: " + exception);
    }
  }
  // [END speech_quickstart_beta]

  public static void main(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("sample_rate_hertz").build());
    options.addOption(
        Option.builder("").required(false).hasArg(true).longOpt("language_code").build());
    options.addOption(Option.builder("").required(false).hasArg(true).longOpt("uri_path").build());

    CommandLine cl = (new DefaultParser()).parse(options, args);
    int sampleRateHertz =
        cl.getOptionValue("sample_rate_hertz") != null
            ? Integer.parseInt(cl.getOptionValue("sample_rate_hertz"))
            : 44100;
    String languageCode = cl.getOptionValue("language_code", "en-US");
    String uriPath =
        cl.getOptionValue("uri_path", "gs://cloud-samples-data/speech/brooklyn_bridge.mp3");

    sampleRecognize(sampleRateHertz, languageCode, uriPath);
  }
}
