/*
 * Copyright 2017 Google Inc.
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

package dlp.snippets;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.core.SettableApiFuture;
import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.cloud.dlp.v2.DlpServiceSettings;
import com.google.common.collect.ImmutableList;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.CategoricalStatsResult;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.CategoricalStatsResult.CategoricalStatsHistogramBucket;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KAnonymityResult;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KAnonymityResult.KAnonymityEquivalenceClass;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KAnonymityResult.KAnonymityHistogramBucket;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KMapEstimationResult;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KMapEstimationResult.KMapEstimationHistogramBucket;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KMapEstimationResult.KMapEstimationQuasiIdValues;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.LDiversityResult;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.LDiversityResult.LDiversityEquivalenceClass;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.LDiversityResult.LDiversityHistogramBucket;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.NumericalStatsResult;
import com.google.privacy.dlp.v2.CreateDlpJobRequest;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.GetDlpJobRequest;
import com.google.privacy.dlp.v2.Value;
import com.google.privacy.dlp.v2.ValueFrequency;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@RunWith(JUnit4.class)
public class RiskAnalysisTests extends TestBase {

  private SettableApiFuture<Boolean> doneMock;

  @Override
  protected ImmutableList<String> requiredEnvVars() {
    return ImmutableList.of("GOOGLE_APPLICATION_CREDENTIALS", "GOOGLE_CLOUD_PROJECT");
  }

  @Test
  public void testNumericalStats() throws Exception {
    DlpServiceClient dlpServiceClient = mock(DlpServiceClient.class);
    doneMock = mock(SettableApiFuture.class);
    try (MockedStatic<DlpServiceClient> mockedStatic = Mockito.mockStatic(DlpServiceClient.class)) {
      mockedStatic.when(() -> DlpServiceClient.create()).thenReturn(dlpServiceClient);
      try (MockedStatic<SettableApiFuture> mockedStatic1 =
          Mockito.mockStatic(SettableApiFuture.class)) {
        mockedStatic1.when(() -> SettableApiFuture.create()).thenReturn(doneMock);

        DlpJob dlpJob =
            DlpJob.newBuilder()
                .setName("projects/project_id/locations/global/dlpJobs/job_id")
                .setState(DlpJob.JobState.DONE)
                .setRiskDetails(
                    AnalyzeDataSourceRiskDetails.newBuilder()
                        .setNumericalStatsResult(
                            NumericalStatsResult.newBuilder()
                                .setMaxValue(Value.newBuilder().setIntegerValue(1).build())
                                .setMinValue(Value.newBuilder().setIntegerValue(1).build())
                                .addQuantileValues(Value.newBuilder().setFloatValue(1).build()))
                        .build())
                .build();

        when(doneMock.get(15, TimeUnit.MINUTES)).thenReturn(true);
        when(dlpServiceClient.createDlpJob(any(CreateDlpJobRequest.class))).thenReturn(dlpJob);
        when(dlpServiceClient.getDlpJob((GetDlpJobRequest) any())).thenReturn(dlpJob);
        RiskAnalysisNumericalStats.numericalStatsAnalysis(
            "bigquery-public-data", "usa_names", "usa_1910_current", "topic_id", "subscription_id");
        String output = bout.toString();
        assertThat(output)
            .contains("Job name: projects/project_id/locations/global/dlpJobs/job_id");
        assertThat(output).contains("Job status: DONE");
        assertThat(output).contains("Value at");
        verify(dlpServiceClient, times(1)).createDlpJob(any(CreateDlpJobRequest.class));
        verify(dlpServiceClient, times(1)).getDlpJob(any(GetDlpJobRequest.class));
      }
    }
  }

  @Test
  public void testCategoricalStats() throws Exception {
    DlpServiceClient dlpServiceClient = mock(DlpServiceClient.class);
    doneMock = mock(SettableApiFuture.class);
    try (MockedStatic<DlpServiceClient> mockedStatic = Mockito.mockStatic(DlpServiceClient.class)) {
      mockedStatic.when(() -> DlpServiceClient.create()).thenReturn(dlpServiceClient);
      try (MockedStatic<SettableApiFuture> mockedStatic1 =
          Mockito.mockStatic(SettableApiFuture.class)) {
        mockedStatic1.when(() -> SettableApiFuture.create()).thenReturn(doneMock);

        CategoricalStatsHistogramBucket categoricalStatsHistogramBucket =
            CategoricalStatsHistogramBucket.newBuilder()
                .setValueFrequencyLowerBound(1)
                .setValueFrequencyUpperBound(1)
                .addBucketValues(ValueFrequency.newBuilder()
                        .setValue(Value.newBuilder().setStringValue("James").build())
                        .setCount(1)
                        .build())
                .build();
        DlpJob dlpJob =
            DlpJob.newBuilder()
                .setName("projects/project_id/locations/global/dlpJobs/job_id")
                .setState(DlpJob.JobState.DONE)
                .setRiskDetails(
                    AnalyzeDataSourceRiskDetails.newBuilder()
                        .setCategoricalStatsResult(
                            CategoricalStatsResult.newBuilder()
                                .addValueFrequencyHistogramBuckets(categoricalStatsHistogramBucket)
                                .build())
                        .build())
                .build();

        when(doneMock.get(15, TimeUnit.MINUTES)).thenReturn(true);
        when(dlpServiceClient.createDlpJob(any(CreateDlpJobRequest.class))).thenReturn(dlpJob);
        when(dlpServiceClient.getDlpJob((GetDlpJobRequest) any())).thenReturn(dlpJob);
        RiskAnalysisCategoricalStats.categoricalStatsAnalysis(
            "bigquery-public-data", "usa_names", "usa_1910_current", "topic_id", "subscription_id");
        String output = bout.toString();
        assertThat(output)
            .contains("Job name: projects/project_id/locations/global/dlpJobs/job_id");
        assertThat(output).contains("Job status: DONE");
        assertThat(output).containsMatch("Most common value occurs \\d time");
        assertThat(output).containsMatch("Least common value occurs \\d time");
        verify(dlpServiceClient, times(1)).createDlpJob(any(CreateDlpJobRequest.class));
        verify(dlpServiceClient, times(1)).getDlpJob(any(GetDlpJobRequest.class));
      }
    }
  }

  @Test
  public void testKAnonymity() throws Exception {

    DlpServiceClient dlpServiceClient = mock(DlpServiceClient.class);
    doneMock = mock(SettableApiFuture.class);
    try (MockedStatic<DlpServiceClient> mockedStatic = Mockito.mockStatic(DlpServiceClient.class)) {
      mockedStatic.when(() -> DlpServiceClient.create()).thenReturn(dlpServiceClient);
      try (MockedStatic<SettableApiFuture> mockedStatic1 =
          Mockito.mockStatic(SettableApiFuture.class)) {
        mockedStatic1.when(() -> SettableApiFuture.create()).thenReturn(doneMock);

        KAnonymityHistogramBucket anonymityHistogramBucket =
            KAnonymityHistogramBucket.newBuilder()
                .addBucketValues(KAnonymityEquivalenceClass.newBuilder()
                        .addQuasiIdsValues(Value.newBuilder().setIntegerValue(19).build())
                        .setEquivalenceClassSize(1)
                        .build())
                .build();
        DlpJob dlpJob =
            DlpJob.newBuilder()
                .setName("projects/project_id/locations/global/dlpJobs/job_id")
                .setState(DlpJob.JobState.DONE)
                .setRiskDetails(
                    AnalyzeDataSourceRiskDetails.newBuilder()
                        .setKAnonymityResult(
                            KAnonymityResult.newBuilder()
                                .addEquivalenceClassHistogramBuckets(anonymityHistogramBucket)
                                .build())
                        .build())
                .build();

        when(doneMock.get(15, TimeUnit.MINUTES)).thenReturn(true);
        when(dlpServiceClient.createDlpJob(any(CreateDlpJobRequest.class))).thenReturn(dlpJob);
        when(dlpServiceClient.getDlpJob((GetDlpJobRequest) any())).thenReturn(dlpJob);
        RiskAnalysisKAnonymity.calculateKAnonymity(
            "bigquery-public-data", "usa_names", "usa_1910_current", "topic_id", "subscription_id");
        String output = bout.toString();
        assertThat(output)
                .contains("Job name: projects/project_id/locations/global/dlpJobs/job_id");
        assertThat(output).contains("Job status: DONE");
        assertThat(output).containsMatch("Bucket size range: \\[\\d, \\d\\]");
        assertThat(output).contains("Quasi-ID values: integer_value: 19");
        assertThat(output).contains("Class size: 1");
        verify(dlpServiceClient, times(1)).createDlpJob(any(CreateDlpJobRequest.class));
        verify(dlpServiceClient, times(1)).getDlpJob(any(GetDlpJobRequest.class));
      }
    }
  }

  @Test
  public void testLDiversity() throws Exception {

    DlpServiceClient dlpServiceClient = mock(DlpServiceClient.class);
    doneMock = mock(SettableApiFuture.class);
    try (MockedStatic<DlpServiceClient> mockedStatic = Mockito.mockStatic(DlpServiceClient.class)) {
      mockedStatic
          .when(() -> DlpServiceClient.create(any(DlpServiceSettings.class)))
          .thenReturn(dlpServiceClient);
      try (MockedStatic<SettableApiFuture> mockedStatic1 =
          Mockito.mockStatic(SettableApiFuture.class)) {
        mockedStatic1.when(() -> SettableApiFuture.create()).thenReturn(doneMock);

        LDiversityHistogramBucket ldiversityHistogramBucket =
            LDiversityHistogramBucket.newBuilder()
                .setSensitiveValueFrequencyLowerBound(1)
                .setSensitiveValueFrequencyUpperBound(1)
                .addBucketValues(LDiversityEquivalenceClass.newBuilder()
                        .addQuasiIdsValues(Value.newBuilder().setIntegerValue(19).build())
                        .addTopSensitiveValues(ValueFrequency.newBuilder()
                                .setValue(Value.newBuilder().setStringValue("James").build())
                                .setCount(1)
                                .build())
                        .setEquivalenceClassSize(1))
                .build();
        DlpJob dlpJob =
            DlpJob.newBuilder()
                .setName("projects/project_id/locations/global/dlpJobs/job_id")
                .setState(DlpJob.JobState.DONE)
                .setRiskDetails(
                    AnalyzeDataSourceRiskDetails.newBuilder()
                        .setLDiversityResult(
                            LDiversityResult.newBuilder()
                                .addSensitiveValueFrequencyHistogramBuckets(
                                    ldiversityHistogramBucket)
                                .build())
                        .build())
                .build();
        when(doneMock.get(15, TimeUnit.MINUTES)).thenReturn(true);
        when(dlpServiceClient.createDlpJob(any(CreateDlpJobRequest.class))).thenReturn(dlpJob);
        when(dlpServiceClient.getDlpJob((GetDlpJobRequest) any())).thenReturn(dlpJob);
        RiskAnalysisLDiversity.calculateLDiversity(
            "bigquery-public-data", "usa_names", "usa_1910_current", "topic_id", "subscription_id");
        String output = bout.toString();
        assertThat(output)
                .contains("Job name: projects/project_id/locations/global/dlpJobs/job_id");
        assertThat(output).contains("Job status: DONE");
        assertThat(output).contains("Quasi-ID values: integer_value: 19");
        assertThat(output).contains("Class size: 1");
        assertThat(output).contains("Sensitive value string_value: \"James\"");
        verify(dlpServiceClient, times(1)).createDlpJob(any(CreateDlpJobRequest.class));
        verify(dlpServiceClient, times(1)).getDlpJob(any(GetDlpJobRequest.class));
      }
    }
  }

  @Test
  public void testKMap() throws Exception {
    DlpServiceClient dlpServiceClient = mock(DlpServiceClient.class);
    doneMock = mock(SettableApiFuture.class);
    try (MockedStatic<DlpServiceClient> mockedStatic = Mockito.mockStatic(DlpServiceClient.class)) {
      mockedStatic.when(() -> DlpServiceClient.create()).thenReturn(dlpServiceClient);
      try (MockedStatic<SettableApiFuture> mockedStatic1 =
          Mockito.mockStatic(SettableApiFuture.class)) {
        mockedStatic1.when(() -> SettableApiFuture.create()).thenReturn(doneMock);

        KMapEstimationHistogramBucket kmapEstimationHistogramBucket =
            KMapEstimationHistogramBucket.newBuilder()
                .setMaxAnonymity(1)
                .setMinAnonymity(1)
                .addBucketValues(KMapEstimationQuasiIdValues.newBuilder()
                        .addQuasiIdsValues(Value.newBuilder().setIntegerValue(27).build())
                        .addQuasiIdsValues(Value.newBuilder().setStringValue("Female").build())
                        .build())
                .build();
        DlpJob dlpJob =
            DlpJob.newBuilder()
                .setName("projects/project_id/locations/global/dlpJobs/job_id")
                .setState(DlpJob.JobState.DONE)
                .setRiskDetails(
                    AnalyzeDataSourceRiskDetails.newBuilder()
                        .setKMapEstimationResult(
                            KMapEstimationResult.newBuilder()
                                .addKMapEstimationHistogram(kmapEstimationHistogramBucket)
                                .build())
                        .build())
                .build();

        when(doneMock.get(15, TimeUnit.MINUTES)).thenReturn(true);
        when(dlpServiceClient.createDlpJob(any(CreateDlpJobRequest.class))).thenReturn(dlpJob);
        when(dlpServiceClient.getDlpJob((GetDlpJobRequest) any())).thenReturn(dlpJob);
        RiskAnalysisKMap.calculateKMap(
            "bigquery-public-data", "usa_names", "usa_1910_current", "topic_id", "subscription_id");
        String output = bout.toString();
        assertThat(output)
                .contains("Job name: projects/project_id/locations/global/dlpJobs/job_id");
        assertThat(output).contains("Job status: DONE");
        assertThat(output).containsMatch("Anonymity range: \\[\\d, \\d]");
        assertThat(output).containsMatch("Size: \\d");
        assertThat(output).containsMatch("Values: \\{\\d{2}, \"Female\"\\}");
        verify(dlpServiceClient, times(1)).createDlpJob(any(CreateDlpJobRequest.class));
        verify(dlpServiceClient, times(1)).getDlpJob(any(GetDlpJobRequest.class));
      }
    }
  }

  @Test
  public void testKAnonymityWithEntityId() throws Exception {
    DlpServiceClient dlpServiceClient = mock(DlpServiceClient.class);
    try (MockedStatic<DlpServiceClient> mockedStatic = Mockito.mockStatic(DlpServiceClient.class)) {
      mockedStatic.when(() -> DlpServiceClient.create()).thenReturn(dlpServiceClient);

      KAnonymityHistogramBucket anonymityHistogramBucket =
          KAnonymityHistogramBucket.newBuilder()
              .addBucketValues(
                  KAnonymityEquivalenceClass.newBuilder()
                      .addQuasiIdsValues(
                          Value.newBuilder().setStringValue("[\"25\",\"engineer\"]").build())
                      .setEquivalenceClassSize(1)
                      .build())
              .build();
      DlpJob dlpJob =
          DlpJob.newBuilder()
              .setName("projects/project_id/locations/global/dlpJobs/job_id")
              .setState(DlpJob.JobState.DONE)
              .setRiskDetails(
                  AnalyzeDataSourceRiskDetails.newBuilder()
                      .setKAnonymityResult(
                          KAnonymityResult.newBuilder()
                              .addEquivalenceClassHistogramBuckets(anonymityHistogramBucket)
                              .build())
                      .build())
              .build();

      when(dlpServiceClient.createDlpJob(any(CreateDlpJobRequest.class))).thenReturn(dlpJob);
      when(dlpServiceClient.getDlpJob((GetDlpJobRequest) any())).thenReturn(dlpJob);
      RiskAnalysisKAnonymityWithEntityId.calculateKAnonymityWithEntityId(
          "project_id", "dataset_id", "table_id");
      String output = bout.toString();
      assertThat(output).contains("Quasi-ID values");
      assertThat(output).contains("Class size: 1");
      assertThat(output).contains("Job status: DONE");
      assertThat(output).containsMatch("Bucket size range: \\[\\d, \\d\\]");
      assertThat(output).contains("Job name: projects/project_id/locations/global/dlpJobs/job_id");
      verify(dlpServiceClient, times(1)).createDlpJob(any(CreateDlpJobRequest.class));
      verify(dlpServiceClient, times(1)).getDlpJob(any(GetDlpJobRequest.class));
    }
  }
}
