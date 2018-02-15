/*
 * Copyright 2018 Google Inc.
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

package com.example.appengine.translatepubsub;

import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.common.base.Strings;

public class Translate {
  /**
   * Translate the source text from source to target language.
   *
   * @param sourceText source text to be translated
   * @param sourceLang source language of the text
   * @param targetLang target language of translated text
   * @return source text translated into target language.
   */
  public static String translateText(
      String sourceText,
      String sourceLang,
      String targetLang) {
    if (Strings.isNullOrEmpty(sourceLang)
        || Strings.isNullOrEmpty(targetLang)
        || sourceLang.equals(targetLang)) {
      return sourceText;
    }
    com.google.cloud.translate.Translate translate = createTranslateService();
    TranslateOption srcLang = TranslateOption.sourceLanguage(sourceLang);
    TranslateOption tgtLang = TranslateOption.targetLanguage(targetLang);

    Translation translation = translate.translate(sourceText, srcLang, tgtLang);
    return translation.getTranslatedText();
  }

  /**
   * Create Google Translate API Service.
   *
   * @return Google Translate Service
   */
  public static com.google.cloud.translate.Translate createTranslateService() {
    return TranslateOptions.newBuilder().build().getService();
  }
}
