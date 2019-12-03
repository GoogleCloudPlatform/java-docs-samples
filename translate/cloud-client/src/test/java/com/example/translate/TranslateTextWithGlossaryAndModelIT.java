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
import static junit.framework.TestCase.assertNotNull;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.translate.v3.CreateGlossaryMetadata;
import com.google.cloud.translate.v3.CreateGlossaryRequest;
import com.google.cloud.translate.v3.DeleteGlossaryMetadata;
import com.google.cloud.translate.v3.DeleteGlossaryRequest;
import com.google.cloud.translate.v3.DeleteGlossaryResponse;
import com.google.cloud.translate.v3.GcsSource;
import com.google.cloud.translate.v3.Glossary;
import com.google.cloud.translate.v3.GlossaryInputConfig;
import com.google.cloud.translate.v3.GlossaryName;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslationServiceClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for Batch Translate Text With Glossary and Model sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class TranslateTextWithGlossaryAndModelIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION = "us-central1";
  private static final String MODEL_ID = "TRL2188848820815848149";
  private static final String GLOSSARY_INPUT_URI =
      "gs://cloud-samples-data/translation/glossary_ja.csv";
  private static final String GLOSSARY_ID =
      String.format("test_%s", UUID.randomUUID().toString().replace("-", "_").substring(0, 26));

  private ByteArrayOutputStream bout;
  private PrintStream out;

  private static void requireEnvVar(String varName) {
    assertNotNull(
            System.getenv(varName),
            "Environment variable '%s' is required to perform these tests.".format(varName)
    );
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void setUp() throws InterruptedException, ExecutionException, IOException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    try (TranslationServiceClient client = TranslationServiceClient.create()) {
      LocationName parent = LocationName.of(PROJECT_ID, LOCATION);
      GlossaryName glossaryName = GlossaryName.of(PROJECT_ID, LOCATION, GLOSSARY_ID);
      Glossary.LanguageCodesSet languageCodesSet =
          Glossary.LanguageCodesSet.newBuilder()
              .addLanguageCodes("en")
              .addLanguageCodes("ja")
              .build();
      GcsSource gcsSource = GcsSource.newBuilder().setInputUri(GLOSSARY_INPUT_URI).build();
      GlossaryInputConfig inputConfig =
          GlossaryInputConfig.newBuilder().setGcsSource(gcsSource).build();
      Glossary glossary =
          Glossary.newBuilder()
              .setName(glossaryName.toString())
              .setLanguageCodesSet(languageCodesSet)
              .setInputConfig(inputConfig)
              .build();
      CreateGlossaryRequest request =
          CreateGlossaryRequest.newBuilder()
              .setParent(parent.toString())
              .setGlossary(glossary)
              .build();

      OperationFuture<Glossary, CreateGlossaryMetadata> future =
          client.createGlossaryAsync(request);
      Glossary response = future.get();
    }
    System.setOut(out);
  }

  @After
  public void tearDown() throws InterruptedException, ExecutionException, IOException {
    // Clean up
    try (TranslationServiceClient client = TranslationServiceClient.create()) {
      GlossaryName glossaryName = GlossaryName.of(PROJECT_ID, LOCATION, GLOSSARY_ID);
      DeleteGlossaryRequest request =
          DeleteGlossaryRequest.newBuilder().setName(glossaryName.toString()).build();
      OperationFuture<DeleteGlossaryResponse, DeleteGlossaryMetadata> future =
          client.deleteGlossaryAsync(request);
      DeleteGlossaryResponse response = future.get();
    }
    System.setOut(null);
  }

  @Test
  public void testTranslateTextWithGlossaryAndModel() throws IOException {
    // Act
    TranslateTextWithGlossaryAndModel.translateTextWithGlossaryAndModel(
        PROJECT_ID, LOCATION, "en", "ja", "That' il do it. deception", GLOSSARY_ID, MODEL_ID);

    // Assert
    String got = bout.toString();

    assertThat(got).contains("それはそうだ"); // custom model
    assertThat(got).contains("欺く"); // glossary
  }
}
