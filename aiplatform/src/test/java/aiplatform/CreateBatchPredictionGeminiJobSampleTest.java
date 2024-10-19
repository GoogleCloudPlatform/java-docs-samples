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

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import aiplatform.batchpredict.CreateBatchPredictionGeminiBigqueryJobSample;
import aiplatform.batchpredict.CreateBatchPredictionGeminiJobSample;
import com.google.cloud.aiplatform.v1.BatchPredictionJob;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateBatchPredictionGeminiJobSampleTest {
  private static final String PROJECT = System.getenv("UCAIP_PROJECT_ID");
  private static final String GCS_OUTPUT_URI = "gs://ucaip-samples-test-output/";
  private static final String BIGQUERY_DESTINATION_OUTPUT_URI_PREFIX = "bq://ucaip-sample-tests";

  private ByteArrayOutputStream bout;
  private PrintStream originalPrintStream;
  private String batchPredictionGcsJobId;
  private String batchPredictionBqJobId;

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
    PrintStream out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown()
      throws InterruptedException, ExecutionException, IOException, TimeoutException {
    // Cloud Storage job
    CancelBatchPredictionJobSample.cancelBatchPredictionJobSample(PROJECT, batchPredictionGcsJobId);

    // Assert
    String cancelResponse = bout.toString();
    assertThat(cancelResponse, containsString("Cancelled the Batch Prediction Job"));
    TimeUnit.MINUTES.sleep(2);

    // Delete the Batch Prediction Job
    DeleteBatchPredictionJobSample.deleteBatchPredictionJobSample(PROJECT, batchPredictionGcsJobId);

    // Assert
    String deleteResponse = bout.toString();
    assertThat(deleteResponse, containsString("Deleted Batch"));

    // BigQuery job
    CancelBatchPredictionJobSample.cancelBatchPredictionJobSample(PROJECT, batchPredictionBqJobId);

    // Assert
    cancelResponse = bout.toString();
    assertThat(cancelResponse, containsString("Cancelled the Batch Prediction Job"));
    TimeUnit.MINUTES.sleep(2);

    // Delete the Batch Prediction Job
    DeleteBatchPredictionJobSample.deleteBatchPredictionJobSample(PROJECT, batchPredictionBqJobId);

    // Assert
    deleteResponse = bout.toString();
    assertThat(deleteResponse, containsString("Deleted Batch"));

    System.out.flush();
    System.setOut(originalPrintStream);
  }

  @Test
  public void testCreateBatchPredictionGeminiJobSampleTest() throws IOException {
    // Cloud Storage job
    // Act
    BatchPredictionJob job =
        CreateBatchPredictionGeminiJobSample.createBatchPredictionGeminiJobSample(
            PROJECT, GCS_OUTPUT_URI);

    // Assert
    assertThat(job.getName(), containsString("batchPredictionJobs"));

    String[] id = job.getName().split("/");
    batchPredictionGcsJobId = id[id.length - 1];

    // BigQuery job
    // Act
    job =
        CreateBatchPredictionGeminiBigqueryJobSample.createBatchPredictionGeminiBigqueryJobSample(
            PROJECT, BIGQUERY_DESTINATION_OUTPUT_URI_PREFIX);

    // Assert
    assertThat(job.getName(), containsString("batchPredictionJobs"));

    id = job.getName().split("/");
    batchPredictionBqJobId = id[id.length - 1];
  }
}
