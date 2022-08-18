/*
 * Copyright 2020 Google LLC
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
package com.example.dataflowpipelines;

import com.example.pubsubcbt.PubsubCBTHelper;
import com.example.pubsubcbt.RowDetails;
import com.example.util.AggregatedData;
import com.example.util.CustomerDemographics;
import com.example.util.TransactionDetails;
import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictRequest;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.cloud.bigtable.beam.AbstractCloudBigtableTableDoFn;
import com.google.cloud.bigtable.beam.CloudBigtableConfiguration;
import com.google.cloud.bigtable.beam.CloudBigtableScanConfiguration;
import com.google.common.base.Preconditions;
import com.google.protobuf.ListValue;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.util.List;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FraudDetection {

  /**
   * a Logger object to help logging details.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(
      FraudDetection.class);
  /**
   * Convert the line read from Cloud Pubsub into a TransactionDetails object.
   */
  static final DoFn<String, TransactionDetails> PREPROCESS_INPUT =
      new DoFn<String, TransactionDetails>() {
        @ProcessElement
        public void processElement(
            final DoFn<String, TransactionDetails>.ProcessContext c) {
          try {
            TransactionDetails transactionDetails = new TransactionDetails(
                c.element());
            c.output(transactionDetails);
          } catch (Exception e) {
            LOGGER.error("Failed to preprocess {}", c.element(), e);
          }
        }
      };
  /**
   * Set the field isFraud to true if the fraud_probability was >= 0.1 This is a
   * configurable number that should be tuned depending on the ML model.
   */
  private static final double FRAUD_PROBABILITY_THRESHOLD = 0.1d;
  /**
   * Query the ML model.
   */
  static final DoFn<AggregatedData, RowDetails> QUERY_ML_MODEL =
      new DoFn<AggregatedData, RowDetails>() {
        @ProcessElement
        public void processElement(
            final DoFn<AggregatedData, RowDetails>.ProcessContext c) {
          try {
            // Get pipeline options.
            FraudDetectionOptions options = c.getPipelineOptions()
                .as(FraudDetectionOptions.class);
            String payload = c.element().getMLFeatures();
            String endpointID = options.getMLEndpoint();
            String projectID = options.getProjectID();

            LOGGER.info(
                "Querying the ML model for these features: " + c.element()
                    .getMLFeatures());

            // Query the ML using the endpointID.
            PredictionServiceSettings predictionServiceSettings =
                PredictionServiceSettings.newBuilder()
                    .setEndpoint(options.getMLRegion()
                        + "-aiplatform.googleapis.com:443")
                    .build();
            PredictionServiceClient predictionServiceClient =
                PredictionServiceClient.create(predictionServiceSettings);

            EndpointName endpointName =
                EndpointName.of(projectID, options.getMLRegion(), endpointID);

            ListValue.Builder listValue = ListValue.newBuilder();
            JsonFormat.parser().merge(payload, listValue);
            List<Value> instanceList = listValue.getValuesList();

            // Send a predection request and receive a response.
            PredictRequest predictRequest =
                PredictRequest.newBuilder()
                    .setEndpoint(endpointName.toString())
                    .addAllInstances(instanceList)
                    .build();
            PredictResponse predictResponse = predictionServiceClient.predict(
                predictRequest);
            double fraudProbability =
                predictResponse
                    .getPredictionsList()
                    .get(0)
                    .getListValue()
                    .getValues(0)
                    .getNumberValue();

            LOGGER.info("fraudProbability = " + fraudProbability);

            if (fraudProbability >= FRAUD_PROBABILITY_THRESHOLD) {
              c.element().getTransactionDetails().setIsFraud("1");
            } else {
              c.element().getTransactionDetails().setIsFraud("0");
            }

            c.output(c.element().getTransactionDetails());
          } catch (Exception e) {
            LOGGER.error("Failed to preprocess {}", c.element(), e);
          }
        }
      };

  /**
   * Hiding the constructor.
   */
  private FraudDetection() {
  }

  /**
   * @param args the input arguments.
   */
  public static void main(final String[] args) {
    FraudDetectionOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation()
            .as(FraudDetectionOptions.class);
    options.setStreaming(true);
    // options.setJobName("fd-streaming-pipeline-" + options.getRandomID());
    options.setJobName("fd-streaming-pipeline");

    CloudBigtableScanConfiguration config =
        new CloudBigtableScanConfiguration.Builder()
            .withProjectId(options.getProjectID())
            .withInstanceId(options.getCBTInstanceId())
            .withTableId(options.getCBTTableId())
            .build();

    // Create a fraud-detection Dataflow pipeline.
    Pipeline pipeline = Pipeline.create(options);

    PCollection<RowDetails> modelOutput =
        pipeline
            .apply(
                "Read PubSub Messages",
                PubsubIO.readStrings().fromTopic(options.getInputTopic()))
            .apply("Preprocess Input", ParDo.of(PREPROCESS_INPUT))
            .apply("Read from Cloud Bigtable",
                ParDo.of(new ReadFromTableFn(config)))
            .apply("Query ML Model", ParDo.of(QUERY_ML_MODEL));

    PubsubCBTHelper pubsubCBTHelper = new PubsubCBTHelper(config);

    pubsubCBTHelper.writeToPubsub(modelOutput, options.getOutputTopic());
    pubsubCBTHelper.writeToCBT(modelOutput);
    pipeline.run();
  }

  // Read the transaction history for that customer, and outputs an
  // AggregatedData object.
  public static class ReadFromTableFn
      extends
      AbstractCloudBigtableTableDoFn<TransactionDetails, AggregatedData> {

    /**
     * @param config the CloudBigtableConfiguration used in reading from
     * Cloud Bigtable.
     */
    public ReadFromTableFn(final CloudBigtableConfiguration config) {
      super(config);
    }

    /**
     * @param c the process context that converts a TransactionDetails into an
     * AggregatedData object.
     */
    @ProcessElement
    public void processElement(
        final DoFn<TransactionDetails, AggregatedData>.ProcessContext c)
        throws IOException, IllegalAccessException {
      try {
        FraudDetectionOptions options = c.getPipelineOptions()
            .as(FraudDetectionOptions.class);
        TransactionDetails transactionDetails = c.element();
        LOGGER.info("Reading CBT for customerID = "
            + transactionDetails.getCustomerID());

        // Read the cells for that customer ID.
        Scan scan =
            new Scan()
                .withStartRow(Bytes.toBytes(transactionDetails.getCustomerID()))
                .setOneRowLimit()
                .setMaxVersions();
        Table table = getConnection().getTable(
            TableName.valueOf(options.getCBTTableId()));
        ResultScanner data = table.getScanner(scan);
        Result row = data.next();
        Preconditions.checkArgument(new String(row.getRow()).equals(
            transactionDetails.getCustomerID()));

        CustomerDemographics customerDemographics = new CustomerDemographics(
            row);

        // Generate an AggregatedData object.
        AggregatedData aggregatedData =
            new AggregatedData(customerDemographics, transactionDetails, row);

        c.output(aggregatedData);
      } catch (Exception e) {
        LOGGER.error("Failed to preprocess {}", c.element(), e);
        throw e;
      }
    }
  }
}
