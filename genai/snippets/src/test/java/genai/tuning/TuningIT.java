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

package genai.tuning;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.Pager;
import com.google.genai.Tunings;
import com.google.genai.types.CreateTuningJobConfig;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GetTuningJobConfig;
import com.google.genai.types.JobState;
import com.google.genai.types.ListTuningJobsConfig;
import com.google.genai.types.TunedModel;
import com.google.genai.types.TuningDataset;
import com.google.genai.types.TuningJob;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
public class TuningIT {

  private static final String GEMINI_FLASH = "gemini-2.5-flash";
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private Client.Builder mockedBuilder;
  private Client mockedClient;
  private Tunings mockedTunings;
  private TuningJob mockedResponse;
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
  }

  @Before
  public void setUp() throws NoSuchFieldException, IllegalAccessException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
    mockedBuilder = mock(Client.Builder.class, RETURNS_SELF);
    mockedClient = mock(Client.class);
    mockedTunings = mock(Tunings.class);
    mockedResponse = mock(TuningJob.class);
    mockedStatic = mockStatic(Client.class);
    mockedStatic.when(Client::builder).thenReturn(mockedBuilder);
    when(mockedBuilder.build()).thenReturn(mockedClient);
    // Using reflection because 'tunings' is a final field and cannot be mockable directly
    Field field = Client.class.getDeclaredField("tunings");
    field.setAccessible(true);
    field.set(mockedClient, mockedTunings);
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
    mockedStatic.close();
  }

  @Test
  public void testTuningJobCreate() throws InterruptedException {

    String expectedResponse = "test-tuning-job";

    when(mockedClient.tunings.tune(
            anyString(), any(TuningDataset.class), any(CreateTuningJobConfig.class)))
        .thenReturn(mockedResponse);

    TunedModel tunedModel =
        TunedModel.builder().model("test-model").endpoint("test-endpoint").build();
    when(mockedResponse.name()).thenReturn(Optional.of("test-tuning-job"));
    when(mockedResponse.experiment()).thenReturn(Optional.of("test-experiment"));
    when(mockedResponse.tunedModel()).thenReturn(Optional.of(tunedModel));
    when(mockedResponse.state())
        .thenReturn(Optional.of(new JobState(JobState.Known.JOB_STATE_SUCCEEDED)));

    String response = TuningJobCreate.createTuningJob(GEMINI_FLASH);

    verify(mockedClient.tunings, times(1))
        .tune(anyString(), any(TuningDataset.class), any(CreateTuningJobConfig.class));
    assertThat(response).isNotEmpty();
    assertThat(response).isEqualTo(expectedResponse);
  }

  @Test
  public void testTuningJobGet() {
    when(mockedClient.tunings.get(anyString(), any(GetTuningJobConfig.class)))
        .thenReturn(mockedResponse);
    when(mockedResponse.name()).thenReturn(Optional.of("test-tuning-job"));

    Optional<String> response = TuningJobGet.getTuningJob(GEMINI_FLASH);
    verify(mockedClient.tunings, times(1)).get(anyString(), any(GetTuningJobConfig.class));
    assertThat(response).isPresent();
    assertThat(response.get()).isEqualTo("test-tuning-job");
  }

  @Test
  public void testTuningJobList() {
    Pager<TuningJob> mockPagerResponse = mock(Pager.class);
    Iterator mockIterator = mock(Iterator.class);

    TuningJob tuningJob1 = TuningJob.builder().name("test-tuning-job1").build();
    TuningJob tuningJob2 = TuningJob.builder().name("test-tuning-job2").build();

    when(mockedClient.tunings.list(any(ListTuningJobsConfig.class))).thenReturn(mockPagerResponse);
    when(mockPagerResponse.size()).thenReturn(2);
    when(mockPagerResponse.iterator()).thenReturn(mockIterator);
    when(mockIterator.hasNext()).thenReturn(true, true, false);
    when(mockIterator.next()).thenReturn(tuningJob1, tuningJob2);

    Pager<TuningJob> tuningJobs = TuningJobList.listTuningJob();
    verify(mockedClient.tunings, times(1)).list(any(ListTuningJobsConfig.class));
    assertThat(tuningJobs.size()).isEqualTo(2);
    assertThat(bout.toString()).isNotEmpty();
    assertThat(bout.toString()).contains("test-tuning-job1");
    assertThat(bout.toString()).contains("test-tuning-job2");
  }

  @Test
  public void testTuningTextGenWithTxt() throws NoSuchFieldException, IllegalAccessException {
    Models mockedModels = mock(Models.class);
    // Using reflection because 'models' is a final field and cannot be mockable directly
    Field field = Client.class.getDeclaredField("models");
    field.setAccessible(true);
    field.set(mockedClient, mockedModels);

    when(mockedClient.tunings.get(anyString(), any(GetTuningJobConfig.class)))
        .thenReturn(mockedResponse);
    TunedModel tunedModel = TunedModel.builder().endpoint("test-endpoint").build();
    when(mockedResponse.tunedModel()).thenReturn(Optional.of(tunedModel));

    GenerateContentResponse mockedGeneratedResponse = mock(GenerateContentResponse.class);

    when(mockedClient.models.generateContent(
            anyString(), anyString(), any(GenerateContentConfig.class)))
        .thenReturn(mockedGeneratedResponse);
    when(mockedGeneratedResponse.text()).thenReturn("Example response");

    String response = TuningTextGenWithTxt.predictWithTunedEndpoint("test-tuning-job");

    verify(mockedClient.tunings, times(1)).get(anyString(), any(GetTuningJobConfig.class));
    verify(mockedClient.models, times(1))
        .generateContent(anyString(), anyString(), any(GenerateContentConfig.class));
    assertThat(response).isNotEmpty();
  }
}
