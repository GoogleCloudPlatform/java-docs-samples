/*
 * Copyright 2022 Google LLC
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

package functions;

// [START functions_ocr_translate_pojo]

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

// Object for storing OCR translation requests
public class OcrTranslateApiMessage {
  private static final Gson gson = new Gson();

  private String text;
  private String filename;
  private String lang;

  public OcrTranslateApiMessage(String text, String filename, String lang) {
    if (text == null) {
      throw new IllegalArgumentException("Missing text parameter");
    }
    if (filename == null) {
      throw new IllegalArgumentException("Missing filename parameter");
    }
    if (lang == null) {
      throw new IllegalArgumentException("Missing lang parameter");
    }

    this.text = text;
    this.filename = filename;
    this.lang = lang;
  }

  public String getText() {
    return text;
  }

  public String getFilename() {
    return filename;
  }

  public String getLang() {
    return lang;
  }

  public static OcrTranslateApiMessage fromPubsubData(byte[] data) {
    String jsonStr = new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
    Map<String, String> jsonMap = gson.fromJson(jsonStr, Map.class);

    return new OcrTranslateApiMessage(
        jsonMap.get("text"), jsonMap.get("filename"), jsonMap.get("lang"));
  }

  public byte[] toPubsubData() {
    return gson.toJson(this).getBytes(StandardCharsets.UTF_8);
  }
}
// [END functions_ocr_translate_pojo]
