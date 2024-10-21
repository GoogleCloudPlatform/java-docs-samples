/*
 * Copyright 2024 Google LLC
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

// [START dataflow_read_from_cloud_storage]
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.TypeDescriptors;

public class ReadFromStorage {
  // [END dataflow_read_from_cloud_storage]
  public interface Options extends PipelineOptions {
    @Description("The Cloud Storage bucket to read from")
    String getBucket();

    void setBucket(String value);
  }

  public static PipelineResult.State main(String[] args) {
    var options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    Pipeline pipeline = createPipeline(options);
    return pipeline.run().waitUntilFinish();
  }

  // [START dataflow_read_from_cloud_storage]
  public static Pipeline createPipeline(Options options) {
    var pipeline = Pipeline.create(options);
    pipeline
        // Read from a text file.
        .apply(TextIO.read().from(
            "gs://" + options.getBucket() + "/*.txt"))
        .apply(
            MapElements.into(TypeDescriptors.strings())
                .via(
                    (x -> {
                      System.out.println(x);
                      return x;
                    })));
    return pipeline;
  }
}
// [END dataflow_read_from_cloud_storage]
