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

import static com.google.common.truth.Truth.assertThat;
import static java.util.stream.Collectors.toList;
import static junit.framework.TestCase.assertNotNull;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.aiplatform.v1.CancelPipelineJobRequest;
import com.google.cloud.aiplatform.v1.DeleteOperationMetadata;
import com.google.cloud.aiplatform.v1.PipelineJob;
import com.google.cloud.aiplatform.v1.PipelineServiceClient;
import com.google.cloud.aiplatform.v1.PipelineServiceSettings;
import com.google.cloud.aiplatform.v1.PipelineState;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.protobuf.Empty;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedRunnable;
import java.io.IOException;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EmbeddingModelTuningSampleTest {
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);

  private static final String API_ENDPOINT = "us-central1-aiplatform.googleapis.com:443";
  private static final String PROJECT = System.getenv("UCAIP_PROJECT_ID");
  private static final String BASE_MODEL_VERSION_ID = "text-embedding-004";
  private static final String TASK_TYPE = "DEFAULT";
  private static final String JOB_DISPLAY_NAME = "embedding-customization-pipeline-sample";
  private static final String CORPUS = 
      "gs://cloud-samples-data/ai-platform/embedding/goog-10k-2024/r11/corpus.jsonl";
  private static final String QUERIES = 
      "gs://cloud-samples-data/ai-platform/embedding/goog-10k-2024/r11/queries.jsonl";
  private static final String TRAIN_LABEL =
      "gs://cloud-samples-data/ai-platform/embedding/goog-10k-2024/r11/train.tsv";
  private static final String TEST_LABEL = 
      "gs://cloud-samples-data/ai-platform/embedding/goog-10k-2024/r11/test.tsv";
  private static final String OUTPUT_DIR =
      "gs://ucaip-samples-us-central1/training_pipeline_output";
  private static final double LEARNING_RATE_MULTIPLIER = 0.3;
  private static final int OUTPUT_DIMENSIONALITY = 512;
  private static final int BATCH_SIZE = 50;
  private static final int ITERATIONS = 300;

  private static Queue<String> JobNames = new LinkedList<String>();
  private static final RetryConfig RETRY_CONFIG =
      RetryConfig.custom()
          .maxAttempts(30)
          .waitDuration(Duration.ofSeconds(6))
          .retryExceptions(TimeoutException.class)
          .failAfterMaxAttempts(false)
          .build();
  private static final RetryRegistry RETRY_REGISTRY = RetryRegistry.of(RETRY_CONFIG);

  private static void requireEnvVar(String varName) {
    String errorMessage = String.format("Test requires environment variable '%s'.", varName);
    assertNotNull(errorMessage, System.getenv(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("UCAIP_PROJECT_ID");
  }

  @AfterClass
  public static void tearDown() throws Throwable {
    PipelineServiceSettings settings =
        PipelineServiceSettings.newBuilder().setEndpoint(API_ENDPOINT).build();
    try (PipelineServiceClient client = PipelineServiceClient.create(settings)) {
      List<CancelPipelineJobRequest> requests =
          JobNames.stream()
              .map(n -> CancelPipelineJobRequest.newBuilder().setName(n).build())
              .collect(toList());
      CheckedRunnable runnable =
          Retry.decorateCheckedRunnable(
              RETRY_REGISTRY.retry("delete-pipeline-jobs", RETRY_CONFIG),
              () -> {
                List<OperationFuture<Empty, DeleteOperationMetadata>> deletions =
                    requests.stream()
                        .map(
                            req -> {
                              client.cancelPipelineJobCallable().futureCall(req);
                              return client.deletePipelineJobAsync(req.getName());
                            })
                        .collect(toList());
                for (OperationFuture<Empty, DeleteOperationMetadata> d : deletions) {
                  d.get(0, TimeUnit.SECONDS);
                }
              });
      try {
        runnable.run();
      } catch (TimeoutException e) {
        // Do nothing.
      }
    }
  }

  @Test
  public void createPipelineJobEmbeddingModelTuningSample() throws IOException {
    PipelineJob job =
        EmbeddingModelTuningSample.createEmbeddingModelTuningPipelineJob(
            API_ENDPOINT,
            PROJECT,
            BASE_MODEL_VERSION_ID,
            TASK_TYPE,
            JOB_DISPLAY_NAME,
            OUTPUT_DIR,
            QUERIES,
            CORPUS,
            TRAIN_LABEL,
            TEST_LABEL,
            LEARNING_RATE_MULTIPLIER,
            OUTPUT_DIMENSIONALITY,
            BATCH_SIZE,
            ITERATIONS);
    assertThat(job.getState()).isNotEqualTo(PipelineState.PIPELINE_STATE_FAILED);
    JobNames.add(job.getName());
  }
}
