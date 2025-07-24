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

// [START googlegenaisdk_ctrlgen_with_enum_schema]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import java.util.List;

public class ControlledGenerationWithEnumSchema {

    public static void main(String[] args) {
        // TODO(developer): Replace these variables before running the sample.
        String contents = "What type of instrument is an oboe?";
        String modelId = "gemini-2.5-flash";
        generateContent(modelId, contents);
    }

    // Generates content with an enum response schema
    public static String generateContent(String modelId, String contents) {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests.
        try (Client client =
                     Client.builder().httpOptions(HttpOptions.builder().apiVersion("v1").build()).build()) {

            // Define the response schema with an enum.
            Schema responseSchema =
                    Schema.builder()
                            .type(Type.Known.STRING)
                            .enum_(List.of("Percussion", "String", "Woodwind", "Brass", "Keyboard"))
                            .build();

            GenerateContentConfig config =
                    GenerateContentConfig.builder()
                            .responseMimeType("text/x.enum")
                            .responseSchema(responseSchema)
                            .build();

            GenerateContentResponse response = client.models.generateContent(modelId, contents, config);

            System.out.print(response.text());
            // Example response:
            // Woodwind
            return response.text();
        }
    }
}
// [END googlegenaisdk_ctrlgen_with_enum_schema]
