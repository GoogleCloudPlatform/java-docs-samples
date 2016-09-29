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

import java.util.List;
import com.google.common.collect.ImmutableList;

import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.testing.RemoteTranslateHelper;

public class TranslateText {
  private static final Translate TRANSLATE = RemoteTranslateHelper.create().options().service();

  /**
   * Detect the language of input text
   */
  public static void detectLanguage(String sourceText) {
    List<Detection> detections = TRANSLATE.detect(ImmutableList.of(sourceText));
    
    System.out.println("Language(s) detected:");
    for(Detection detection : detections) {
      System.out.println("\t"+detection);
    }
  }

  /**
   * Translates the source text in any language to english
   */
  public static void translateText(String sourceText) {
    Translation translation = TRANSLATE.translate(sourceText);
    System.out.println("Source Text:\n\t"+sourceText);
    System.out.println("Translated Text:\n\t"+translation.translatedText());
  }

  public static void main(String[] args) {
    String command = args[0];
    String text = args[1];

    if(command.equals("detect")) {
      TranslateText.detectLanguage(text);
    }
    else if(command.equals("translate")) {
      TranslateText.translateText(text);
    }
  }
}
