/*
 * Copyright 2024 Google LLC
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

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import com.google.api.gax.rpc.InvalidArgumentException;
import com.google.cloud.securitycenter.v1.Asset;
import com.google.cloud.securitycenter.v1.ListAssetsRequest;
import com.google.cloud.securitycenter.v1.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.OrganizationName;
import com.google.cloud.securitycenter.v2.SecurityMarks;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
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
  private static final String LOCATION = "global";
  private static String assetId, assetName;
  private static ByteArrayOutputStream stdOut;

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule =
      new MultipleAttemptsRule(3, 120000); // 2 minutes

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertThat(System.getenv(envVarName)).isNotEmpty();
  }

  // Extracts the asset ID from a full resource name.
  private static String extractAssetId(String assetPath) {
    // Pattern to match the asset ID at the end of the resource name.
    Pattern pattern = Pattern.compile("assets/(\\d+)$");
    Matcher matcher = pattern.matcher(assetPath);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return assetPath;
  }

  @SuppressWarnings("deprecation")
  @BeforeClass
  public static void setUp() throws IOException, InterruptedException {
    final PrintStream out = System.out;
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");

    // Fetch a valid asset ID dynamically
    try (SecurityCenterClient client = SecurityCenterClient.create()) {
      OrganizationName orgName = OrganizationName.of(ORGANIZATION_ID);
      ListAssetsRequest request =
          ListAssetsRequest.newBuilder().setParent(orgName.toString()).setPageSize(1).build();

      Asset asset = client.listAssets(request).iterateAll().iterator().next().getAsset();
      assetName = asset.getName(); // Get the full resource name for the asset
      assetId = extractAssetId(assetName);
    } catch (InvalidArgumentException e) {
      System.err.println("Error retrieving asset ID: " + e.getMessage());
      throw e;
    }

    stdOut = null;
    System.setOut(out);
    TimeUnit.MINUTES.sleep(1);
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
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
    SecurityMarks response =
        AddSecurityMarksToAssets.addToAsset(ORGANIZATION_ID, LOCATION, assetId);

    assertTrue(response.getMarksOrThrow("key_a").contains("value_a"));
    assertTrue(response.getMarksOrThrow("key_b").contains("value_b"));
  }

  @Test
  public void testDeleteSecurityMarksOnAsset() throws IOException {
    SecurityMarks response =
        DeleteAssetsSecurityMarks.deleteSecurityMarks(ORGANIZATION_ID, LOCATION, assetId);

    assertFalse(response.containsMarks("key_a"));
    assertFalse(response.containsMarks("key_b"));
  }

  @Test
  public void testAddAndDeleteSecurityMarks() throws IOException {
    SecurityMarks response =
        AddDeleteSecurityMarks.addDeleteSecurityMarks(ORGANIZATION_ID, LOCATION, assetId);

    // Assert update for key_a
    assertTrue(response.getMarksOrThrow("key_a").contains("new_value_for_a"));

    // Assert deletion for key_b
    assertFalse(response.getMarksMap().containsKey("key_b"));
  }
}
