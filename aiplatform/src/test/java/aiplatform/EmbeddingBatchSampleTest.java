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

package aiplatform;

import com.google.cloud.aiplatform.v1.BatchPredictionJob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.util.UUID;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EmbeddingBatchSampleTest extends TestCase {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION = "us-central1";
  private static String BUCKET_NAME;
  private static final String GCS_SOURCE_URI =
      "gs://cloud-samples-data/generative-ai/embeddings/embeddings_input.jsonl";
  private static final String GCS_OUTPUT_URI =
      String.format("gs://%s/embedding_batch_output", BUCKET_NAME);
  private static final String MODEL_ID = "text-embedding-005";
  static Storage storage;
  static Bucket bucket;

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
    storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    storage.create(BucketInfo.of(BUCKET_NAME));
  }

  @AfterClass
  public static void afterClass() {
    // Delete the Google Cloud Storage bucket created for usage reports.
    storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    bucket = storage.get(BUCKET_NAME);
    bucket.delete();
  }

  @Test
  public void testEmbeddingBatchSample() throws IOException {

    BatchPredictionJob batchPredictionJob =
        EmbeddingBatchSample.embeddingBatchSample(PROJECT_ID, LOCATION, GCS_SOURCE_URI,
            GCS_OUTPUT_URI, MODEL_ID);

    Assertions.assertNotNull(batchPredictionJob);
    assertTrue(batchPredictionJob.getDisplayName().contains("my embedding batch job "));
    assertTrue(batchPredictionJob.getModel()
        .contains("publishers/google/models/textembedding-gecko"));
  }
}
