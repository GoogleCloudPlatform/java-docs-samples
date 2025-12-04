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

// [START googlegenaisdk_ctrlgen_with_nullable_schema]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;

import java.util.List;
import java.util.Map;

public class ControlledGenerationWithNullableSchema {

    public static void main(String[] args) {
        String modelId = "gemini-2.5-flash";

        String prompt = "The week ahead brings a mix of weather conditions.\n"
                + "Sunday is expected to be sunny with a temperature of 77°F and a humidity level of 50%. "
                + "Winds will be light at around 10 km/h.\n"
                + "Monday will see partly cloudy skies with a slightly cooler temperature of 72°F and the winds "
                + "will pick up slightly to around 15 km/h.\n"
                + "Tuesday brings rain showers, with temperatures dropping to 64°F and humidity rising to 70%.\n"
                + "Wednesday may see thunderstorms, with a temperature of 68°F.\n"
                + "Thursday will be cloudy with a temperature of 66°F and moderate humidity at 60%.\n"
                + "Friday returns to partly cloudy conditions, with a temperature of 73°F and the Winds will be "
                + "light at 12 km/h.\n"
                + "Finally, Saturday rounds off the week with sunny skies, a temperature of 80°F, and a humidity "
                + "level of 40%. Winds will be gentle at 8 km/h.\n";

        generateContent(modelId, prompt);
    }

    public static String generateContent(String modelId, String contents) {

        try (Client client =
                     Client.builder()
                             .location("global")
                             .vertexAI(true)
                             .httpOptions(HttpOptions.builder().apiVersion("v1").build())
                             .build()) {

            // Define schema for array items (each weather entry object)
            Schema dayForecastSchema =
                    Schema.builder()
                            .type(Type.Known.OBJECT)
                            .properties(
                                    Map.of(
                                            "Day", Schema.builder().type(Type.Known.STRING).nullable(true).build(),
                                            "Forecast", Schema.builder().type(Type.Known.STRING).nullable(true).build(),
                                            "Temperature", Schema.builder().type(Type.Known.INTEGER).nullable(true).build(),
                                            "Humidity", Schema.builder().type(Type.Known.STRING).nullable(true).build(),
                                            "Wind Speed", Schema.builder().type(Type.Known.INTEGER).nullable(true).build()))
                            .required(List.of("Day", "Temperature", "Forecast", "Wind Speed"))
                            .build();

            // Full response schema
            Schema responseSchema =
                    Schema.builder()
                            .type(Type.Known.OBJECT)
                            .properties(
                                    Map.of(
                                            "forecast",
                                            Schema.builder()
                                                    .type(Type.Known.ARRAY)
                                                    .items(dayForecastSchema)
                                                    .build()))
                            .build();

            GenerateContentConfig config =
                    GenerateContentConfig.builder()
                            .responseMimeType("application/json")
                            .responseSchema(responseSchema)
                            .build();

            GenerateContentResponse response = client.models.generateContent(modelId, contents, config);

            System.out.println(response.text());
            // Example response:
            // {"forecast": [{"Day": "Sunday", "Forecast": "sunny", "Temperature": 77, "Wind Speed": 10, "Humidity": "50%"},
            //  {"Day": "Monday", "Forecast": "partly cloudy", "Temperature": 72, "Wind Speed": 15},
            //  {"Day": "Tuesday", "Forecast": "rain showers", "Temperature": 64, "Wind Speed": null, "Humidity": "70%"},
            //  {"Day": "Wednesday", "Forecast": "thunderstorms", "Temperature": 68, "Wind Speed": null},
            //  {"Day": "Thursday", "Forecast": "cloudy", "Temperature": 66, "Wind Speed": null, "Humidity": "60%"},
            //  {"Day": "Friday", "Forecast": "partly cloudy", "Temperature": 73, "Wind Speed": 12},
            //  {"Day": "Saturday", "Forecast": "sunny", "Temperature": 80, "Wind Speed": 8, "Humidity": "40%"}]}
            return response.text();
        }
    }
}
// [END googlegenaisdk_ctrlgen_with_nullable_schema]