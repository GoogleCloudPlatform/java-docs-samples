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
public class ControlledGenerationWithEnumClassSchemaIT {

    private static final String GEMINI_FLASH = "gemini-2.5-flash";
    private ByteArrayOutputStream bout;
    private PrintStream out;

    public static void requireEnvVar(String envVarName) {
        assertWithMessage(String.format("Missing environment variable '%s'", envVarName))
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
    public void testControlledGenerationWithEnumClassSchema() {
        String prompt = "What type of instrument is a guitar?";
        String response =
                ControlledGenerationWithEnumClassSchema.generateContent(GEMINI_FLASH, prompt);

        assertThat(response).isNotEmpty();
        // The response *should* contain one of the expected enum values
        assertThat(response)
                .isEqualTo("String");
    }
}