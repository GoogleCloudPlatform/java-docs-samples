package DataflowPipelines;

import PubsubCBTHelper.RowDetails;
import PubsubCBTHelper.PubsubCBTHelper;
import Util.CustomerDemographics;
import Util.TransactionDetails;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

// Load customer demographics and history into Cloud Bigtable.
public class GCSToCBT {
  public static void main(String[] args) throws IllegalArgumentException {
    GCStoCBTOption options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(GCStoCBTOption.class);
    options.setJobName("fd-load-cbt-pipeline");


    CloudBigtableTableConfiguration config =
        new CloudBigtableTableConfiguration.Builder()
            .withProjectId(options.getProjectID())
            .withInstanceId(options.getCBTInstanceId())
            .withTableId(options.getCBTTableId())
            .build();

    // Create a pipeline that reads the GCS demographics csv file and write it into CBT
    Pipeline pDemographics = Pipeline.create(options);
    PubsubCBTHelper pubsubCBTHelper = new PubsubCBTHelper(config);
    PCollection<RowDetails> demographicsLine =
        pDemographics
            .apply("ReadGCSFile", TextIO.read().from(options.getDemographicsInputFile()))
            .apply(
                ParDo.of(
                    new DoFn<String, RowDetails>() {
                      @ProcessElement
                      public void processElement(
                          @Element String row, OutputReceiver<RowDetails> out)
                          throws IllegalAccessException {
                        out.output(new CustomerDemographics(row));
                      }
                    }));
    pubsubCBTHelper.writeToCBT(demographicsLine);

    // Create a pipeline that reads the GCS history csv file and write it into CBT
    Pipeline pHistory = Pipeline.create(options);
    pubsubCBTHelper = new PubsubCBTHelper(config);
    PCollection<RowDetails> historyLine =
        pHistory
            .apply("ReadGCSFile", TextIO.read().from(options.getHistoryInputFile()))
            .apply(
                ParDo.of(
                    new DoFn<String, RowDetails>() {
                      @ProcessElement
                      public void processElement(
                          @Element String row, OutputReceiver<RowDetails> out)
                          throws IllegalAccessException {
                        out.output(new TransactionDetails(row));
                      }
                    }));
    pubsubCBTHelper.writeToCBT(historyLine);

    // Wait until all the data is loaded into CBT.
    pDemographics.run().waitUntilFinish();
    pHistory.run().waitUntilFinish();
  }
}
