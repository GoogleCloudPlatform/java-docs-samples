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

import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.testing.RemoteTranslateHelper;
import com.google.common.collect.ImmutableList;

import java.io.PrintStream;
import java.util.List;

public class TranslateText {
  private static final Translate TRANSLATE = RemoteTranslateHelper.create().options().service();

  /**
   * Detect the language of input text.
   */
  public static void detectLanguage(String sourceText, PrintStream out) {
    List<Detection> detections = TRANSLATE.detect(ImmutableList.of(sourceText));
    System.out.println("Language(s) detected:");
    for (Detection detection : detections) {
      out.printf("\t%s\n", detection);
    }
  }

  /**
   * Translates the source text in any language to english.
   */
  public static void translateText(String sourceText, PrintStream out) {
    Translation translation = TRANSLATE.translate(sourceText);
    out.printf("Source Text:\n\t%s\n", sourceText);
    out.printf("Translated Text:\n\t%s\n", translation.translatedText());
  }

  /**
   * Translate the source text from source to target language.
   */
  public static void translateTextWithOptions(
      String sourceText,
      String sourceLang,
      String targetLang,
      PrintStream out) {

    TranslateOption srcLang = TranslateOption.sourceLanguage(sourceLang);
    TranslateOption tgtLang = TranslateOption.targetLanguage(targetLang);

    Translation translation = TRANSLATE.translate(sourceText, srcLang, tgtLang);
    out.printf("Source Text:\n\tLang: %s, Text: %s\n", sourceLang, sourceText);
    out.printf("TranslatedText:\n\tLang: %s, Text: %s\n", targetLang, translation.translatedText());
  }

  /**
   * Displays a list of supported languages (codes).
   */
  public static void displaySupportedLanguages(PrintStream out) {
    List<Language> languages = TRANSLATE.listSupportedLanguages();

    for (Language language : languages) {
      out.printf("Name: %s, Code: %s\n", language.name(), language.code());
    }
  }

  public static void main(String[] args) {
    String command = args[0];
    String text;

    if (command.equals("detect")) {
      text = args[1];
      TranslateText.detectLanguage(text, System.out);
    } else if (command.equals("translate")) {
      text = args[1];
      try {
        String sourceLang = args[2];
        String targetLang = args[3];
        TranslateText.translateTextWithOptions(text, sourceLang, targetLang, System.out);
      } catch (ArrayIndexOutOfBoundsException ex) {
        TranslateText.translateText(text, System.out);
      }
    } else if (command.equals("langsupport")) {
      TranslateText.displaySupportedLanguages(System.out);
    }
  }
}
