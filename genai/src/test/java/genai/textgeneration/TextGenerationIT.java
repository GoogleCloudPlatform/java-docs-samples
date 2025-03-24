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

// Tests for Gen AI SDK code samples.

package genai.textgeneration;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.io.IOException;
import org.apache.http.HttpException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TextGenerationIT {

  private static final String MODEL_ID = "gemini-2.0-flash-001";

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  // Helper function to set environment variables programmatically
  public static void setEnvVariable(String key, String value) {
    try {
      Map<String, String> env = System.getenv();
      Class<?> cl = env.getClass();
      Field field = cl.getDeclaredField("m");
      field.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<String, String> writableEnv = (Map<String, String>) field.get(env);
      if (!writableEnv.containsKey(key)) {
        writableEnv.put(key, value);
      }
    } catch (Exception e) {
      throw new IllegalStateException("Failed to set environment variable", e);
    }
  }

  @BeforeClass
  public static void setUp() {
    setEnvVariable("GOOGLE_CLOUD_LOCATION", "us-central1");
    setEnvVariable("GOOGLE_GENAI_USE_VERTEXAI", "True");

    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
    requireEnvVar("GOOGLE_CLOUD_LOCATION");
    requireEnvVar("GOOGLE_GENAI_USE_VERTEXAI");
  }

  @Test
  public void testTextGeneration() throws IOException, HttpException {
    String prompt = "How does AI work?";
    String response = TextGeneration.generateContent(MODEL_ID, prompt);
    assertThat(response).isNotEmpty();
  }
}
