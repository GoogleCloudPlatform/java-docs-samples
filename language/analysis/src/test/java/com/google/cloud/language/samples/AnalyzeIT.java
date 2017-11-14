/*
 * Copyright 2016 Google Inc.
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

import com.google.cloud.language.v1.PartOfSpeech.Tag;
import com.google.cloud.language.v1.Sentiment;
import com.google.cloud.language.v1.Token;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Integration (system) tests for {@link Analyze}.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class AnalyzeIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String BUCKET = PROJECT_ID;

  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @Test
  public void analyzeCategoriesInTextReturnsExpectedResult() throws Exception {
    Analyze.classifyText("Android is a mobile operating system developed by Google, "
        + "based on the Linux kernel and designed primarily for touchscreen "
        + "mobile devices such as smartphones and tablets.");
    String got = bout.toString();
    assertThat(got).contains("Computers & Electronics");
  }

  @Test
  public void analyzeCategoriesInFileReturnsExpectedResult() throws Exception {
    String gcsFile = "gs://" + PROJECT_ID + "/natural-language/android_text.txt";
    Analyze.classifyFile(gcsFile);
    String got = bout.toString();
    assertThat(got).contains("Computers & Electronics");
  }

  @Test
  public void analyzeEntities_withEntities_returnsLarryPage() throws Exception {
    Analyze.analyzeEntitiesText(
        "Larry Page, Google's co-founder, once described the 'perfect search engine' as"
            + " something that 'understands exactly what you mean and gives you back exactly what"
            + " you want.' Since he spoke those words Google has grown to offer products beyond"
            + " search, but the spirit of what he said remains.");
    String got = bout.toString();
    assertThat(got).contains("Larry Page");
  }

  @Test
  public void analyzeEntities_withEntitiesFile_containsGod() throws Exception {
    Analyze.analyzeEntitiesFile("gs://" + BUCKET + "/natural-language/gettysburg.txt");
    String got = bout.toString();
    assertThat(got).contains("God");
  }

  @Test
  public void analyzeSentimentText_returnPositive() throws Exception {
    Sentiment sentiment = Analyze.analyzeSentimentText(
        "Tom Cruise is one of the finest actors in hollywood and a great star!");
    assertThat(sentiment.getMagnitude()).isGreaterThan(0.0F);
    assertThat(sentiment.getScore()).isGreaterThan(0.0F);
  }

  @Test
  public void analyzeSentimentFile_returnPositiveFile() throws Exception {
    Sentiment sentiment = Analyze.analyzeSentimentFile("gs://" + BUCKET + "/natural-language/"
        + "sentiment/bladerunner-pos.txt");
    assertThat(sentiment.getMagnitude()).isGreaterThan(0.0F);
    assertThat(sentiment.getScore()).isGreaterThan(0.0F);
  }

  @Test
  public void analyzeSentimentText_returnNegative() throws Exception {
    Sentiment sentiment = Analyze.analyzeSentimentText(
        "That was the worst performance I've seen in a while.");
    assertThat(sentiment.getMagnitude()).isGreaterThan(0.0F);
    assertThat(sentiment.getScore()).isLessThan(0.0F);
  }

  @Test
  public void analyzeSentiment_returnNegative() throws Exception {
    Sentiment sentiment = Analyze.analyzeSentimentFile("gs://" + BUCKET + "/natural-language/"
        + "sentiment/bladerunner-neg.txt");
    assertThat(sentiment.getMagnitude()).isGreaterThan(0.0F);
    assertThat(sentiment.getScore()).isLessThan(0.0F);
  }

  @Test
  public void analyzeSentiment_returnNeutralFile() throws Exception {
    Sentiment sentiment = Analyze.analyzeSentimentFile("gs://" + BUCKET + "/natural-language/"
        + "sentiment/bladerunner-neutral.txt");
    assertThat(sentiment.getMagnitude()).isGreaterThan(1.0F);
    // TODO sentiment score for netural sample appears to be zero now.
    // assertThat((double)sentiment.getScore()).isGreaterThan(0.0);
  }

  @Test
  public void analyzeSyntax_partOfSpeech() throws Exception {
    List<Token> tokens = Analyze
        .analyzeSyntaxText("President Obama was elected for the second term");

    List<Tag> got = tokens.stream().map(e -> e.getPartOfSpeech().getTag())
        .collect(Collectors.toList());

    assertThat(got).containsExactly(Tag.NOUN, Tag.NOUN, Tag.VERB,
        Tag.VERB, Tag.ADP, Tag.DET, Tag.ADJ, Tag.NOUN).inOrder();
  }

  @Test
  public void analyzeSyntax_partOfSpeechFile() throws Exception {
    List<Token> token = Analyze.analyzeSyntaxFile("gs://" + BUCKET + "/natural-language/"
        + "sentiment/bladerunner-neutral.txt");

    List<Tag> got = token.stream().map(e -> e.getPartOfSpeech().getTag())
        .collect(Collectors.toList());

    assertThat(got).containsExactly(Tag.PRON, Tag.CONJ, Tag.VERB, Tag.CONJ, Tag.VERB,
        Tag.DET, Tag.NOUN, Tag.PUNCT, Tag.NOUN, Tag.VERB, Tag.ADJ, Tag.PUNCT, Tag.CONJ,
        Tag.ADV, Tag.PRON, Tag.VERB, Tag.VERB, Tag.VERB, Tag.ADJ, Tag.PUNCT, Tag.DET,
        Tag.NOUN, Tag.VERB, Tag.ADV, Tag.ADJ, Tag.PUNCT).inOrder();
  }

  @Test
  public void analyzeEntitySentimentTextReturnsExpectedResult() throws Exception {
    Analyze.entitySentimentText("Oranges, grapes, and apples can be "
        + "found in the cafeterias located in Mountain View, Seattle, and London.");
    String got = bout.toString();
    assertThat(got).contains("Seattle");
  }

  @Test
  public void analyzeEntitySentimentTextEncodedReturnsExpectedResult() throws Exception {
    Analyze.entitySentimentText("foo→bar");
    String got = bout.toString();
    assertThat(got).contains("offset: 4");
  }

  @Test
  public void analyzeEntitySentimenFileReturnsExpectedResult() throws Exception {
    Analyze.entitySentimentFile("gs://" + BUCKET + "/natural-language/gettysburg.txt");
    String got = bout.toString();
    assertThat(got).contains("God");
  }

}
