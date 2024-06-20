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

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.cloud.aiplatform.v1beta1.FeatureOnlineStore;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FeatureOnlineStoreSamplesTest {
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);

  private static final String PROJECT_ID = System.getenv("UCAIP_PROJECT_ID");
  private static final int MIN_NODE_COUNT = 1;
  private static final int MAX_NODE_COUNT = 2;
  private static final int TARGET_CPU_UTILIZATION = 60;
  private static final String DESCRIPTION = "Test Description";
  private static final int MONITORING_INTERVAL_DAYS = 1;
  private static final boolean USE_FORCE = true;
  private static final String LOCATION = "us-central1";
  private static final String ENDPOINT = "us-central1-aiplatform.googleapis.com:443";
  private static final int TIMEOUT = 600;
  private String featureOnlineStoreId;

  private static void requireEnvVar(String varName) {
    String errorMessage =
        String.format("Environment variable '%s' is required to perform these tests.", varName);
    assertNotNull(errorMessage, System.getenv(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    // requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("UCAIP_PROJECT_ID");
  }


  @Test
  public void testCreateAndDeleteFeaturestore()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Create the featureOnlineStore
    String tempUuid = UUID.randomUUID().toString().replaceAll("-", "_").substring(0, 25);
    String id = String.format("temp_fos_samples_test_%s", tempUuid);
    FeatureOnlineStore featureOnlineStoreResponse;

    featureOnlineStoreResponse =
        CreateFeatureOnlineStoreFixedNodesSample.createFeatureOnlineStoreFixedNodesSample(
            PROJECT_ID,
            id,
            MIN_NODE_COUNT,
            MAX_NODE_COUNT,
            TARGET_CPU_UTILIZATION,
            LOCATION,
            ENDPOINT,
            TIMEOUT);

    // Assert
    featureOnlineStoreId =
        featureOnlineStoreResponse.getName().split("featureOnlineStores/")[1].split("\n")[0].trim();
    assertThat(featureOnlineStoreId).isEqualTo(id);

    // Delete the featureOnlineStore
    DeleteFeatureOnlineStoreSample.deleteFeatureOnlineStoreSample(
        PROJECT_ID, featureOnlineStoreId, USE_FORCE, LOCATION, ENDPOINT, TIMEOUT);
  }
}
