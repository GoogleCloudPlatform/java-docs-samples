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

import com.google.cloud.language.spi.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.PartOfSpeech.Tag;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.language.v1.Token;

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
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String BUCKET = PROJECT_ID;

  private Analyze analyzeApp;

  @Before public void setup() throws Exception {
    analyzeApp = new Analyze(LanguageServiceClient.create());
  }

  @Test public void analyzeEntities_withEntities_returnsLarryPage() throws Exception {
    // Act
    List<Entity> entities =
        analyzeApp.analyzeEntitiesText(
            "Larry Page, Google's co-founder, once described the 'perfect search engine' as"
            + " something that 'understands exactly what you mean and gives you back exactly what"
            + " you want.' Since he spoke those words Google has grown to offer products beyond"
            + " search, but the spirit of what he said remains.");
    List<String> got = entities.stream().map(e -> e.getName()).collect(Collectors.toList());

    // Assert
    assertThat(got).named("entity names").contains("Larry Page");
  }

  @Test public void analyzeEntities_withEntitiesFile_containsGod() throws Exception {
      // Act
      List<Entity> entities =
          analyzeApp.analyzeEntitiesFile("gs://" + BUCKET + "/natural-language/gettysburg.txt");
      List<String> got = entities.stream().map(e -> e.getName()).collect(Collectors.toList());

      // Assert
      assertThat(got).named("entity names").contains("God");
    }

  @Test public void analyzeSentimentText_returnPositive() throws Exception {
    // Act
    Sentiment sentiment =
        analyzeApp.analyzeSentimentText(
            "Tom Cruise is one of the finest actors in hollywood and a great star!");

    // Assert
    assertThat((double)sentiment.getMagnitude()).isGreaterThan(0.0);
    assertThat((double)sentiment.getScore()).isGreaterThan(0.0);
  }

  @Test public void analyzeSentimentFile_returnPositiveFile() throws Exception {
      // Act
      Sentiment sentiment =
          analyzeApp.analyzeSentimentFile("gs://" + BUCKET + "/natural-language/"
                  + "sentiment/bladerunner-pos.txt");

      // Assert
      assertThat((double)sentiment.getMagnitude()).isGreaterThan(0.0);
      assertThat((double)sentiment.getScore()).isGreaterThan(0.0);
    }

  @Test public void analyzeSentiment_returnNegative() throws Exception {
    // Act
    Sentiment sentiment =
        analyzeApp.analyzeSentimentText(
            "That was the worst performance I've seen in awhile.");

    // Assert
    assertThat((double)sentiment.getMagnitude()).isGreaterThan(0.0);
    assertThat((double)sentiment.getScore()).isLessThan(0.0);
  }

  @Test public void analyzeSentiment_returnNegativeFile() throws Exception {
      // Act
      Sentiment sentiment =
          analyzeApp.analyzeSentimentFile("gs://" + BUCKET + "/natural-language/"
                  + "sentiment/bladerunner-neg.txt");

      // Assert
      assertThat((double)sentiment.getMagnitude()).isGreaterThan(0.0);
      assertThat((double)sentiment.getScore()).isLessThan(0.0);
    }

  @Test public void analyzeSentiment_returnNeutralFile() throws Exception {
      // Act
      Sentiment sentiment =
          analyzeApp.analyzeSentimentFile("gs://" + BUCKET + "/natural-language/"
                  + "sentiment/bladerunner-neutral.txt");

      // Assert
      assertThat((double)sentiment.getMagnitude()).isGreaterThan(1.0);
      assertThat((double)sentiment.getScore()).isWithin(0.1);
    }

  @Test public void analyzeSyntax_partOfSpeech() throws Exception {
    // Act
    List<Token> token =
        analyzeApp.analyzeSyntaxText(
            "President Obama was elected for the second term");

    List<Tag> got = token.stream().map(e -> e.getPartOfSpeech().getTag())
        .collect(Collectors.toList());

    // Assert
    assertThat(got).containsExactly(Tag.NOUN, Tag.NOUN, Tag.VERB,
        Tag.VERB, Tag.ADP, Tag.DET, Tag.ADJ, Tag.NOUN).inOrder();
  }

  @Test public void analyzeSyntax_partOfSpeechFile() throws Exception {
      // Act
      List<Token> token =
          analyzeApp.analyzeSyntaxFile("gs://" + BUCKET + "/natural-language/"
                  + "sentiment/bladerunner-neutral.txt");

      List<Tag> got = token.stream().map(e -> e.getPartOfSpeech().getTag())
          .collect(Collectors.toList());

      // Assert
      assertThat(got).containsExactly(Tag.PRON, Tag.CONJ, Tag.VERB, Tag.CONJ, Tag.VERB,
          Tag.DET, Tag.NOUN, Tag.PUNCT, Tag.NOUN, Tag.VERB, Tag.ADJ, Tag.PUNCT, Tag.CONJ,
          Tag.ADV, Tag.PRON, Tag.VERB, Tag.VERB, Tag.VERB, Tag.ADJ, Tag.PUNCT, Tag.DET,
          Tag.NOUN, Tag.VERB, Tag.ADV, Tag.ADJ,Tag.PUNCT).inOrder();
    }
}
