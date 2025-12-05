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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ControlledGenerationIT {

  private static final String GEMINI_FLASH = "gemini-2.5-flash";
  private ByteArrayOutputStream bout;
  private PrintStream out;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testControlledGenerationWithEnumSchema() {
    String prompt = "What type of instrument is an oboe?";
    String response = ControlledGenerationWithEnumSchema.generateContent(GEMINI_FLASH, prompt);
    assertThat(response).isNotEmpty();
  }

    @Test
    public void testControlledGenerationWithEnumClassSchema() {
        String prompt = "What type of instrument is a guitar?";
        String response = ControlledGenerationWithEnumClassSchema.generateContent(GEMINI_FLASH, prompt);

        assertThat(response).isNotEmpty();
        assertThat(response).isEqualTo("String");
    }

    @Test
    public void testControlledGenerationWithNullableSchema() {
        String prompt =
                "The week ahead brings a mix of weather conditions.\n"
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

        String response = ControlledGenerationWithNullableSchema.generateContent(GEMINI_FLASH, prompt);

        assertThat(response).isNotEmpty();
        assertThat(response).contains("forecast");
    }

    @Test
    public void testControlledGenerationWithResponseSchema() {
        String prompt = "List a few popular cookie recipes.";

        String response = ControlledGenerationWithResponseSchema.generateContent(GEMINI_FLASH, prompt);

        assertThat(response).isNotEmpty();
        assertThat(response).contains("recipe_name");
    }

}
