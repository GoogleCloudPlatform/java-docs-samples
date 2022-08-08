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
import com.example.util.CustomerDemographics;
import com.example.util.TransactionDetails;
import com.google.cloud.bigtable.beam.CloudBigtableTableConfiguration;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

// Load customer demographics and history into Cloud Bigtable.
public final class GCSToCBT {

  /**
   * Hiding the constructor.
   */
  private GCSToCBT() {
  }

  /**
   * @param args the input arguments.
   */
  public static void main(final String[] args) throws
      IllegalArgumentException {
    GCStoCBTOption options =
        PipelineOptionsFactory.fromArgs(args).withValidation()
            .as(GCStoCBTOption.class);
    options.setJobName("fd-load-cbt-pipeline");

    CloudBigtableTableConfiguration config =
        new CloudBigtableTableConfiguration.Builder()
            .withProjectId(options.getProjectID())
            .withInstanceId(options.getCBTInstanceId())
            .withTableId(options.getCBTTableId())
            .build();

    // Create a pipeline that reads the GCS demographics csv file
    // and write it into CBT.
    Pipeline pDemographics = Pipeline.create(options);
    PubsubCBTHelper pubsubCBTHelper = new PubsubCBTHelper(config);
    PCollection<RowDetails> demographicsLine =
        pDemographics
            .apply("ReadGCSFile",
                TextIO.read().from(options.getDemographicsInputFile()))
            .apply(
                ParDo.of(
                    new DoFn<String, RowDetails>() {
                      @ProcessElement
                      public void processElement(
                          @Element final String row,
                          final OutputReceiver<RowDetails> out)
                          throws IllegalAccessException {
                        out.output(new CustomerDemographics(row));
                      }
                    }));
    pubsubCBTHelper.writeToCBT(demographicsLine);

    // Create a pipeline that reads the GCS history csv file and write
    // it into CBT
    Pipeline pHistory = Pipeline.create(options);
    pubsubCBTHelper = new PubsubCBTHelper(config);
    PCollection<RowDetails> historyLine =
        pHistory
            .apply("ReadGCSFile",
                TextIO.read().from(options.getHistoryInputFile()))
            .apply(
                ParDo.of(
                    new DoFn<String, RowDetails>() {
                      @ProcessElement
                      public void processElement(
                          @Element final String row,
                          final OutputReceiver<RowDetails> out)
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
