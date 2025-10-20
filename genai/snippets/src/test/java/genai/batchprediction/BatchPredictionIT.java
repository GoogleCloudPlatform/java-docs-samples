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
import static com.google.genai.types.JobState.Known.JOB_STATE_PENDING;
import static com.google.genai.types.JobState.Known.JOB_STATE_RUNNING;
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
import com.google.genai.types.GetBatchJobConfig;
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
  private static String jobName;
  private static String outputBqUri;
  private ByteArrayOutputStream bout;
  private Batches mockedBatches;
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
    outputBqUri = "bq://your-project.your_dataset.your_table";
  }

  @Before
  public void setUp() throws NoSuchFieldException, IllegalAccessException {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    // Arrange
    Client.Builder mockedBuilder = mock(Client.Builder.class, RETURNS_SELF);
    mockedBatches = mock(Batches.class);
    mockedStatic = mockStatic(Client.class);
    mockedStatic.when(Client::builder).thenReturn(mockedBuilder);
    Client mockedClient = mock(Client.class);
    when(mockedBuilder.build()).thenReturn(mockedClient);

    // Using reflection because 'batches' is a final field and cannot be mocked directly.
    // This is brittle but necessary for testing this class structure.
    Field field = Client.class.getDeclaredField("batches");
    field.setAccessible(true);
    field.set(mockedClient, mockedBatches);

    // Mock the sequence of job states to test the polling loop
    BatchJob pendingJob = mock(BatchJob.class);
    when(pendingJob.name()).thenReturn(Optional.of(jobName));
    when(pendingJob.state()).thenReturn(Optional.of(new JobState(JOB_STATE_PENDING)));

    BatchJob runningJob = mock(BatchJob.class);
    when(runningJob.state()).thenReturn(Optional.of(new JobState(JOB_STATE_RUNNING)));

    BatchJob succeededJob = mock(BatchJob.class);
    when(succeededJob.state()).thenReturn(Optional.of(new JobState(JOB_STATE_SUCCEEDED)));

    when(mockedBatches.create(
            anyString(), any(BatchJobSource.class), any(CreateBatchJobConfig.class)))
        .thenReturn(pendingJob);
    when(mockedBatches.get(anyString(), any(GetBatchJobConfig.class)))
        .thenReturn(runningJob, succeededJob);
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
    mockedStatic.close();
  }

  @Test
  public void testBatchPredictionWithBq() throws InterruptedException {
    // Act
    Optional<JobState> response = BatchPredictionWithBq.createBatchJob(GEMINI_FLASH, outputBqUri);

    // Assert
    verify(mockedBatches, times(1))
        .create(anyString(), any(BatchJobSource.class), any(CreateBatchJobConfig.class));
    verify(mockedBatches, times(2)).get(anyString(), any(GetBatchJobConfig.class));

    assertThat(response).isPresent();
    assertThat(response.get().knownEnum()).isEqualTo(JOB_STATE_SUCCEEDED);

    String output = bout.toString();
    assertThat(output).contains("Job name: " + jobName);
    assertThat(output).contains("Job state: JOB_STATE_PENDING");
    assertThat(output).contains("Job state: JOB_STATE_RUNNING");
    assertThat(output).contains("Job state: JOB_STATE_SUCCEEDED");
  }
}
