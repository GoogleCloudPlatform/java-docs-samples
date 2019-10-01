/*
 * Copyright 2019 Google LLC
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

package com.example.translate;

// [START translate_hybrid_imports]
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;

import com.google.cloud.translate.v3beta1.CreateGlossaryRequest;
import com.google.cloud.translate.v3beta1.GcsSource;
import com.google.cloud.translate.v3beta1.Glossary;
import com.google.cloud.translate.v3beta1.Glossary.LanguageCodesSet;
import com.google.cloud.translate.v3beta1.GlossaryInputConfig;
import com.google.cloud.translate.v3beta1.GlossaryName;
import com.google.cloud.translate.v3beta1.LocationName;
import com.google.cloud.translate.v3beta1.TranslateTextGlossaryConfig;
import com.google.cloud.translate.v3beta1.TranslateTextRequest;
import com.google.cloud.translate.v3beta1.TranslateTextResponse;
import com.google.cloud.translate.v3beta1.TranslationServiceClient;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;

import com.google.common.html.HtmlEscapers;
import com.google.protobuf.ByteString;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
// [END translate_hybrid_imports]

public class HybridGlossaries {

  // [START translate_hybrid_vision]
  static String picToText(String filePath) {
    // String filePath = "resources/example.png";

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      ByteString imageBytes = ByteString.readFrom(new FileInputStream(filePath));
      Image image = Image.newBuilder().setContent(imageBytes).build();
      Feature feature = Feature.newBuilder().setType(Type.DOCUMENT_TEXT_DETECTION).build();
      AnnotateImageRequest request =
          AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();

      BatchAnnotateImagesResponse response = client.batchAnnotateImages(Arrays.asList(request));
      StringBuilder result = new StringBuilder();
      for (AnnotateImageResponse res : response.getResponsesList()) {
        result.append(String.format("%s\n", res.getFullTextAnnotation().getText()));
      }

      return result.toString();
    } catch (IOException e) {
      return String.format("Failed to get the text from the image.\n%s\n", e.getMessage());
    }
  }
  // [END translate_hybrid_vision]

  // [START translate_hybrid_create_glossary]
  // Creates a GCP glossary resource (Assumes you've already manually uploaded a glossary to Cloud
  // Storage).
  static void createGlossary(String projectId, String glossaryDisplayName) {
    // String projectId = "YOUR_PROJECT_ID";
    // String glossaryDisplayName = "bistro-glossary";

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (TranslationServiceClient translationServiceClient = TranslationServiceClient.create()) {
      LanguageCodesSet languageCodesSet =
          LanguageCodesSet.newBuilder().addAllLanguageCodes(Arrays.asList("fr", "en")).build();
      GcsSource gcsSource =
          GcsSource.newBuilder()
              .setInputUri("gs://cloud-samples-data/translation/bistro_glossary.csv")
              .build();
      GlossaryInputConfig glossaryInputConfig =
          GlossaryInputConfig.newBuilder().setGcsSource(gcsSource).build();

      GlossaryName glossaryName =
          GlossaryName.newBuilder()
              .setProject(projectId)
              .setLocation("us-central1")
              .setGlossary(glossaryDisplayName)
              .build();
      Glossary glossary =
          Glossary.newBuilder()
              .setLanguageCodesSet(languageCodesSet)
              .setInputConfig(glossaryInputConfig)
              .setName(glossaryName.toString())
              .build();
      CreateGlossaryRequest request =
          CreateGlossaryRequest.newBuilder()
              .setParent(LocationName.of(projectId, "us-central1").toString())
              .setGlossary(glossary)
              .build();
      // Call the API
      Glossary response =
          translationServiceClient.createGlossaryAsync(request).get(300, TimeUnit.SECONDS);
      System.out.format("Created: %s\n", response.getName());
    } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
      System.out.format("No new glossary was created.\n%s\n", e.getMessage());
    }
  }
  // [END translate_hybrid_create_glossary]

  // [START translate_hybrid_translate]
  // Translates text to a given language using a glossary
  static String translateText(String projectId, String text, String glossaryDisplayName) {
    // String projectId = "YOUR_PROJECT_ID";
    // String text = "String of text to translate";
    // String glossaryDisplayName = "bistro_glossary";

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (TranslationServiceClient translationServiceClient = TranslationServiceClient.create()) {
      GlossaryName glossaryId =
          GlossaryName.newBuilder()
              .setProject(projectId)
              .setLocation("us-central1")
              .setGlossary(glossaryDisplayName)
              .build();
      TranslateTextGlossaryConfig translateTextGlossaryConfig =
          TranslateTextGlossaryConfig.newBuilder().setGlossary(glossaryId.toString()).build();
      TranslateTextRequest translateTextRequest =
          TranslateTextRequest.newBuilder()
              .setParent(LocationName.of(projectId, "us-central1").toString())
              .setMimeType("text/plain")
              .setSourceLanguageCode("fr")
              .setTargetLanguageCode("en")
              .addContents(text)
              .setGlossaryConfig(translateTextGlossaryConfig)
              .build();
      // Call the API
      TranslateTextResponse response = translationServiceClient.translateText(translateTextRequest);
      return response.getGlossaryTranslationsList().get(0).getTranslatedText();
    } catch (IOException e) {
      throw new RuntimeException("Couldn't create client.", e);
    }
  }
  // [END translate_hybrid_translate]

  // [START translate_hybrid_tts]
  /**
   * Generates synthetic audio from plaintext.
   *
   * <p>Given a string of plaintext, this function converts the contents of the input text file into
   * a String of tagged SSML text. This function formats the SSML String so that, when synthesized,
   * the synthetic audio will pause for two seconds between each line of the text file. This
   * function also handles special text characters which might interfere with SSML commands. Given a
   * string of SSML text and an output file name, this function calls the Text-to-Speech API. The
   * API returns a synthetic audio version of the text, formatted according to the SSML commands.
   * This function saves the synthetic audio to the designated output file.
   */
  static void textToAudio(String text, String outFile) {
    // String text = "Text to convert to audio";
    // String outFile = "path_to_save_output_to.mp3";

    // Replace special characters with HTML Ampersand Character Codes. These codes prevent the API
    // from confusing text with SSML tags
    // For example, '<' is replaced with '&lt;' and '&' is replaced with '&amp;'
    String escapedLines = HtmlEscapers.htmlEscaper().escape(text);

    // Convert plaintext to SSML and add a  2 second pause between each address
    // SSML Reference: https://cloud.google.com/text-to-speech/docs/ssml
    String expandedNewline = escapedLines.replaceAll("\\n", "\n<break time='2s'/>");
    String ssmlText = "<speak>" + expandedNewline + "</speak>";

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
      // Set the ssml text input to synthesize
      SynthesisInput input = SynthesisInput.newBuilder().setSsml(ssmlText).build();

      // Build the voice request, select the language code ("en-US") and the ssml voice gender
      // ("male")
      VoiceSelectionParams voice =
          VoiceSelectionParams.newBuilder()
              .setLanguageCode("en-US")
              .setSsmlGender(SsmlVoiceGender.MALE)
              .build();

      // Select the audio file type
      AudioConfig audioConfig =
          AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

      // Perform the text-to-speech request on the text input with the selected voice parameters and
      // audio file type
      SynthesizeSpeechResponse response =
          textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

      // Get the audio contents from the response
      ByteString audioContents = response.getAudioContent();

      // Write the response to the output file
      FileOutputStream out = new FileOutputStream(outFile);
      out.write(audioContents.toByteArray());
      System.out.println("Audio content written to file " + outFile);
    } catch (IOException e) {
      throw new RuntimeException("Error converting the text to audio.", e);
    }
  }
  // [END translate_hybrid_tts]

  // [START translate_hybrid_integration]
  public static void main(String... args) {
    // [START translate_hybrid_project_id]
    String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
    // [END translate_hybrid_project_id]
    String glossaryDisplayName = "bistro-glossary";

    createGlossary(projectId, glossaryDisplayName);
    String text = picToText("resources/example.png");
    String translatedText = translateText(projectId, text, glossaryDisplayName);
    textToAudio(translatedText, "resources/example.mp3");
  }
  // [END translate_hybrid_integration]
}
