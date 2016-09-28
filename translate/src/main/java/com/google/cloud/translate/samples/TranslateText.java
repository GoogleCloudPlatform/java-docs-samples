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

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.testing.RemoteTranslateHelper;

public class TranslateText {
  private static final Translate TRANSLATE = RemoteTranslateHelper.create().options().service();

  public void detectLanguages() {
  }

  public void translateText(String sourceText) {
    Translation translation = TRANSLATE.translate(sourceText);
    System.out.println(translation.translatedText());
  }
  public static void main(String[] args) {
    new TranslateText().translateText("Hola"); 
  }
}
