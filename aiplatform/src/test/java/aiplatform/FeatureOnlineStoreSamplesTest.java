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
 *
 *
 * Create a featurestore resource to contain entity types and features. See
 * https://cloud.google.com/vertex-ai/docs/featurestore/setup before running
 * the code snippet
 */

package aiplatform;

import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

@RunWith(JUnit4.class)
public class FeatureOnlineStoreSamplesTest {
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);

  private static final String PROJECT_ID = System.getenv("UCAIP_PROJECT_ID");
  private static final int MIN_NODE_COUNT = 1;
  private static final int MAX_NODE_COUNT = 2;
  private static final String DESCRIPTION = "Test Description";
  private static final int MONITORING_INTERVAL_DAYS = 1;
  private static final boolean USE_FORCE = true;
  private static final String LOCATION = "us-central1";
  private static final String ENDPOINT = "us-central1-aiplatform.googleapis.com:443";
  private static final int TIMEOUT = 1800;
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;
  private String featureOnlineStoreId;

  private static void requireEnvVar(String varName) {
    String errorMessage =
        String.format("Environment variable '%s' is required to perform these tests.", varName);
    assertNotNull(errorMessage, System.getenv(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("UCAIP_PROJECT_ID");
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown()
      throws InterruptedException, ExecutionException, IOException, TimeoutException {

    if (featureOnlineStoreId != null) {
      // Delete the featureOnlineStore
      DeleteFeatureOnlineStoreSample.deleteFeatureOnlineStoreSample(
          PROJECT_ID, featureOnlineStoreId, USE_FORCE, LOCATION, ENDPOINT, TIMEOUT);

      // Assert
      String deleteFeatureOnlineStoreResponse = bout.toString();
      assertThat(deleteFeatureOnlineStoreResponse).contains("Deleted FeatureOnlineStore");
    }
    System.out.flush();
    System.setOut(originalPrintStream);
  }

  @Test
  public void testCreateFeaturestoreSample()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Create the featureOnlineStore
    String tempUuid = UUID.randomUUID().toString().replaceAll("-", "_").substring(0, 25);
    String id = String.format("temp_fos_samples_test_%s", tempUuid);
    CreateFeatureOnlineStoreFixedNodesSample.createFeatureOnlineStoreFixedNodesSample(
        PROJECT_ID, id, MIN_NODE_COUNT, MAX_NODE_COUNT, LOCATION, ENDPOINT, TIMEOUT);

    // Assert
    String createFeatureOnlineStoreResponse = bout.toString();
    assertThat(createFeatureOnlineStoreResponse).contains("Create FeatureOnlineStore Response");
    featureOnlineStoreId =
        createFeatureOnlineStoreResponse.split("Name: ")[1].split("featureOnlineStores/")[1]
            .split("\n")[0].trim();
  }
}
