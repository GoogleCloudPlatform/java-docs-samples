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

package com.google.cloud.translate.samples;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link Analyze}.
 */
@RunWith(JUnit4.class)
public class TranslateTextTest {

  @Test public void testSupportedLanguages() throws Exception {
    // Supported languages
    List<String> languages = Arrays.asList(
        "Afrikaans", "Albanian", "Amharic", "Arabic", "Armenian", "Azerbaijani", "Basque",
        "Belarusian", "Bengali", "Bosnian", "Bulgarian", "Catalan", "Cebuano", "Chichewa",
        "Chinese", "Chinese", "Corsican", "Croatian", "Czech", "Danish", "Dutch", "English",
        "Esperanto", "Estonian", "Filipino", "Finnish", "French", "Frisian", "Galician",
        "Georgian", "German", "Greek", "Gujarati", "Haitian", "Hausa", "Hawaiian", "Hebrew",
        "Hindi", "Hmong", "Hungarian", "Icelandic", "Igbo", "Indonesian", "Irish", "Italian",
        "Japanese", "Javanese", "Kannada", "Kazakh", "Khmer", "Korean", "Kurdish", "Kyrgyz",
        "Lao", "Latin", "Latvian", "Lithuanian", "Luxembourgish", "Macedonian", "Malagasy",
        "Malay", "Malayalam", "Maltese", "Maori", "Marathi", "Mongolian", "Myanmar", "Nepali",
        "Norwegian", "Pashto", "Persian", "Polish", "Portuguese", "Punjabi", "Romanian",
        "Russian", "Samoan", "Scots", "Serbian", "Sesotho", "Shona", "Sindhi", "Sinhala",
        "Slovak", "Slovenian", "Somali", "Spanish", "Sundanese", "Swahili", "Swedish",
        "Tajik", "Tamil", "Telugu", "Thai", "Turkish", "Ukrainian", "Urdu", "Uzbek",
        "Vietnamese", "Welsh", "Xhosa", "Yiddish", "Yoruba", "Zulu");

    // Arrange
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);

    // Act
    TranslateText.displaySupportedLanguages(out);

    // Assert
    String got = bout.toString();
    for (String language : languages) {
      assertThat(got).contains(language);
    }
  }

  @Test public void testEnglishLangDetection() throws Exception {
    // Arrange
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);

    // Act
    TranslateText.detectLanguage("With power comes great responsibility.", out);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("language=en, confidence=0.79742646");
  }

  @Test public void testGermanLangDetection() throws Exception {
    // Arrange
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);

    // Act
    TranslateText.detectLanguage("Mit Macht kommt große Verantwortung.", out);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("language=de, confidence=0.9293963");

  }

  @Test public void testDefaultIdentityTranslation() throws Exception {
    // Arrange
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);

    // Act
    String proverb = "What you do not wish for yourself, do not do to others.";
    TranslateText.translateText(proverb, out);

    // Assert
    String got = bout.toString();
    assertThat(got).contains(proverb);
  }

  @Test public void testGermanToSpanishTranslation() throws Exception {
    // Arrange
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);

    // Act
    TranslateText.translateTextWithOptions("Mit Macht kommt große Verantwortung.", "de", "es", out);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Con el poder viene una gran responsabilidad.");
  }
}
