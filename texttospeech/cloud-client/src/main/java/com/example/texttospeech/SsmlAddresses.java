/*
 * Copyright 2019 Google Inc.
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

package com.example.texttospeech;

// [START tts_ssml_address_imports]
// Imports the Google Cloud client library
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.common.html.HtmlEscapers;
import com.google.protobuf.ByteString;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
// [END tts_ssml_address_imports]


/**
 * Google Cloud TextToSpeech API sample application.
 * Example usage: mvn package exec:java
 *                    -Dexec.mainClass='com.example.texttospeech.SsmlAddresses
 */
public class SsmlAddresses {

  // [START tts_ssml_address_audio]
  public static void ssmlToAudio(String ssmlText, String outfile)
      throws Exception {
    // Instantiates a client
    try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
      // Set the ssml text input to synthesize
      SynthesisInput input = SynthesisInput.newBuilder()
           .setSsml(ssmlText)
           .build();

      // Build the voice request, select the language code ("en-US") and the ssml voice gender
      // ("neutral")
      VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
          .setLanguageCode("en-US")
          .setSsmlGender(SsmlVoiceGender.MALE)
          .build();

      // Select the type of audio file you want returned
      AudioConfig audioConfig = AudioConfig.newBuilder()
          .setAudioEncoding(AudioEncoding.MP3)
          .build();

      // Perform the text-to-speech request on the text input with the selected voice parameters and
      // audio file type
      SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice,
          audioConfig);

      // Get the audio contents from the response
      ByteString audioContents = response.getAudioContent();

      // Write the response to the output file.
      try (OutputStream out = new FileOutputStream(outfile)) {
        out.write(audioContents.toByteArray());
        System.out.println("Audio content written to file " + outfile);
      }
    }
    // [END tts_ssml_address_audio]

  }

  // [START tts_ssml_address_ssml]
  public static String textToSsml(String infile)
      throws Exception {

    String rawLines = new String(Files.readAllBytes(Paths.get(infile)));

    String escapedLines = HtmlEscapers.htmlEscaper().escape(rawLines);

    String expandedNewline = escapedLines.replaceAll("\\n","\n<break time='2s'/>");
    String ssml = "<speak>" + expandedNewline + "</speak>";

    return ssml;
  }
  // [START tts_ssml_address_ssml]

  /**
   * Demonstrates using the Text-to-Speech API.
   */
  public static void main(String... args) throws Exception {
    // test example address file
    String infile = "resources/addresses.txt";
    String outfile = "resources/addresses.mp3";

    String ssml = textToSsml(infile);
    ssmlToAudio(ssml, outfile);
  }
}