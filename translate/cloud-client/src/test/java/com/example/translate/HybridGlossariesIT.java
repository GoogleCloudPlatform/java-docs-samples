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

package com.google.cloud.translate;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.v3beta1.BatchTranslateResponse;
import com.google.cloud.translate.v3beta1.BatchTranslateTextRequest;
import com.google.cloud.translate.v3beta1.CreateGlossaryRequest;
import com.google.cloud.translate.v3beta1.DeleteGlossaryResponse;
import com.google.cloud.translate.v3beta1.DetectLanguageRequest;
import com.google.cloud.translate.v3beta1.DetectLanguageResponse;
import com.google.cloud.translate.v3beta1.GcsDestination;
import com.google.cloud.translate.v3beta1.GcsSource;
import com.google.cloud.translate.v3beta1.GetSupportedLanguagesRequest;
import com.google.cloud.translate.v3beta1.Glossary;
import com.google.cloud.translate.v3beta1.Glossary.LanguageCodesSet;
import com.google.cloud.translate.v3beta1.GlossaryInputConfig;
import com.google.cloud.translate.v3beta1.GlossaryName;
import com.google.cloud.translate.v3beta1.InputConfig;
import com.google.cloud.translate.v3beta1.LocationName;
import com.google.cloud.translate.v3beta1.OutputConfig;
import com.google.cloud.translate.v3beta1.SupportedLanguage;
import com.google.cloud.translate.v3beta1.SupportedLanguages;
import com.google.cloud.translate.v3beta1.TranslateTextGlossaryConfig;
import com.google.cloud.translate.v3beta1.TranslateTextRequest;
import com.google.cloud.translate.v3beta1.TranslateTextResponse;
import com.google.cloud.translate.v3beta1.TranslationServiceClient;
import com.google.cloud.translate.v3beta1.TranslationServiceClient.ListGlossariesPagedResponse;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.TextAnnotation;

import com.google.common.html.HtmlEscapers;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;



/**
 * Tests for SsmlAddresses sample.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class HybridGlossariesIT {

  private static String PROJECT_ID = System.getenv("PROJECT_ID");
  private static String OUTPUT_FILE = "output.mp3";
  private static String INPUT_FILE = "resources/test.jpeg";
  private static String TEXT_FILE = "resources/test.txt";
  private static String SRC_LANG = "fr";
  private static String TGT_LANG = "en";
  private static String GLOSS_NAME = "bistro-glossary";
  private static String GLOSS_URI =
      "gs://cloud-samples-data/translation/bistro_glossary.csv";

  private ByteArrayOutputStream bout;
  private PrintStream out;
  private File outputFile;

  @Before
  public void setUp() {
    // collect project ID from environment
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @Test
  public void testPicToText() throws Exception {
    // Act
    String ocr = HybridGlossaries.picToText(INPUT_FILE);

    // Assert
    assertThat(ocr).contains("test");
  }

  @Test
  public void testCreateGlossary() throws Exception {
    // Act
    HybridGlossaries.createGlossary(SRC_LANG, TGT_LANG, PROJECT_ID, GLOSS_NAME, GLOSS_URI);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("glossary");
  }

  @Test
  public void testTranslateText() throws Exception {
    // Act
    HybridGlossaries.createGlossary(SRC_LANG, TGT_LANG, PROJECT_ID, GLOSS_NAME, GLOSS_URI);

    String inputText = "chevre";
    String translation =
        HybridGlossaries.translateText(inputText, SRC_LANG, TGT_LANG, PROJECT_ID, GLOSS_NAME);

    // Assert
    assertThat(translation).contains("goat cheese");
  }

  @Test
  public void testTextToAudio() throws Exception {
    // Act
    String text = new String(Files.readAllBytes(Paths.get(TEXT_FILE)));
    HybridGlossaries.textToAudio(text, OUTPUT_FILE);

    // Assert
    outputFile = new File(OUTPUT_FILE);
    assertThat(outputFile.isFile()).isTrue();
    String got = bout.toString();
    assertThat(got).contains("Audio content written to file " + OUTPUT_FILE);

    // After
    outputFile.delete();
  }
}