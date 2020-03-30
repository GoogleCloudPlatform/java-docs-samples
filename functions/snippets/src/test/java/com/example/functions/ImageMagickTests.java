/*
 * Copyright 2020 Google LLC
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

package com.example.functions;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.TestLogHandler;
import com.google.gson.Gson;
import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class ImageMagickTests {
  private static String BUCKET_NAME = "nodejs-docs-samples-tests";
  private static String BLURRED_BUCKET_NAME = System.getenv("BLURRED_BUCKET_NAME");

  // Loggers + handlers for various tested classes
  // (Must be declared at class-level, or LoggingHandler won't detect log records!)
  private static final Logger LOGGER = Logger.getLogger(ImageMagick.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  // Use GSON (https://github.com/google/gson) to parse JSON content.
  private Gson gson = new Gson();

  @Rule
  public EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @BeforeClass
  public static void setUp() {
    LOGGER.addHandler(LOG_HANDLER);
  }

  @After
  public void afterTest() {
    LOG_HANDLER.clear();
  }

  @Test
  public void functionsImagemagickAnalyze_shouldBlurOffensiveImages() {
    String imageName = "zombie.jpg";

    GcsEvent event = new GcsEvent();
    event.setBucket(BUCKET_NAME);
    event.setName(imageName);

    new ImageMagick().accept(event, new MockContext());

    List<LogRecord> logs = LOG_HANDLER.getStoredLogRecords();
    String uploadedMessage = String.format(
        "Blurred image uploaded to: gs://%s/%s", BLURRED_BUCKET_NAME, imageName);
    assertThat(logs.get(2).getMessage()).isEqualTo(uploadedMessage);
  }

  @Test
  public void functionsImagemagickAnalyze_shouldHandleSafeImages() {
    GcsEvent event = new GcsEvent();
    event.setBucket(BUCKET_NAME);
    event.setName("wakeupcat.jpg");

    new ImageMagick().accept(event, new MockContext());

    assertThat(LOG_HANDLER.getStoredLogRecords().get(1).getMessage()).isEqualTo(
        "Detected wakeupcat.jpg as OK.");
  }

  @Test
  public void functionsImagemagickAnalyze_shouldHandleMissingImages() {
    GcsEvent event = new GcsEvent();
    event.setBucket(BUCKET_NAME);
    event.setName("missing.jpg");

    new ImageMagick().accept(event, new MockContext());

    assertThat(LOG_HANDLER.getStoredLogRecords().get(1).getMessage()).contains(
        "Error opening file");
  }
}
