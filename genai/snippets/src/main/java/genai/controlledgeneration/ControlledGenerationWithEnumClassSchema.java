/*
 * Copyright 2025 Google LLC
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

package genai.controlledgeneration;

// [START googlegenaisdk_ctrlgen_with_enum_class_schema]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import java.util.List;

public class ControlledGenerationWithEnumClassSchema {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.5-flash";
    String prompt = "What type of instrument is a guitar?";
    generateContent(modelId, prompt);
  }

  // Generates content with an enum class response schema
  public static String generateContent(String modelId, String contents) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client =
        Client.builder()
            .location("global")
            .vertexAI(true)
            .httpOptions(HttpOptions.builder().apiVersion("v1").build())
            .build()) {

      // Build schema using enum values
      Schema responseSchema =
          Schema.builder()
              .type(Type.Known.STRING)
              .enum_(
                  List.of(
                      InstrumentClass.PERCUSSION.getValue(),
                      InstrumentClass.STRING.getValue(),
                      InstrumentClass.WOODWIND.getValue(),
                      InstrumentClass.BRASS.getValue(),
                      InstrumentClass.KEYBOARD.getValue()))
              .build();

      GenerateContentConfig config =
          GenerateContentConfig.builder()
              .responseMimeType("text/x.enum")
              .responseSchema(responseSchema)
              .build();

      GenerateContentResponse response = client.models.generateContent(modelId, contents, config);

      System.out.println(response.text());
      // Example response:
      // String
      return response.text();
    }
  }

  // Enum mirroring the Python sample
  public enum InstrumentClass {
    PERCUSSION("Percussion"),
    STRING("String"),
    WOODWIND("Woodwind"),
    BRASS("Brass"),
    KEYBOARD("Keyboard");

    private final String value;

    InstrumentClass(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
// [END googlegenaisdk_ctrlgen_with_enum_class_schema]
