/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aiplatform;

// [START generativeaionvertexai_batch_text_predict]

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BatchTextPredictionSampleTest {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION = "us-central1";
  private static String BUCKET_NAME;
  private static final String GCS_SOURCE_URI =
      "gs://cloud-samples-data/batch/prompt_for_batch_code_predict.jsonl";
  private static final String GCS_DESTINATION_OUTPUT_PREFIX =
      String.format("gs://%s/ucaip-test-output/", BUCKET_NAME);
  private static final String MODEL_ID = "text-bison";
  private ByteArrayOutputStream stdOut;

  private static void requireEnvVar(String varName) {
    String errorMessage =
        String.format("Environment variable '%s' is required to perform these tests.", varName);
    assertNotNull(errorMessage, System.getenv(varName));
  }

  @BeforeClass
  public static void checkRequirements() throws IOException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
    BUCKET_NAME = "my-new-test-bucket" + UUID.randomUUID();

    // Create a Google Cloud Storage bucket for UsageReports
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    storage.create(BucketInfo.of(BUCKET_NAME));
  }

  @AfterClass
  public static void afterClass() {
    // Delete the Google Cloud Storage bucket created for usage reports.
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    Bucket bucket = storage.get(BUCKET_NAME);
    bucket.delete();
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

  @Test
  public void testBatchTextPredictionSample() throws IOException {
    BatchTextPredictionSample.batchTextPrediction(PROJECT_ID, LOCATION, GCS_SOURCE_URI,
        GCS_DESTINATION_OUTPUT_PREFIX, MODEL_ID);
    assertThat(stdOut.toString()).contains("publishers/google/models/text-bison");
    assertThat(stdOut.toString()).contains("my batch text prediction job");
  }
}
// [END generativeaionvertexai_batch_text_predict]