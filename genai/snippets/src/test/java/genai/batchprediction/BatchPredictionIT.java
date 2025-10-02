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
import static com.google.genai.types.JobState.Known.JOB_STATE_SUCCEEDED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.genai.Batches;
import com.google.genai.Client;
import com.google.genai.types.BatchJob;
import com.google.genai.types.BatchJobSource;
import com.google.genai.types.CreateBatchJobConfig;
import com.google.genai.types.JobState;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
public class BatchPredictionIT {

  private static final String GEMINI_FLASH = "gemini-2.5-flash";
  private static final String EMBEDDING_MODEL = "text-embedding-005";
  private static String jobName;
  private static String outputGcsUri;
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private Client mockedClient;
  private MockedStatic<Client> mockedStatic;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
    jobName = "projects/project_id/locations/us-central1/batchPredictionJobs/job_id";
    outputGcsUri = "gs://your-bucket/your-prefix";
  }

  @Before
  public void setUp() throws NoSuchFieldException, IllegalAccessException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    // Mock builder, client, batches and response
    Client.Builder mockedBuilder = mock(Client.Builder.class, RETURNS_SELF);
    mockedClient = mock(Client.class);
    Batches mockedBatches = mock(Batches.class);
    BatchJob mockedBatchJobResponse = mock(BatchJob.class);
    // Static mock of Client.builder()
    mockedStatic = mockStatic(Client.class);
    mockedStatic.when(Client::builder).thenReturn(mockedBuilder);
    when(mockedBuilder.build()).thenReturn(mockedClient);

    // Inject mockBatches into mockClient by using reflection because
    // 'batches' is a final field and cannot be mockable directly
    Field field = Client.class.getDeclaredField("batches");
    field.setAccessible(true);
    field.set(mockedClient, mockedBatches);

    when(mockedClient.batches.create(
            anyString(), any(BatchJobSource.class), any(CreateBatchJobConfig.class)))
        .thenReturn(mockedBatchJobResponse);

    when(mockedBatchJobResponse.name()).thenReturn(Optional.of(jobName));
    when(mockedBatchJobResponse.state()).thenReturn(Optional.of(new JobState(JOB_STATE_SUCCEEDED)));
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
    mockedStatic.close();
  }

  @Test
  public void testBatchPredictionWithGcs() throws InterruptedException {

    JobState response = BatchPredictionWithGcs.createBatchJob(GEMINI_FLASH, outputGcsUri);

    verify(mockedClient.batches, times(1))
        .create(anyString(), any(BatchJobSource.class), any(CreateBatchJobConfig.class));

    assertThat(response.toString()).isNotEmpty();
    assertThat(response.toString()).isEqualTo("JOB_STATE_SUCCEEDED");
    assertThat(bout.toString()).contains("Job name: " + jobName);
    assertThat(bout.toString()).contains("Job state: JOB_STATE_SUCCEEDED");
  }

  @Test
  public void testBatchPredictionEmbeddingsWithGcs() throws InterruptedException {

    JobState response =
        BatchPredictionEmbeddingsWithGcs.createBatchJob(EMBEDDING_MODEL, outputGcsUri);

    verify(mockedClient.batches, times(1))
        .create(anyString(), any(BatchJobSource.class), any(CreateBatchJobConfig.class));

    assertThat(response.toString()).isNotEmpty();
    assertThat(response.toString()).isEqualTo("JOB_STATE_SUCCEEDED");
    assertThat(bout.toString()).contains("Job name: " + jobName);
    assertThat(bout.toString()).contains("Job state: JOB_STATE_SUCCEEDED");
  }
}
