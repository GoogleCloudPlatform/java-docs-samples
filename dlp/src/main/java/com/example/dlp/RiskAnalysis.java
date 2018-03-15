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

package com.example.dlp;

import com.google.api.core.SettableApiFuture;
import com.google.cloud.ServiceOptions;
import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.privacy.dlp.v2.Action;
import com.google.privacy.dlp.v2.Action.PublishToPubSub;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.CategoricalStatsResult.CategoricalStatsHistogramBucket;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KAnonymityResult;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KAnonymityResult.KAnonymityEquivalenceClass;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.KAnonymityResult.KAnonymityHistogramBucket;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.LDiversityResult;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.LDiversityResult.LDiversityEquivalenceClass;
import com.google.privacy.dlp.v2.AnalyzeDataSourceRiskDetails.LDiversityResult.LDiversityHistogramBucket;
import com.google.privacy.dlp.v2.BigQueryTable;
import com.google.privacy.dlp.v2.CreateDlpJobRequest;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.GetDlpJobRequest;
import com.google.privacy.dlp.v2.PrivacyMetric;
import com.google.privacy.dlp.v2.PrivacyMetric.CategoricalStatsConfig;
import com.google.privacy.dlp.v2.PrivacyMetric.KAnonymityConfig;
import com.google.privacy.dlp.v2.PrivacyMetric.LDiversityConfig;
import com.google.privacy.dlp.v2.PrivacyMetric.NumericalStatsConfig;
import com.google.privacy.dlp.v2.ProjectName;
import com.google.privacy.dlp.v2.RiskAnalysisJobConfig;
import com.google.privacy.dlp.v2.Value;
import com.google.privacy.dlp.v2.ValueFrequency;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class RiskAnalysis {

  private static void calculateNumericalStats(
      String projectId,
      String datasetId,
      String tableId,
      String columnName,
      String topicId,
      String subscriptionId)
      throws Exception {
    // [START dlp_numerical_stats]
    /**
     * Calculate numerical statistics for a column in a BigQuery table using the DLP API.
     *
     * @param projectId The Google Cloud Platform project ID to run the API call under.
     * @param datasetId The BigQuery dataset to analyze.
     * @param tableId The BigQuery table to analyze.
     * @param columnName The name of the column to analyze, which must contain only numerical data.
     * @param topicId The name of the Pub/Sub topic to notify once the job completes
     * @param subscriptionId The name of the Pub/Sub subscription to use when listening for job
     *     completion status.
     */

    // instantiate a client
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {
      BigQueryTable bigQueryTable =
          BigQueryTable.newBuilder()
              .setTableId(tableId)
              .setDatasetId(datasetId)
              .setProjectId(projectId)
              .build();

      FieldId fieldId = FieldId.newBuilder().setName(columnName).build();

      NumericalStatsConfig numericalStatsConfig =
          NumericalStatsConfig.newBuilder().setField(fieldId).build();

      PrivacyMetric privacyMetric =
          PrivacyMetric.newBuilder().setNumericalStatsConfig(numericalStatsConfig).build();

      String topicName = String.format("projects/%s/topics/%s", projectId, topicId);

      PublishToPubSub publishToPubSub = PublishToPubSub.newBuilder().setTopic(topicName).build();

      // create /action to publish job status notifications over Google Cloud Pub/Sub
      Action action = Action.newBuilder().setPubSub(publishToPubSub).build();

      RiskAnalysisJobConfig riskAnalysisJobConfig =
          RiskAnalysisJobConfig.newBuilder()
              .setSourceTable(bigQueryTable)
              .setPrivacyMetric(privacyMetric)
              .addActions(action)
              .build();

      CreateDlpJobRequest createDlpJobRequest =
          CreateDlpJobRequest.newBuilder()
              .setParent(ProjectName.of(projectId).toString())
              .setRiskJob(riskAnalysisJobConfig)
              .build();

      DlpJob dlpJob = dlpServiceClient.createDlpJob(createDlpJobRequest);
      String dlpJobName = dlpJob.getName();

      // wait on job completion
      waitOnJobCompletion(projectId, subscriptionId, dlpJobName);

      // retrieve completed job status
      DlpJob completedJob =
          dlpServiceClient.getDlpJob(GetDlpJobRequest.newBuilder().setName(dlpJobName).build());

      System.out.println("Job status: " + completedJob.getState());
      AnalyzeDataSourceRiskDetails riskDetails = completedJob.getRiskDetails();
      AnalyzeDataSourceRiskDetails.NumericalStatsResult result =
          riskDetails.getNumericalStatsResult();

      System.out.printf(
          "Value range : [%.3f, %.3f]\n",
          result.getMinValue().getFloatValue(), result.getMaxValue().getFloatValue());

      int percent = 1;
      for (Value quantileValue : result.getQuantileValuesList()) {
        System.out.printf(
            "Value at %d \\% quantile : %.3f", percent, quantileValue.getFloatValue());
      }
    }
  }

  // [START wait_on_dlp_job_completion]
  // wait on receiving a job status update over a Google Cloud Pub/Sub subscriber
  private static void waitOnJobCompletion(
      String projectId, String subscriptionId, String dlpJobName)
      throws InterruptedException, ExecutionException {
    // wait for job completion
    final SettableApiFuture<Boolean> done = SettableApiFuture.create();

    // setup a Pub/Sub subscriber to listen on the job completion status
    Subscriber subscriber =
        Subscriber.newBuilder(
            ProjectSubscriptionName.newBuilder()
                .setProject(projectId)
                .setSubscription(subscriptionId)
                .build(),
            (pubsubMessage, ackReplyConsumer) -> {
              ackReplyConsumer.ack();
              if (pubsubMessage.getAttributesCount() > 0
                  && pubsubMessage.getAttributesMap().get("DlpJobName").equals(dlpJobName)) {
                // notify job completion
                done.set(true);
              }
            })
            .build();

    // wait for job completion
    done.get();
  }
  // [END wait_on_dlp_job_completion]

  private static void calculateCategoricalStats(
      String projectId,
      String datasetId,
      String tableId,
      String columnName,
      String topicId,
      String subscriptionId)
      throws Exception {
    // [START dlp_categorical_stats]
    /**
     * Calculate categorical statistics for a column in a BigQuery table using the DLP API.
     *
     * @param projectId The Google Cloud Platform project ID to run the API call under.
     * @param datasetId The BigQuery dataset to analyze.
     * @param tableId The BigQuery table to analyze.
     * @param columnName The name of the column to analyze, which need not contain numerical data.
     */

    // instantiate a client
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {

      // projectId = process.env.GCLOUD_PROJECT;
      // datasetId = "my_dataset";
      // tableId = "my_table";
      // columnName = "firstName";

      FieldId fieldId = FieldId.newBuilder().setName(columnName).build();

      CategoricalStatsConfig categoricalStatsConfig =
          CategoricalStatsConfig.newBuilder().setField(fieldId).build();

      BigQueryTable bigQueryTable =
          BigQueryTable.newBuilder()
              .setProjectId(projectId)
              .setDatasetId(datasetId)
              .setTableId(tableId)
              .build();

      PrivacyMetric privacyMetric =
          PrivacyMetric.newBuilder().setCategoricalStatsConfig(categoricalStatsConfig).build();

      ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);

      PublishToPubSub publishToPubSub = PublishToPubSub.newBuilder()
          .setTopic(topicName.toString())
          .build();

      // create /action to publish job status notifications over Google Cloud Pub/Sub
      Action action = Action.newBuilder().setPubSub(publishToPubSub).build();

      RiskAnalysisJobConfig riskAnalysisJobConfig =
          RiskAnalysisJobConfig.newBuilder()
              .setSourceTable(bigQueryTable)
              .setPrivacyMetric(privacyMetric)
              .addActions(action)
              .build();

      CreateDlpJobRequest createDlpJobRequest =
          CreateDlpJobRequest.newBuilder()
              .setParent(ProjectName.of(projectId).toString())
              .setRiskJob(riskAnalysisJobConfig)
              .build();

      DlpJob dlpJob = dlpServiceClient.createDlpJob(createDlpJobRequest);
      String dlpJobName = dlpJob.getName();

      // wait on job completion
      waitOnJobCompletion(projectId, subscriptionId, dlpJobName);

      // retrieve completed job status
      DlpJob completedJob =
          dlpServiceClient.getDlpJob(GetDlpJobRequest.newBuilder().setName(dlpJobName).build());

      System.out.println("Job status: " + completedJob.getState());
      AnalyzeDataSourceRiskDetails riskDetails = completedJob.getRiskDetails();
      AnalyzeDataSourceRiskDetails.CategoricalStatsResult result =
          riskDetails.getCategoricalStatsResult();

      for (CategoricalStatsHistogramBucket bucket :
          result.getValueFrequencyHistogramBucketsList()) {
        System.out.println(
            "Most common value occurs " + bucket.getValueFrequencyUpperBound() + " time(s)");
        System.out.println(
            "Least common value occurs " + bucket.getValueFrequencyLowerBound() + " time(s)");
        for (ValueFrequency valueFrequency : bucket.getBucketValuesList()) {
          System.out.println(
              "Value "
                  + valueFrequency.getValue().toString()
                  + " occurs "
                  + valueFrequency.getCount()
                  + " time(s).");
        }
      }
    } catch (Exception e) {
      System.out.println("Error in categoricalStatsAnalysis: " + e.getMessage());
    }
  }
  // [END dlp_categorical_stats_analysis]

  // [START dlp_k_anonymity]
  /**
   * Calculate k-anonymity for quasi-identifiers in a BigQuery table using the DLP API.
   *
   * @param projectId The Google Cloud Platform project ID to run the API call under.
   * @param datasetId The BigQuery dataset to analyze.
   * @param tableId The BigQuery table to analyze.
   * @param quasiIds The names of columns that form a composite key ('quasi-identifiers').
   */
  private static void calculateKAnonymity(
      String projectId,
      String datasetId,
      String tableId,
      List<String> quasiIds,
      String topicId,
      String subscriptionId)
      throws Exception {
    // instantiate a client
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {

      // projectId = process.env.GCLOUD_PROJECT;
      // datasetId = 'my_dataset';
      // tableId = 'my_table';
      // quasiIds = [{ columnName: 'age' }, { columnName: 'city' }];

      List<FieldId> quasiIdFields =
          quasiIds
              .stream()
              .map(columnName -> FieldId.newBuilder().setName(columnName).build())
              .collect(Collectors.toList());

      KAnonymityConfig kanonymityConfig =
          KAnonymityConfig.newBuilder().addAllQuasiIds(quasiIdFields).build();

      BigQueryTable bigQueryTable =
          BigQueryTable.newBuilder()
              .setProjectId(projectId)
              .setDatasetId(datasetId)
              .setTableId(tableId)
              .build();

      PrivacyMetric privacyMetric =
          PrivacyMetric.newBuilder().setKAnonymityConfig(kanonymityConfig).build();

      String topicName = String.format("projects/%s/topics/%s", projectId, topicId);

      PublishToPubSub publishToPubSub = PublishToPubSub.newBuilder().setTopic(topicName).build();

      // create /action to publish job status notifications over Google Cloud Pub/Sub
      Action action = Action.newBuilder().setPubSub(publishToPubSub).build();

      RiskAnalysisJobConfig riskAnalysisJobConfig =
          RiskAnalysisJobConfig.newBuilder()
              .setSourceTable(bigQueryTable)
              .setPrivacyMetric(privacyMetric)
              .addActions(action)
              .build();

      CreateDlpJobRequest createDlpJobRequest =
          CreateDlpJobRequest.newBuilder()
              .setParent(ProjectName.of(projectId).toString())
              .setRiskJob(riskAnalysisJobConfig)
              .build();

      DlpJob dlpJob = dlpServiceClient.createDlpJob(createDlpJobRequest);
      String dlpJobName = dlpJob.getName();

      // wait on job completion
      waitOnJobCompletion(projectId, subscriptionId, dlpJobName);

      // retrieve completed job status
      DlpJob completedJob =
          dlpServiceClient.getDlpJob(GetDlpJobRequest.newBuilder().setName(dlpJobName).build());

      System.out.println("Job status: " + completedJob.getState());
      AnalyzeDataSourceRiskDetails riskDetails = completedJob.getRiskDetails();

      KAnonymityResult kAnonymityResult = riskDetails.getKAnonymityResult();
      for (KAnonymityHistogramBucket result :
          kAnonymityResult.getEquivalenceClassHistogramBucketsList()) {
        System.out.println(
            "Bucket size range: ["
                + result.getEquivalenceClassSizeLowerBound()
                + ", "
                + result.getEquivalenceClassSizeUpperBound()
                + "]");

        for (KAnonymityEquivalenceClass bucket : result.getBucketValuesList()) {
          List<String> quasiIdValues =
              bucket
                  .getQuasiIdsValuesList()
                  .stream()
                  .map(v -> v.toString())
                  .collect(Collectors.toList());

          System.out.println("\tQuasi-ID values: " + String.join(", ", quasiIdValues));
          System.out.println("\tClass size: " + bucket.getEquivalenceClassSize());
        }
      }
    } catch (Exception e) {
      System.out.println("Error in kAnonymityAnalysis: " + e.getMessage());
    }
  }
  // [END dlp_k_anonymity]

  /**
   * [START dlp_l_diversity]
   *
   * <p>Calculate l-diversity for an attribute relative to quasi-identifiers in a BigQuery table.
   *
   * @param projectId The Google Cloud Platform project ID to run the API call under.
   * @param datasetId The BigQuery dataset to analyze.
   * @param tableId The BigQuery table to analyze.
   * @param sensitiveAttribute The name of the attribute to compare the quasi-ID against
   * @param quasiIds A set of column names that form a composite key ('quasi-identifiers').
   */
  private static void calculateLDiversity(
      String projectId,
      String datasetId,
      String tableId,
      String sensitiveAttribute,
      List<String> quasiIds,
      String topicId,
      String subscriptionId)
      throws Exception {

    // instantiate a client
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {

      FieldId sensitiveAttributeField = FieldId.newBuilder().setName(sensitiveAttribute).build();

      List<FieldId> quasiIdFields =
          quasiIds
              .stream()
              .map(columnName -> FieldId.newBuilder().setName(columnName).build())
              .collect(Collectors.toList());

      LDiversityConfig ldiversityConfig =
          LDiversityConfig.newBuilder()
              .addAllQuasiIds(quasiIdFields)
              .setSensitiveAttribute(sensitiveAttributeField)
              .build();

      BigQueryTable bigQueryTable =
          BigQueryTable.newBuilder()
              .setProjectId(projectId)
              .setDatasetId(datasetId)
              .setTableId(tableId)
              .build();

      PrivacyMetric privacyMetric =
          PrivacyMetric.newBuilder().setLDiversityConfig(ldiversityConfig).build();

      String topicName = String.format("projects/%s/topics/%s", projectId, topicId);

      PublishToPubSub publishToPubSub = PublishToPubSub.newBuilder().setTopic(topicName).build();

      // create /action to publish job status notifications over Google Cloud Pub/Sub
      Action action = Action.newBuilder().setPubSub(publishToPubSub).build();

      RiskAnalysisJobConfig riskAnalysisJobConfig =
          RiskAnalysisJobConfig.newBuilder()
              .setSourceTable(bigQueryTable)
              .setPrivacyMetric(privacyMetric)
              .addActions(action)
              .build();

      CreateDlpJobRequest createDlpJobRequest =
          CreateDlpJobRequest.newBuilder()
              .setParent(ProjectName.of(projectId).toString())
              .setRiskJob(riskAnalysisJobConfig)
              .build();

      DlpJob dlpJob = dlpServiceClient.createDlpJob(createDlpJobRequest);
      String dlpJobName = dlpJob.getName();

      // wait on job completion
      waitOnJobCompletion(projectId, subscriptionId, dlpJobName);

      // retrieve completed job status
      DlpJob completedJob =
          dlpServiceClient.getDlpJob(GetDlpJobRequest.newBuilder().setName(dlpJobName).build());

      System.out.println("Job status: " + completedJob.getState());
      AnalyzeDataSourceRiskDetails riskDetails = completedJob.getRiskDetails();

      LDiversityResult lDiversityResult = riskDetails.getLDiversityResult();
      for (LDiversityHistogramBucket result :
          lDiversityResult.getSensitiveValueFrequencyHistogramBucketsList()) {
        for (LDiversityEquivalenceClass bucket : result.getBucketValuesList()) {
          List<String> quasiIdValues =
              bucket
                  .getQuasiIdsValuesList()
                  .stream()
                  .map(Value::toString)
                  .collect(Collectors.toList());

          System.out.println("\tQuasi-ID values: " + String.join(", ", quasiIdValues));
          System.out.println("\tClass size: " + bucket.getEquivalenceClassSize());

          for (ValueFrequency valueFrequency : bucket.getTopSensitiveValuesList()) {
            System.out.println(
                "\t\tSensitive value "
                    + valueFrequency.getValue().toString()
                    + " occurs "
                    + valueFrequency.getCount()
                    + " time(s).");
          }
        }
      }
    } catch (Exception e) {
      System.out.println("Error in lDiversityAnalysis: " + e.getMessage());
    }
    // [END dlp_l_diversity]
  }

  /**
   * Command line application to perform risk analysis using the Data Loss Prevention API. Supported
   * data format: BigQuery tables
   */
  public static void main(String[] args) throws Exception {

    OptionGroup optionsGroup = new OptionGroup();
    optionsGroup.setRequired(true);

    Option numericalAnalysisOption = new Option("n", "numerical");
    optionsGroup.addOption(numericalAnalysisOption);

    Option categoricalAnalysisOption = new Option("c", "categorical");
    optionsGroup.addOption(categoricalAnalysisOption);

    Option kanonymityOption = new Option("k", "kAnonymity");
    optionsGroup.addOption(kanonymityOption);

    Option ldiversityOption = new Option("l", "lDiversity");
    optionsGroup.addOption(ldiversityOption);

    Options commandLineOptions = new Options();
    commandLineOptions.addOptionGroup(optionsGroup);

    Option datasetIdOption = Option.builder("datasetId").hasArg(true).required(false).build();
    commandLineOptions.addOption(datasetIdOption);

    Option tableIdOption = Option.builder("tableId").hasArg(true).required(false).build();
    commandLineOptions.addOption(tableIdOption);

    Option projectIdOption = Option.builder("projectId").hasArg(true).required(false).build();
    commandLineOptions.addOption(projectIdOption);

    Option topicIdOption = Option.builder("topicId").hasArg(true).required(false).build();
    commandLineOptions.addOption(topicIdOption);

    Option subscriptionIdOption =
        Option.builder("subscriptionId").hasArg(true).required(false).build();
    commandLineOptions.addOption(subscriptionIdOption);

    Option columnNameOption = Option.builder("columnName").hasArg(true).required(false).build();
    commandLineOptions.addOption(columnNameOption);

    Option sensitiveAttributeOption =
        Option.builder("sensitiveAttribute").hasArg(true).required(false).build();
    commandLineOptions.addOption(sensitiveAttributeOption);

    Option quasiIdColumnNamesOption =
        Option.builder("quasiIdColumnNames").hasArg(true).required(false).build();
    commandLineOptions.addOption(quasiIdColumnNamesOption);

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;

    try {
      cmd = parser.parse(commandLineOptions, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp(RiskAnalysis.class.getName(), commandLineOptions);
      System.exit(1);
      return;
    }

    String datasetId = cmd.getOptionValue(datasetIdOption.getOpt());
    String tableId = cmd.getOptionValue(tableIdOption.getOpt());
    // use default project id when project id is not specified
    String projectId =
        cmd.getOptionValue(projectIdOption.getOpt(), ServiceOptions.getDefaultProjectId());

    String topicId = cmd.getOptionValue(topicIdOption.getOpt());
    String subscriptionId = cmd.getOptionValue(subscriptionIdOption.getOpt());

    if (cmd.hasOption("n")) {
      // numerical stats analysis
      String columnName = cmd.getOptionValue(columnNameOption.getOpt());
      calculateNumericalStats(projectId, datasetId, tableId, columnName, topicId, subscriptionId);
    } else if (cmd.hasOption("c")) {
      // categorical stats analysis
      String columnName = cmd.getOptionValue(columnNameOption.getOpt());
      calculateCategoricalStats(projectId, datasetId, tableId, columnName, topicId, subscriptionId);
    } else if (cmd.hasOption("k")) {
      // k-anonymity analysis
      List<String> quasiIdColumnNames =
          Arrays.asList(cmd.getOptionValues(quasiIdColumnNamesOption.getOpt()));
      calculateKAnonymity(
          projectId, datasetId, tableId, quasiIdColumnNames, topicId, subscriptionId);
    } else if (cmd.hasOption("l")) {
      // l-diversity analysis
      String sensitiveAttribute = cmd.getOptionValue(sensitiveAttributeOption.getOpt());
      List<String> quasiIdColumnNames =
          Arrays.asList(cmd.getOptionValues(quasiIdColumnNamesOption.getOpt()));
      calculateLDiversity(
          projectId,
          datasetId,
          tableId,
          sensitiveAttribute,
          quasiIdColumnNames,
          topicId,
          subscriptionId);
    }
  }
}
