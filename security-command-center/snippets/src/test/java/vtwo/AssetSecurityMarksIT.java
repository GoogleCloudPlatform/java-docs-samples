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

package vtwo;

// import static org.junit.Assert.assertThat;
import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import vtwo.assets.AddDeleteSecurityMarks;
import vtwo.assets.AddSecurityMarksToAssets;
import vtwo.assets.DeleteAssetsSecurityMarks;

@RunWith(JUnit4.class)
public class AssetSecurityMarksIT {

  private static final String ORGANIZATION_ID = System.getenv("SCC_PROJECT_ORG_ID");
  private static String assetId;
  private static ByteArrayOutputStream stdOut;

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule =
      new MultipleAttemptsRule(3, 120000); // 2 minutes

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertThat(System.getenv(envVarName)).isNotEmpty();
  }

  // Extracts the asset ID from a full resource name.
  // This regex pattern matches the last segment of the resource name,
  // which consists of digits after the final forward slash (e.g., "assets/12345").
  private static String extractAssetId(String assetPath) {
    // Pattern to match the asset ID at the end of the resource name.
    Pattern pattern = Pattern.compile("assets/([^/]+)$");
    Matcher matcher = pattern.matcher(assetPath);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return assetPath;
  }

  @BeforeClass
  public static void setUp() throws IOException, InterruptedException {

    // Validate required environment variables.
    requireEnvVar("SCC_PROJECT_ORG_ID");

    // Load static_asset.json from resources
    // Since there are no APIs to create an Asset
    InputStream inputStream =
        AssetSecurityMarksIT.class.getClassLoader().getResourceAsStream("static_asset.json");

    if (inputStream == null) {
      throw new IOException("static_asset.json file not found in resources.");
    }

    // Convert InputStream to String
    String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

    // Parse JSON (using Gson)
    JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();

    // Extract assetId from mock data
    assetId = extractAssetId(jsonObject.get("name").getAsString());

    if (assetId == null || assetId.isEmpty()) {
      throw new IllegalStateException("Asset ID is missing from static_asset.json");
    }
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
  }

  @After
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }

  @AfterClass
  public static void cleanUp() {
    System.setOut(System.out);
  }

  @Test
  public void testAddSecurityMarksToAsset() throws IOException {
    com.google.cloud.securitycenter.v2.SecurityMarks response =
        AddSecurityMarksToAssets.addToAsset(ORGANIZATION_ID, assetId);

    assertTrue(response.getMarksOrThrow("key_a").contains("value_a"));
    assertTrue(response.getMarksOrThrow("key_b").contains("value_b"));
  }

  @Test
  public void testDeleteSecurityMarksOnAsset() throws IOException {
    com.google.cloud.securitycenter.v2.SecurityMarks response =
        DeleteAssetsSecurityMarks.deleteSecurityMarks(ORGANIZATION_ID, assetId);

    assertFalse(response.containsMarks("key_a"));
    assertFalse(response.containsMarks("key_b"));
  }

  @Test
  public void testAddAndDeleteSecurityMarks() throws IOException {
    com.google.cloud.securitycenter.v2.SecurityMarks response =
        AddDeleteSecurityMarks.addAndDeleteSecurityMarks(ORGANIZATION_ID, assetId);

    assertTrue(response.getMarksOrThrow("key_a").contains("new_value_for_a"));
    assertTrue(response.getMarksOrThrow("key_b").contains("new_value_for_b"));
  }
}
