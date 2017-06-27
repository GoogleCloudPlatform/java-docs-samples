/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.language.samples;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.language.v1beta2.Entity;
import com.google.cloud.language.v1beta2.EntityMention;
import com.google.cloud.language.v1beta2.LanguageServiceClient;
import com.google.cloud.language.v1beta2.Sentiment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Integration (system) tests for {@link Analyze}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class AnalyzeBetaIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String BUCKET = PROJECT_ID;

  private AnalyzeBeta analyzeApp;

  @Before public void setup() throws Exception {
    analyzeApp = new AnalyzeBeta(LanguageServiceClient.create());
  }

  @Test public void analyzeSentiment_returnPositiveGerman() throws Exception {
    // Act
    Sentiment sentiment =
        analyzeApp.analyzeSentimentText(
            "Ich hatte die schönste Erfahrung mit euch allen.", "DE");

    // Assert
    assertThat((double)sentiment.getMagnitude()).isGreaterThan(0.0);
    assertThat((double)sentiment.getScore()).isGreaterThan(0.0);
  }

  @Test public void analyzeSyntax_entitySentimentText() throws Exception {
    List<Entity> entities = analyzeApp.entitySentimentText("Oranges, grapes, and apples can be "
        + "found in the cafeterias located in Mountain View, Seattle, and London.");

    List<String> got = entities.stream().map(e -> e.getName()).collect(Collectors.toList());

    // Assert
    assertThat(got).named("entity names").contains("Seattle");
  }

  @Test public void analyzeSyntax_entitySentimentTextEncoded() throws Exception {
    List<Entity> entities = analyzeApp.entitySentimentText("foo→bar");

    List<EntityMention> mentions = entities.listIterator().next().getMentionsList();

    // Assert
    assertThat(mentions.get(0).getText().getBeginOffset()).isEqualTo(4);
  }

  @Test public void analyzeSyntax_entitySentimentFile() throws Exception {
    List<Entity> entities =
        analyzeApp.entitySentimentFile("gs://" + BUCKET + "/natural-language/gettysburg.txt");

    List<String> got = entities.stream().map(e -> e.getName()).collect(Collectors.toList());

    // Assert
    assertThat(got).named("entity names").contains("God");
  }
}
