/*
 * Copyright 2023 Google LLC
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

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.aiplatform.v1beta1.DeleteOperationMetadata;
import com.google.cloud.aiplatform.v1beta1.PipelineServiceClient;
import com.google.cloud.aiplatform.v1beta1.PipelineServiceSettings;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CreatePipelineJobCodeModelTuningSampleTest {
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);

  private static final String PROJECT = System.getenv("UCAIP_PROJECT_ID");
  private static final String LOCATION = "europe-west4";
  private static final String OUTPUT_DIR =
      "gs://ucaip-samples-europe-west4/training_pipeline_output";
  private static final String DATASET_URI =
      "gs://cloud-samples-data/ai-platform/generative_ai/sql_create_context.jsonl";
  private static final int TRAINING_STEPS = 300;
  private String pipelineJobName;
  private ByteArrayOutputStream bout;
  private PrintStream originalPrintStream;

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
      throws IOException, InterruptedException, TimeoutException, ExecutionException {
    final String endpoint = String.format("%s-aiplatform.googleapis.com:443", LOCATION);
    PipelineServiceSettings pipelineServiceSettings =
        PipelineServiceSettings.newBuilder().setEndpoint(endpoint).build();

    try (PipelineServiceClient pipelineServiceClient =
        PipelineServiceClient.create(pipelineServiceSettings)) {
      // Cancel the PipelineJob
      pipelineServiceClient.cancelPipelineJob(pipelineJobName);
      TimeUnit.MINUTES.sleep(2);

      // Delete the PipelineJob
      int retryCount = 3;
      while (retryCount > 0) {
        retryCount--;
        try {
          OperationFuture<Empty, DeleteOperationMetadata> operationFuture =
              pipelineServiceClient.deletePipelineJobAsync(pipelineJobName);
          operationFuture.get(300, TimeUnit.SECONDS);

          // if delete operation is successful, break out of the loop and continue
          break;
        } catch (StatusRuntimeException e) {
          // wait for another 1 minute, then retry
          System.out.println("Retrying (due to unfinished cancellation operation)...");
          TimeUnit.MINUTES.sleep(1);
        } catch (Exception otherExceptions) {
          // other exception, let them throw
          throw otherExceptions;
        }
      }
    }

    System.out.flush();
    System.setOut(originalPrintStream);
  }

  @Test
  public void createPipelineJobModelModelTuningSample() throws IOException {
    final String pipelineJobDisplayName =
        String.format(
            "temp_create_pipeline_job_test_%s",
            UUID.randomUUID().toString().replaceAll("-", "_").substring(0, 26));

    final String modelDisplayName =
        String.format(
            "temp_create_pipeline_job_code_model_test_%s",
            UUID.randomUUID().toString().replaceAll("-", "_").substring(0, 26));

    // Act
    CreatePipelineJobCodeModelTuningSample.createPipelineJobCodeModelTuningSample(
        PROJECT,
        LOCATION,
        pipelineJobDisplayName,
        modelDisplayName,
        OUTPUT_DIR,
        DATASET_URI,
        TRAINING_STEPS);

    // Assert
    String got = bout.toString();
    assertThat(got).contains(pipelineJobDisplayName);
    pipelineJobName = got.split("Name: ")[1].split("\n")[0];
  }
}
