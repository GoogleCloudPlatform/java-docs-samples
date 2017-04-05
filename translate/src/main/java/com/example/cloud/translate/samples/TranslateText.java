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

package com.example.cloud.translate.samples;

import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.LanguageListOption;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.common.collect.ImmutableList;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

public class TranslateText {
  /**
   * Detect the language of input text.
   *
   * @param sourceText source text to be detected for language
   * @param out print stream
   */
  public static void detectLanguage(String sourceText, PrintStream out) {
    Translate translate = createTranslateService();
    List<Detection> detections = translate.detect(ImmutableList.of(sourceText));
    System.out.println("Language(s) detected:");
    for (Detection detection : detections) {
      out.printf("\t%s\n", detection);
    }
  }

  /**
   * Translates the source text in any language to English.
   *
   * @param sourceText source text to be translated
   * @param out print stream
   */
  public static void translateText(String sourceText, PrintStream out) {
    Translate translate = createTranslateService();
    Translation translation = translate.translate(sourceText);
    out.printf("Source Text:\n\t%s\n", sourceText);
    out.printf("Translated Text:\n\t%s\n", translation.getTranslatedText());
  }

  /**
   * Translate the source text from source to target language.
   * Make sure that your project is whitelisted.
   *
   * @param sourceText source text to be translated
   * @param sourceLang source language of the text
   * @param targetLang target language of translated text
   * @param out print stream
   */
  public static void translateTextWithOptionsAndModel(
      String sourceText,
      String sourceLang,
      String targetLang,
      PrintStream out) {

    Translate translate = createTranslateService();
    TranslateOption srcLang = TranslateOption.sourceLanguage(sourceLang);
    TranslateOption tgtLang = TranslateOption.targetLanguage(targetLang);

    // Use translate `model` parameter with `base` and `nmt` options.
    TranslateOption model = TranslateOption.model("nmt");

    Translation translation = translate.translate(sourceText, srcLang, tgtLang, model);
    out.printf("Source Text:\n\tLang: %s, Text: %s\n", sourceLang, sourceText);
    out.printf("TranslatedText:\n\tLang: %s, Text: %s\n", targetLang,
        translation.getTranslatedText());
  }


  /**
   * Translate the source text from source to target language.
   *
   * @param sourceText source text to be translated
   * @param sourceLang source language of the text
   * @param targetLang target language of translated text
   * @param out print stream
   */
  public static void translateTextWithOptions(
      String sourceText,
      String sourceLang,
      String targetLang,
      PrintStream out) {

    Translate translate = createTranslateService();
    TranslateOption srcLang = TranslateOption.sourceLanguage(sourceLang);
    TranslateOption tgtLang = TranslateOption.targetLanguage(targetLang);

    Translation translation = translate.translate(sourceText, srcLang, tgtLang);
    out.printf("Source Text:\n\tLang: %s, Text: %s\n", sourceLang, sourceText);
    out.printf("TranslatedText:\n\tLang: %s, Text: %s\n", targetLang,
        translation.getTranslatedText());
  }

  /**
   * Displays a list of supported languages and codes.
   *
   * @param out print stream
   * @param tgtLang optional target language
   */
  public static void displaySupportedLanguages(PrintStream out, Optional<String> tgtLang) {
    Translate translate = createTranslateService();
    LanguageListOption target = LanguageListOption.targetLanguage(tgtLang.orElse("en"));
    List<Language> languages = translate.listSupportedLanguages(target);

    for (Language language : languages) {
      out.printf("Name: %s, Code: %s\n", language.getName(), language.getCode());
    }
  }

  /**
   * Create Google Translate API Service.
   *
   * @return Google Translate Service
   */
  public static Translate createTranslateService() {
    return TranslateOptions.newBuilder().build().getService();
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
      try {
        String target = args[1];
        TranslateText.displaySupportedLanguages(System.out, Optional.of(target));
      } catch (ArrayIndexOutOfBoundsException ex) {
        TranslateText.displaySupportedLanguages(System.out, Optional.empty());
      }
    }
  }
}
