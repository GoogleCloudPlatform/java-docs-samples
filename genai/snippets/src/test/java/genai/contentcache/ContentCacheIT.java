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

package genai.contentcache;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ContentCacheIT {

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
  public void testContentCache() {

    // Test create cache
    Optional<String> cacheName =
        ContentCacheCreateWithTextGcsPdf.contentCacheCreateWithTextGcsPdf(GEMINI_FLASH);
    assertThat(cacheName).isPresent();
    assertThat(cacheName.get()).isNotEmpty();

    // Test list cache
    ContentCacheList.contentCacheList();
    assertThat(bout.toString()).contains("Name: ");
    assertThat(bout.toString()).contains("Model: ");
    assertThat(bout.toString()).contains("Last updated at: ");
    assertThat(bout.toString()).contains("Expires at: ");
    bout.reset();

    // Test update cache
    String cacheResourceName = cacheName.get();
    ContentCacheUpdate.contentCacheUpdate(cacheResourceName);
    assertThat(bout.toString()).contains("Expire time: ");
    assertThat(bout.toString()).contains("Expire time after update: ");
    assertThat(bout.toString()).contains(String.format("Updated cache: %s", cacheResourceName));
    bout.reset();

    // Test use cache with text
    String response =
        ContentCacheUseWithText.contentCacheUseWithText(GEMINI_FLASH, cacheResourceName);
    assertThat(response).isNotEmpty();
    assertThat(response).isNotNull();

    // Test delete cache
    ContentCacheDelete.contentCacheDelete(cacheResourceName);
    assertThat(bout.toString()).contains(String.format("Deleted cache: %s", cacheResourceName));
  }
}
