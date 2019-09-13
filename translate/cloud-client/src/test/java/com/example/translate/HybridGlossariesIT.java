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

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.translate.v3beta1.DeleteGlossaryRequest;
import com.google.cloud.translate.v3beta1.Glossary;
import com.google.cloud.translate.v3beta1.ListGlossariesRequest;
import com.google.cloud.translate.v3beta1.ListGlossariesResponse;
import com.google.cloud.translate.v3beta1.LocationName;
import com.google.cloud.translate.v3beta1.TranslationServiceClient;
import com.google.cloud.translate.v3beta1.TranslationServiceClient.ListGlossariesPagedResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

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

  @After
  public void tearDown() {
    try (TranslationServiceClient translationServiceClient = TranslationServiceClient.create()) {
      LocationName locationName =
              LocationName.newBuilder().setProject(PROJECT_ID).setLocation("us-central1").build();

      ListGlossariesRequest listGlossariesRequest = ListGlossariesRequest.newBuilder()
              .setParent(locationName.toString())
              .build();

      ListGlossariesPagedResponse listGlossariesPagedResponse =
              translationServiceClient.listGlossaries(listGlossariesRequest);

      ListGlossariesResponse listGlossariesResponse =
              listGlossariesPagedResponse.getPage().getResponse();

      for (Glossary glossary : listGlossariesResponse.getGlossariesList()) {
        DeleteGlossaryRequest request =
                DeleteGlossaryRequest.newBuilder().setName(glossary.getName()).build();

        translationServiceClient.deleteGlossaryAsync(request).get(300, TimeUnit.SECONDS);
      }
    } catch (Exception e) {
      System.out.format("Failed to delete the glossary.\n%s\n", e.getMessage());
    }

    System.setOut(null);
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
    assertThat(got).contains("bistro-glossary");
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