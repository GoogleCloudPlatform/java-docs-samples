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

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.common.collect.ImmutableList;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KAnonymityResult;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KAnonymityResult.KAnonymityHistogramBucket;
import com.google.privacy.dlp.v2.CreateDlpJobRequest;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.GetDlpJobRequest;
import com.google.privacy.dlp.v2.Value;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;
import java.util.Arrays;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
@RunWith(JUnit4.class)
public class RiskAnalysisTests extends TestBase {

  private static DlpServiceClient DLP_SERVICE_CLIENT;

  private UUID testRunUuid = UUID.randomUUID();
  private TopicName topicName =
      TopicName.of(PROJECT_ID, String.format("%s-%s", TOPIC_ID, testRunUuid.toString()));
  private SubscriptionName subscriptionName =
      SubscriptionName.of(
          PROJECT_ID, String.format("%s-%s", SUBSCRIPTION_ID, testRunUuid.toString()));

  @Override
  protected ImmutableList<String> requiredEnvVars() {
    return ImmutableList.of(
        "GOOGLE_APPLICATION_CREDENTIALS",
        "GOOGLE_CLOUD_PROJECT",
        "PUB_SUB_TOPIC",
        "PUB_SUB_SUBSCRIPTION",
        "BIGQUERY_DATASET",
        "BIGQUERY_TABLE");
  }

  @Before
  public void before() throws Exception {

    DLP_SERVICE_CLIENT = DlpServiceClient.create();
    // Create a new topic
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.createTopic(topicName);
    }
    // Create a new subscription
    try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
      subscriptionAdminClient.createSubscription(
          subscriptionName, topicName, PushConfig.getDefaultInstance(), 0);
    }
  }

  @After
  public void after() throws Exception {
    // Delete the test topic
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topicName);
    } catch (ApiException e) {
      System.err.println(String.format("Error deleting topic %s: %s", topicName.getTopic(), e));
      // Keep trying to clean up
    }

    // Delete the test subscription
    try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
      subscriptionAdminClient.deleteSubscription(subscriptionName);
    } catch (ApiException e) {
      System.err.println(
          String.format(
              "Error deleting subscription %s: %s", subscriptionName.getSubscription(), e));
      // Keep trying to clean up
    }
  }

  @Test
  public void testNumericalStats() throws Exception {
    RiskAnalysisNumericalStats.numericalStatsAnalysis(
        PROJECT_ID, DATASET_ID, TABLE_ID, topicName.getTopic(), subscriptionName.getSubscription());
    String output = bout.toString();
    assertThat(output).contains("Value at ");
    String jobName = Arrays.stream(output.split("\n"))
        .filter(line -> line.contains("Job name:"))
        .findFirst()
        .get();
    jobName = jobName.split(":")[1].trim();
    DLP_SERVICE_CLIENT.deleteDlpJob(jobName);
  }

  @Test
  public void testCategoricalStats() throws Exception {
    RiskAnalysisCategoricalStats.categoricalStatsAnalysis(
        PROJECT_ID, DATASET_ID, TABLE_ID, topicName.getTopic(), subscriptionName.getSubscription());

    String output = bout.toString();

    assertThat(output).containsMatch("Most common value occurs \\d time");
    assertThat(output).containsMatch("Least common value occurs \\d time");
    String jobName = Arrays.stream(output.split("\n"))
        .filter(line -> line.contains("Job name:"))
        .findFirst()
        .get();
    jobName = jobName.split(":")[1].trim();
    DLP_SERVICE_CLIENT.deleteDlpJob(jobName);
  }

  @Test
  public void testKAnonymity() throws Exception {
    RiskAnalysisKAnonymity.calculateKAnonymity(
        PROJECT_ID, DATASET_ID, TABLE_ID, topicName.getTopic(), subscriptionName.getSubscription());
    String output = bout.toString();
    assertThat(output).containsMatch("Bucket size range: \\[\\d, \\d\\]");
    assertThat(output).contains("Quasi-ID values: integer_value: 19");
    assertThat(output).contains("Class size: 1");
    String jobName = Arrays.stream(output.split("\n"))
        .filter(line -> line.contains("Job name:"))
        .findFirst()
        .get();
    jobName = jobName.split(":")[1].trim();
    DLP_SERVICE_CLIENT.deleteDlpJob(jobName);
  }

  @Test
  public void testLDiversity() throws Exception {
    RiskAnalysisLDiversity.calculateLDiversity(
        PROJECT_ID, DATASET_ID, TABLE_ID, topicName.getTopic(), subscriptionName.getSubscription());
    String output = bout.toString();
    assertThat(output).contains("Quasi-ID values: integer_value: 19");
    assertThat(output).contains("Class size: 1");
    assertThat(output).contains("Sensitive value string_value: \"James\"");
    String jobName = Arrays.stream(output.split("\n"))
        .filter(line -> line.contains("Job name:"))
        .findFirst()
        .get();
    jobName = jobName.split(":")[1].trim();
    DLP_SERVICE_CLIENT.deleteDlpJob(jobName);
  }

  @Test
  public void testKMap() throws Exception {
    RiskAnalysisKMap.calculateKMap(
        PROJECT_ID, DATASET_ID, TABLE_ID, topicName.getTopic(), subscriptionName.getSubscription());

    String output = bout.toString();

    assertThat(output).containsMatch("Anonymity range: \\[\\d, \\d]");
    assertThat(output).containsMatch("Size: \\d");
    assertThat(output).containsMatch("Values: \\{\\d{2}, \"Female\"\\}");
    String jobName = Arrays.stream(output.split("\n"))
        .filter(line -> line.contains("Job name:"))
        .findFirst()
        .get();
    jobName = jobName.split(":")[1].trim();
    DLP_SERVICE_CLIENT.deleteDlpJob(jobName);
  }

  @Test
  public void testKAnonymityWithEntityId() throws Exception {
    DlpServiceClient dlpServiceClient = mock(DlpServiceClient.class);
    try (MockedStatic<DlpServiceClient> mockedStatic = Mockito.mockStatic(DlpServiceClient.class)) {
      mockedStatic.when(() -> DlpServiceClient.create()).thenReturn(dlpServiceClient);

      KAnonymityHistogramBucket anonymityHistogramBucket =
          KAnonymityHistogramBucket.newBuilder()
              .addBucketValues(
                  KAnonymityResult.KAnonymityEquivalenceClass.newBuilder()
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
