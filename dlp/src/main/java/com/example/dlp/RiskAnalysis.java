/**
 * Copyright 2017 Google Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.dlp;

import com.google.api.gax.rpc.OperationFuture;
import com.google.cloud.ServiceOptions;
import com.google.cloud.dlp.v2beta1.DlpServiceClient;
import com.google.longrunning.Operation;
import com.google.privacy.dlp.v2beta1.*;
import com.google.privacy.dlp.v2beta1.PrivacyMetric.NumericalStatsConfig;
import com.google.privacy.dlp.v2beta1.PrivacyMetric.CategoricalStatsConfig;
import com.google.privacy.dlp.v2beta1.PrivacyMetric.KAnonymityConfig;
import com.google.privacy.dlp.v2beta1.PrivacyMetric.LDiversityConfig;
import com.google.privacy.dlp.v2beta1.RiskAnalysisOperationResult.NumericalStatsResult;
import com.google.privacy.dlp.v2beta1.RiskAnalysisOperationResult.LDiversityResult.LDiversityHistogramBucket;
import com.google.privacy.dlp.v2beta1.RiskAnalysisOperationResult.LDiversityResult.LDiversityEquivalenceClass;
import com.google.privacy.dlp.v2beta1.RiskAnalysisOperationResult.KAnonymityResult.KAnonymityHistogramBucket;
import com.google.privacy.dlp.v2beta1.RiskAnalysisOperationResult.KAnonymityResult.KAnonymityEquivalenceClass;
import com.google.privacy.dlp.v2beta1.RiskAnalysisOperationResult.CategoricalStatsResult.CategoricalStatsHistogramBucket;
import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RiskAnalysis {

  private static void numericalStatsAnalysis(
      String projectId, String datasetId, String tableId, String columnName)
      throws Exception {
    // [START dlp_numerical_stats_analysis]
    // instantiate a client
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {

      // (Optional) The project ID to run the API call under
      // projectId = process.env.GCLOUD_PROJECT;

      // The ID of the dataset to inspect, e.g. "my_dataset"
      // datasetId = "my_dataset";

      // The ID of the table to inspect, e.g. "my_table"
      // tableId = "my_table";

      // The name of the column to compute risk metrics for, e.g. 'firstName'
      // columnName = "firstName";

      FieldId fieldId =
          FieldId.newBuilder()
              .setColumnName(columnName)
              .build();

      NumericalStatsConfig numericalStatsConfig =
          NumericalStatsConfig.newBuilder()
              .setField(fieldId)
              .build();

      BigQueryTable bigQueryTable =
          BigQueryTable.newBuilder()
              .setProjectId(projectId)
              .setDatasetId(datasetId)
              .setTableId(tableId)
              .build();

      PrivacyMetric privacyMetric =
          PrivacyMetric.newBuilder()
              .setNumericalStatsConfig(numericalStatsConfig)
              .build();

      AnalyzeDataSourceRiskRequest request =
          AnalyzeDataSourceRiskRequest.newBuilder()
              .setPrivacyMetric(privacyMetric)
              .setSourceTable(bigQueryTable)
              .build();

      // asynchronously submit a risk analysis operation
      OperationFuture<RiskAnalysisOperationResult, RiskAnalysisOperationMetadata, Operation> responseFuture =
          dlpServiceClient.analyzeDataSourceRiskAsync(request);

      // ...
      // block on response
      RiskAnalysisOperationResult response = responseFuture.get();
      NumericalStatsResult results =
          response.getNumericalStatsResult();

      System.out.println("Value range: [" + results.getMaxValue() + ", " + results.getMinValue() + "]");

      // Print out unique quantiles
      String previousValue = "";
      for (int i = 0; i < results.getQuantileValuesCount(); i++) {
        Value valueObj = results.getQuantileValues(i);
        String value = valueObj.toString();

        if (!previousValue.equals(value)) {
          System.out.println("Value at " + i + "% quantile: " + value.toString());
          previousValue = value;
        }
      }
    } catch (Exception e) {
      System.out.println("Error in numericalStatsAnalysis: " + e.getMessage());
    }
    // [END dlp_numerical_stats_analysis]
  }

  private static void categoricalStatsAnalysis(
      String projectId, String datasetId, String tableId, String columnName)
      throws Exception {
    // [START dlp_categorical_stats_analysis]
    // instantiate a client
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {

      // (Optional) The project ID to run the API call under
      // projectId = process.env.GCLOUD_PROJECT;

      // The ID of the dataset to inspect, e.g. "my_dataset"
      // datasetId = "my_dataset";

      // The ID of the table to inspect, e.g. "my_table"
      // tableId = "my_table";

      // The name of the column to compute risk metrics for, e.g. 'firstName'
      // columnName = "firstName";

      FieldId fieldId =
          FieldId.newBuilder()
              .setColumnName(columnName)
              .build();

      CategoricalStatsConfig categoricalStatsConfig =
          CategoricalStatsConfig.newBuilder()
              .setField(fieldId)
              .build();

      BigQueryTable bigQueryTable =
          BigQueryTable.newBuilder()
              .setProjectId(projectId)
              .setDatasetId(datasetId)
              .setTableId(tableId)
              .build();

      PrivacyMetric privacyMetric =
          PrivacyMetric.newBuilder()
              .setCategoricalStatsConfig(categoricalStatsConfig)
              .build();

      AnalyzeDataSourceRiskRequest request =
          AnalyzeDataSourceRiskRequest.newBuilder()
              .setPrivacyMetric(privacyMetric)
              .setSourceTable(bigQueryTable)
              .build();

      // asynchronously submit a risk analysis operation
      OperationFuture<RiskAnalysisOperationResult, RiskAnalysisOperationMetadata, Operation> responseFuture =
          dlpServiceClient.analyzeDataSourceRiskAsync(request);

      // ...
      // block on response
      RiskAnalysisOperationResult response = responseFuture.get();
      CategoricalStatsHistogramBucket results =
          response.getCategoricalStatsResult().getValueFrequencyHistogramBuckets(0);

      System.out.println("Most common value occurs " + results.getValueFrequencyUpperBound() + " time(s)");
      System.out.println("Least common value occurs " + results.getValueFrequencyLowerBound() + " time(s)");

      for (ValueFrequency valueFrequency : results.getBucketValuesList()) {
        System.out.println("Value " +
            valueFrequency.getValue().toString() +
            " occurs " +
            valueFrequency.getCount() +
            " time(s).");
      }

    } catch (Exception e) {
      System.out.println("Error in categoricalStatsAnalysis: " + e.getMessage());
    }
    // [END dlp_categorical_stats_analysis]
  }

  private static void kAnonymityAnalysis(
      String projectId, String datasetId, String tableId, List<String> quasiIds)
      throws Exception {
    // [START dlp_k_anonymity]
    // instantiate a client
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {

      // (Optional) The project ID to run the API call under
      // projectId = process.env.GCLOUD_PROJECT;

      // The ID of the dataset to inspect, e.g. 'my_dataset'
      // datasetId = 'my_dataset';

      // The ID of the table to inspect, e.g. 'my_table'
      // tableId = 'my_table';

      // A set of columns that form a composite key ('quasi-identifiers')
      // quasiIds = [{ columnName: 'age' }, { columnName: 'city' }];

      List<FieldId> quasiIdFields =
          quasiIds
              .stream()
              .map(columnName -> FieldId.newBuilder().setColumnName(columnName).build())
              .collect(Collectors.toList());

      KAnonymityConfig kAnonymityConfig =
          KAnonymityConfig.newBuilder()
              .addAllQuasiIds(quasiIdFields)
              .build();

      BigQueryTable bigQueryTable =
          BigQueryTable.newBuilder()
              .setProjectId(projectId)
              .setDatasetId(datasetId)
              .setTableId(tableId)
              .build();

      PrivacyMetric privacyMetric =
          PrivacyMetric.newBuilder()
              .setKAnonymityConfig(kAnonymityConfig)
              .build();

      AnalyzeDataSourceRiskRequest request =
          AnalyzeDataSourceRiskRequest.newBuilder()
              .setPrivacyMetric(privacyMetric)
              .setSourceTable(bigQueryTable)
              .build();

      // asynchronously submit a risk analysis operation
      OperationFuture<RiskAnalysisOperationResult, RiskAnalysisOperationMetadata, Operation> responseFuture =
          dlpServiceClient.analyzeDataSourceRiskAsync(request);

      // ...
      // block on response
      RiskAnalysisOperationResult response = responseFuture.get();
      KAnonymityHistogramBucket results =
          response.getKAnonymityResult().getEquivalenceClassHistogramBuckets(0);

      System.out.println("Bucket size range: ["
          + results.getEquivalenceClassSizeLowerBound()
          + ", "
          + results.getEquivalenceClassSizeUpperBound()
          + "]"
        );

      for (KAnonymityEquivalenceClass bucket : results.getBucketValuesList()) {
        List<String> quasiIdValues = bucket.getQuasiIdsValuesList()
            .stream()
            .map(v -> v.toString())
            .collect(Collectors.toList());

        System.out.println("\tQuasi-ID values: " + String.join(", ", quasiIdValues));
        System.out.println("\tClass size: " + bucket.getEquivalenceClassSize());
      }
    } catch (Exception e) {
      System.out.println("Error in kAnonymityAnalysis: " + e.getMessage());
    }
    // [END dlp_k_anonymity]
  }

  private static void lDiversityAnalysis(
      String projectId, String datasetId, String tableId,  String sensitiveAttribute, List<String> quasiIds)
      throws Exception {
    // [START dlp_l_diversity]
    // instantiate a client
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {

      // (Optional) The project ID to run the API call under
      // projectId = process.env.GCLOUD_PROJECT;

      // The ID of the dataset to inspect, e.g. "my_dataset"
      // datasetId = "my_dataset";

      // The ID of the table to inspect, e.g. "my_table"
      // tableId = "my_table";

      // The column to measure l-diversity relative to, e.g. "firstName"
      // sensitiveAttribute = "name";

      // A set of columns that form a composite key ('quasi-identifiers')
      // quasiIds = [{ columnName: "age" }, { columnName: "city" }];

      FieldId sensitiveAttributeField = FieldId.newBuilder().setColumnName(sensitiveAttribute).build();

      List<FieldId> quasiIdFields =
          quasiIds
              .stream()
              .map(columnName -> FieldId.newBuilder().setColumnName(columnName).build())
              .collect(Collectors.toList());

      LDiversityConfig lDiversityConfig =
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
          PrivacyMetric.newBuilder()
              .setLDiversityConfig(lDiversityConfig)
              .build();

      AnalyzeDataSourceRiskRequest request =
          AnalyzeDataSourceRiskRequest.newBuilder()
              .setPrivacyMetric(privacyMetric)
              .setSourceTable(bigQueryTable)
              .build();

      // asynchronously submit a risk analysis operation
      OperationFuture<RiskAnalysisOperationResult, RiskAnalysisOperationMetadata, Operation> responseFuture =
          dlpServiceClient.analyzeDataSourceRiskAsync(request);

      // ...
      // block on response
      RiskAnalysisOperationResult response = responseFuture.get();
      LDiversityHistogramBucket results =
          response.getLDiversityResult().getSensitiveValueFrequencyHistogramBuckets(0);

      for (LDiversityEquivalenceClass bucket : results.getBucketValuesList()) {
        List<String> quasiIdValues = bucket.getQuasiIdsValuesList()
            .stream()
            .map(v -> v.toString())
            .collect(Collectors.toList());

        System.out.println("\tQuasi-ID values: " + String.join(", ", quasiIdValues));
        System.out.println("\tClass size: " + bucket.getEquivalenceClassSize());

        for (ValueFrequency valueFrequency : bucket.getTopSensitiveValuesList()) {
          System.out.println("\t\tSensitive value " +
              valueFrequency.getValue().toString() +
              " occurs " +
              valueFrequency.getCount() +
              " time(s).");
        }
      }
    } catch (Exception e) {
      System.out.println("Error in lDiversityAnalysis: " + e.getMessage());
    }
    // [END dlp_l_diversity]
  }


  /**
   * Command line application to perform risk analysis using the Data Loss Prevention API.
   * Supported data format: BigQuery tables
   */
  public static void main(String[] args) throws Exception {

    OptionGroup optionsGroup = new OptionGroup();
    optionsGroup.setRequired(true);

    Option numericalAnalysisOption = new Option("n", "numerical");
    optionsGroup.addOption(numericalAnalysisOption);

    Option categoricalAnalysisOption = new Option("c", "categorical");
    optionsGroup.addOption(categoricalAnalysisOption);

    Option kAnonymityOption = new Option("k", "kAnonymity");
    optionsGroup.addOption(kAnonymityOption);

    Option lDiversityOption = new Option("l", "lDiversity");
    optionsGroup.addOption(lDiversityOption);

    Options commandLineOptions = new Options();
    commandLineOptions.addOptionGroup(optionsGroup);

    Option datasetIdOption = Option.builder("datasetId").hasArg(true).required(false).build();
    commandLineOptions.addOption(datasetIdOption);

    Option tableIdOption = Option.builder("tableId").hasArg(true).required(false).build();
    commandLineOptions.addOption(tableIdOption);

    Option projectIdOption = Option.builder("projectId").hasArg(true).required(false).build();
    commandLineOptions.addOption(projectIdOption);

    Option columnNameOption = Option.builder("columnName").hasArg(true).required(false).build();
    commandLineOptions.addOption(columnNameOption);

    Option sensitiveAttributeOption = Option.builder("sensitiveAttribute").hasArg(true).required(false).build();
    commandLineOptions.addOption(sensitiveAttributeOption);

    Option quasiIdColumnNamesOption = Option.builder("quasiIdColumnNames").hasArg(true).required(false).build();
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
        cmd.getOptionValue(
            projectIdOption.getOpt(), ServiceOptions.getDefaultProjectId());

    if (cmd.hasOption("n")) {
      // numerical stats analysis
      String columnName = cmd.getOptionValue(columnNameOption.getOpt());
      numericalStatsAnalysis(projectId, datasetId, tableId, columnName);
    } else if (cmd.hasOption("c")) {
      // categorical stats analysis
      String columnName = cmd.getOptionValue(columnNameOption.getOpt());
      categoricalStatsAnalysis(projectId, datasetId, tableId, columnName);
    } else if (cmd.hasOption("k")) {
      // k-anonymity analysis
      List<String> quasiIdColumnNames = Arrays.asList(cmd.getOptionValues(quasiIdColumnNamesOption.getOpt()));
      kAnonymityAnalysis(projectId, datasetId, tableId, quasiIdColumnNames);
    } else if (cmd.hasOption("l")) {
      // l-diversity analysis
      String sensitiveAttribute = cmd.getOptionValue(sensitiveAttributeOption.getOpt());
      List<String> quasiIdColumnNames = Arrays.asList(cmd.getOptionValues(quasiIdColumnNamesOption.getOpt()));
      lDiversityAnalysis(projectId, datasetId, tableId, sensitiveAttribute, quasiIdColumnNames);
    }
  }
}
