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

package genai.batchprediction;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.genai.types.JobState;
import java.io.ByteArrayOutputStream;
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
public class BatchPredictionIT {

  private static final String GEMINI_FLASH = "gemini-2.5-flash";
  private static final String EMBEDDING_MODEL = "text-embedding-005";
  private static final String BUCKET_NAME = "java-docs-samples-testing";
  private static final String PREFIX = "genai-batch-prediction" + UUID.randomUUID();
  private static final String OUTPUT_GCS_URI = String.format("gs://%s/%s", BUCKET_NAME, PREFIX);
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

  @AfterClass
  public static void cleanup() {
    Storage storage = StorageOptions.getDefaultInstance().getService();
    Page<Blob> blobs = storage.list(BUCKET_NAME, Storage.BlobListOption.prefix(PREFIX));

    for (Blob blob : blobs.iterateAll()) {
      storage.delete(blob.getBlobId());
    }
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
    bout.reset();
  }

  @Test
  public void testBatchPredictionWithGcs() throws InterruptedException {
    JobState response = BatchPredictionWithGcs.createBatchJob(GEMINI_FLASH, OUTPUT_GCS_URI);
    assertThat(response.toString()).isNotEmpty();
    assertThat(response.toString()).isEqualTo("JOB_STATE_SUCCEEDED");
    assertThat(bout.toString()).contains("Job name: ");
    assertThat(bout.toString()).contains("Job state: JOB_STATE_SUCCEEDED");
  }

  @Test
  public void testBatchPredictionEmbeddingsWithGcs() throws InterruptedException {
    JobState response =
        BatchPredictionEmbeddingsWithGcs.createBatchJob(EMBEDDING_MODEL, OUTPUT_GCS_URI);
    assertThat(response.toString()).isNotEmpty();
    assertThat(response.toString()).isEqualTo("JOB_STATE_SUCCEEDED");
    assertThat(bout.toString()).contains("Job name: ");
    assertThat(bout.toString()).contains("Job state: JOB_STATE_SUCCEEDED");
  }
}
