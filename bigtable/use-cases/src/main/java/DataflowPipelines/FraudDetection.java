package DataflowPipelines;

import PubsubCBTHelper.PubsubCBTHelper;
import PubsubCBTHelper.RowDetails;
import Util.AggregatedData;
import Util.CustomerDemographics;
import Util.TransactionDetails;
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

public class FraudDetection {

  private static final Logger LOG = LoggerFactory.getLogger(FraudDetection.class);

  // Preprocess the input and outputs a TransactionDetails object.
  static final DoFn<String, TransactionDetails> PREPROCESS_INPUT =
      new DoFn<String, TransactionDetails>() {
        @ProcessElement
        public void processElement(DoFn<String, TransactionDetails>.ProcessContext c) {
          try {
            TransactionDetails transactionDetails = new TransactionDetails(c.element());
            c.output(transactionDetails);
          } catch (Exception e) {
            LOG.error("Failed to preprocess {}", c.element(), e);
          }
        }
      };
  static final DoFn<AggregatedData, RowDetails> QUERY_ML_MODEL =
      new DoFn<AggregatedData, RowDetails>() {
        @ProcessElement
        public void processElement(DoFn<AggregatedData, RowDetails>.ProcessContext c) {
          try {
            // Get pipeline options.
            FraudDetectionOptions options = c.getPipelineOptions().as(FraudDetectionOptions.class);
            String payload = c.element().GetMLFeatures();
            String endpointID = options.getMLEndpoint();
            String projectID = options.getProjectID();

            LOG.info("Querying the ML model for these features: " + c.element().GetMLFeatures());

            // Query the ML using the endpointID.
            PredictionServiceSettings predictionServiceSettings =
                PredictionServiceSettings.newBuilder()
                    .setEndpoint(options.getMLRegion() + "-aiplatform.googleapis.com:443")
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
            PredictResponse predictResponse = predictionServiceClient.predict(predictRequest);
            double fraud_probability =
                predictResponse
                    .getPredictionsList()
                    .get(0)
                    .getListValue()
                    .getValues(0)
                    .getNumberValue();

            LOG.info("fraud_probability = " + fraud_probability);

            // Set the field isFraud to true if the fraud_probability was >= 0.1
            // This is a configurable number that should be tuned depending on the ML model.
            double FRAUD_PROBABILITY_THRESHOLD = 0.1;
            if (fraud_probability >= FRAUD_PROBABILITY_THRESHOLD) {
              c.element().transactionDetails.isFraud = "1";
            } else {
              c.element().transactionDetails.isFraud = "0";
            }

            c.output(c.element().transactionDetails);
          } catch (Exception e) {
            LOG.error("Failed to preprocess {}", c.element(), e);
          }
        }
      };

  public static void main(String[] args) {
    FraudDetectionOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(FraudDetectionOptions.class);
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
                "Read PubSub Messages", PubsubIO.readStrings().fromTopic(options.getInputTopic()))
            .apply("Preprocess Input", ParDo.of(PREPROCESS_INPUT))
            .apply("Read from Cloud Bigtable", ParDo.of(new ReadFromTableFn(config)))
            .apply("Query ML Model", ParDo.of(QUERY_ML_MODEL));

    PubsubCBTHelper pubsubCBTHelper = new PubsubCBTHelper(config);

    pubsubCBTHelper.writeToPubsub(modelOutput, options.getOutputTopic());
    pubsubCBTHelper.writeToCBT(modelOutput);
    pipeline.run();
  }

  // Read the transaction history for that customer, and outputs an AggregatedData object.
  public static class ReadFromTableFn
      extends AbstractCloudBigtableTableDoFn<TransactionDetails, AggregatedData> {

    public ReadFromTableFn(CloudBigtableConfiguration config) {
      super(config);
    }

    @ProcessElement
    public void processElement(DoFn<TransactionDetails, AggregatedData>.ProcessContext c)
        throws IOException, IllegalAccessException {
      try {
        FraudDetectionOptions options = c.getPipelineOptions().as(FraudDetectionOptions.class);
        TransactionDetails transactionDetails = c.element();
        LOG.info("Reading CBT for customerID = " + transactionDetails.userID);

        // Read the cells for that customer ID.
        Scan scan =
            new Scan()
                .withStartRow(Bytes.toBytes(transactionDetails.userID))
                .setOneRowLimit()
                .setMaxVersions();
        Table table = getConnection().getTable(TableName.valueOf(options.getCBTTableId()));
        ResultScanner data = table.getScanner(scan);
        Result row = data.next();
        Preconditions.checkArgument(new String(row.getRow()).equals(transactionDetails.userID));

        CustomerDemographics customerDemographics = new CustomerDemographics(row);

        // Generate an AggregatedData object.
        AggregatedData aggregatedData =
            new AggregatedData(customerDemographics, transactionDetails, row);

        c.output(aggregatedData);
      } catch (Exception e) {
        LOG.error("Failed to preprocess {}", c.element(), e);
        throw e;
      }
    }
  }
}
