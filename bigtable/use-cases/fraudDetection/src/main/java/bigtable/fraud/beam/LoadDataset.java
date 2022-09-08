/*
 * Copyright 2022 Google LLC
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
package bigtable.fraud.beam;

import bigtable.fraud.utils.CustomerDemographics;
import bigtable.fraud.utils.TransactionDetails;
import bigtable.fraud.utils.RowDetails;
import com.google.cloud.bigtable.beam.CloudBigtableIO;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import com.google.common.base.Preconditions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Load customer demographics and history into Cloud Bigtable.
public final class LoadDataset {

  /**
   * Hiding the constructor.
   */
  private LoadDataset() {
  }

  /**
   * a Logger object to help logging details.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(
      LoadDataset.class);

  /**
   * Convert a RowDetails into a Mutation.
   * The row key is the first member variable in the class that
   * inherits RowDetails.
   */
  private static final DoFn<RowDetails, Mutation> MUTATION_TRANSFORM =
      new DoFn<RowDetails, Mutation>() {
        @ProcessElement
        public void processElement(
            final DoFn<RowDetails, Mutation>.ProcessContext c)
            throws Exception {
          try {
            // Get the necessary data for writing to CBT.
            byte[] family = Bytes.toBytes(c.element().getColFamily());
            String[] writeHeaders = c.element().getHeaders();
            String[] values = c.element().getValues();
            byte[] rowkey = Bytes.toBytes(values[0]);

            Preconditions.checkArgument(writeHeaders.length
                == values.length);

            // Support custom timestamp if 'timestampMillisecond' is set in
            // RowDetails.
            long writeTimestamp = System.currentTimeMillis();
            if (c.element().getTimestampMillisecond() != Long.MAX_VALUE) {
              writeTimestamp = c.element().getTimestampMillisecond();
            }

            // Create a mutation.
            Put row = new Put(rowkey);
            for (int i = 1; i < values.length; i++) {
              row.addColumn(
                  family, Bytes.toBytes(writeHeaders[i]), writeTimestamp,
                  Bytes.toBytes(values[i]));
            }

            // Output the mutation
            c.output(row);
          } catch (Exception e) {
            LOGGER.error("Failed to process input {}", c.element(), e);
            throw e;
          }
        }
      };

  /**
   * @param args the input arguments.
   */
  public static void main(final String[] args) throws
      IllegalArgumentException {
    LoadDatasetOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation()
            .as(LoadDatasetOptions.class);
    options.setJobName("load-customer-demographics-" + options.getRandomUUID());

    CloudBigtableTableConfiguration config =
        new CloudBigtableTableConfiguration.Builder()
            .withProjectId(options.getProjectID())
            .withInstanceId(options.getCBTInstanceId())
            .withTableId(options.getCBTTableId())
            .build();

    // Create a pipeline that reads the GCS demographics csv file
    // and write it into CBT.
    Pipeline pDemographics = Pipeline.create(options);
    pDemographics
        .apply("ReadGCSFile",
            TextIO.read().from(options.getDemographicsInputFile()))
        .apply(
            MapElements.into(TypeDescriptor.of(RowDetails.class))
                .via(CustomerDemographics::new))
        .apply("TransformParsingsToBigtable", ParDo.of(MUTATION_TRANSFORM))
        .apply(
            "WriteToBigtable",
            CloudBigtableIO.writeToTable(config));
    PipelineResult pDemographicsRun = pDemographics.run();

    // Create a pipeline that reads the GCS history csv file and write
    // it into CBT
    options.setJobName("load-customer-historical-transactions-"
        + options.getRandomUUID());
    Pipeline pHistory = Pipeline.create(options);
    pHistory
        .apply("ReadGCSFile",
            TextIO.read().from(options.getHistoryInputFile()))
        .apply(
            MapElements.into(TypeDescriptor.of(RowDetails.class))
                .via(TransactionDetails::new))
        .apply("TransformParsingsToBigtable", ParDo.of(MUTATION_TRANSFORM))
        .apply(
            "WriteToBigtable",
            CloudBigtableIO.writeToTable(config));
    PipelineResult pHistoryRun = pHistory.run();

    pDemographicsRun.waitUntilFinish();
    pHistoryRun.waitUntilFinish();
  }
}
