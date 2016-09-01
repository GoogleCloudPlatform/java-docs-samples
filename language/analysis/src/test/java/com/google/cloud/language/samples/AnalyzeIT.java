/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

import com.google.api.services.language.v1beta1.model.Entity;
import com.google.api.services.language.v1beta1.model.Sentiment;
import com.google.api.services.language.v1beta1.model.Token;

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
public class AnalyzeIT {

  private Analyze analyzeApp;

  @Before public void setup() throws Exception {
    analyzeApp = new Analyze(Analyze.getLanguageService());
  }

  @Test public void analyzeEntities_withEntities_returnsLarryPage() throws Exception {
    // Act
    List<Entity> entities =
        analyzeApp.analyzeEntities(
            "Larry Page, Google's co-founder, once described the 'perfect search engine' as"
            + " something that 'understands exactly what you mean and gives you back exactly what"
            + " you want.' Since he spoke those words Google has grown to offer products beyond"
            + " search, but the spirit of what he said remains.");
    List<String> got = entities.stream().map(e -> e.getName()).collect(Collectors.toList());

    // Assert
    assertThat(got).named("entity names").contains("Larry Page");
  }

  @Test public void analyzeSentiment_returnPositive() throws Exception {
    // Act
    Sentiment sentiment = 
        analyzeApp.analyzeSentiment(
            "Tom Cruise is one of the finest actors in hollywood and a great star!");
    
    // Assert
    assertThat((double)sentiment.getMagnitude()).isGreaterThan(0.0);
    assertThat((double)sentiment.getPolarity()).isGreaterThan(0.0);
  }

  @Test public void analyzeSentiment_returnNegative() throws Exception {
    // Act
    Sentiment sentiment =
        analyzeApp.analyzeSentiment(
            "John was seriously injured in an accident");

    // Assert
    assertThat((double)sentiment.getMagnitude()).isGreaterThan(0.0);
    assertThat((double)sentiment.getPolarity()).isLessThan(0.0);
  }

  @Test public void analyzeSyntax_partOfSpeech() throws Exception {
    // Act
    List<Token> token =
        analyzeApp.analyzeSyntax(
            "President Obama was elected for the second term");

    List<String> got = token.stream().map(e -> e.getPartOfSpeech().getTag())
        .collect(Collectors.toList());

    // Assert
    assertThat(got).containsExactly("NOUN", "NOUN", "VERB", 
        "VERB", "ADP", "DET", "ADJ", "NOUN").inOrder();
  }
}
