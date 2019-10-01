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
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for HybridGlossaries sample.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class HybridGlossariesIT {

  private static String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String OUTPUT_FILE = "output.mp3";
  private static String INPUT_FILE = "resources/test.jpeg";
  private static String TEXT_FILE = "resources/test.txt";
  private static String GLOSSARY_DISPLAY_NAME;

  private ByteArrayOutputStream bout;
  private PrintStream out;
  private File outputFile;

  @Before
  public void setUp() {
    GLOSSARY_DISPLAY_NAME = String.format("bistro-glossary-%s", UUID.randomUUID());
    
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    try (TranslationServiceClient translationServiceClient = TranslationServiceClient.create()) {
      LocationName locationName =
              LocationName.newBuilder().setProject(PROJECT_ID).setLocation("us-central1").build();

      ListGlossariesRequest listGlossariesRequest =
              ListGlossariesRequest.newBuilder().setParent(locationName.toString()).build();

      ListGlossariesPagedResponse listGlossariesPagedResponse =
              translationServiceClient.listGlossaries(listGlossariesRequest);

      ListGlossariesResponse listGlossariesResponse =
              listGlossariesPagedResponse.getPage().getResponse();

      for (Glossary glossary : listGlossariesResponse.getGlossariesList()) {
        DeleteGlossaryRequest request =
                DeleteGlossaryRequest.newBuilder().setName(glossary.getName()).build();

        translationServiceClient.deleteGlossaryAsync(request).get(300, TimeUnit.SECONDS);
      }
    }

    System.setOut(null);
  }

  @Test
  public void testPicToText() {
    // Act
    String ocr = HybridGlossaries.picToText(INPUT_FILE);

    // Assert
    assertThat(ocr).contains("test");
  }

  @Test
  public void testCreateGlossary() {
    // Act
    HybridGlossaries.createGlossary(PROJECT_ID, GLOSSARY_DISPLAY_NAME);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("bistro-glossary");
  }

  @Test
  public void testTranslateText() {
    // Act
    HybridGlossaries.createGlossary(PROJECT_ID, GLOSSARY_DISPLAY_NAME);

    String inputText = "chevre";
    String translation =
        HybridGlossaries.translateText(PROJECT_ID, inputText, GLOSSARY_DISPLAY_NAME);

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
