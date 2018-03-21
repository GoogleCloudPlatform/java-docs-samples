/*
 * Copyright 2018 Google Inc.
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

package com.example.dataflow;

import com.google.cloud.spanner.Struct;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.spanner.ReadOperation;
import org.apache.beam.sdk.io.gcp.spanner.SpannerConfig;
import org.apache.beam.sdk.io.gcp.spanner.SpannerIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.transforms.ToString;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;

/**
 * This sample demonstrates how to read all data from the Cloud Spanner database.
 */
public class SpannerReadAll {

  public interface Options extends PipelineOptions {

    @Description("Spanner instance ID to query from")
    @Validation.Required
    String getInstanceId();

    void setInstanceId(String value);

    @Description("Spanner database name to query from")
    @Validation.Required
    String getDatabaseId();

    void setDatabaseId(String value);

    @Description("Output filename for records size")
    @Validation.Required
    String getOutput();

    void setOutput(String value);
  }

  public static void main(String[] args) {
    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    Pipeline p = Pipeline.create(options);

    SpannerConfig spannerConfig = SpannerConfig.create()
        .withInstanceId(options.getInstanceId())
        .withDatabaseId(options.getDatabaseId());
    // [START spanner_dataflow_readall]
    PCollection<Struct> allRecords = p.apply(SpannerIO.read()
        .withSpannerConfig(spannerConfig)
        .withQuery("SELECT t.table_name FROM information_schema.tables AS t WHERE t"
            + ".table_catalog = '' AND t.table_schema = ''")).apply(
        MapElements.into(TypeDescriptor.of(ReadOperation.class))
            .via((SerializableFunction<Struct, ReadOperation>) input -> {
              String tableName = input.getString(0);
              return ReadOperation.create().withQuery("SELECT * FROM " + tableName);
            })).apply(SpannerIO.readAll().withSpannerConfig(spannerConfig));
    // [END spanner_dataflow_readall]

    PCollection<Long> dbEstimatedSize = allRecords.apply(EstimateSize.create())
        .apply(Sum.longsGlobally());

    dbEstimatedSize.apply(ToString.elements()).apply(TextIO.write().to(options.getOutput())
        .withoutSharding());

    p.run().waitUntilFinish();
  }

}
