// Copyright 2019 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.examples.pubsub.streaming;

import org.apache.beam.examples.common.WriteOneFilePerWindow;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.FileBasedSink;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.fs.ResourceId;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.options.ValueProvider;
import org.apache.beam.sdk.options.ValueProvider.NestedValueProvider;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.joda.time.Duration;

import java.io.IOException;


public class PubSubToGCS {
  // [START pubsub_to_gcs_options]
  public interface PubSubToGCSOptions extends PipelineOptions, StreamingOptions {
    @Description("The Cloud Pub/Sub topic to read from.")
    @Required
    ValueProvider<String> getInputTopic();
    void setInputTopic(ValueProvider<String> value);

    @Description("The directory to output files to.")
    @Required
    ValueProvider<String> getOutputDirectory();
    void setOutputDirectory(ValueProvider<String> value);

    @Description("The filename prefix of the files to write to.")
    @Default.String("output")
    @Required
    ValueProvider<String> getOutputFilenamePrefix();
    void setOutputFilenamePrefix(ValueProvider<String> value);

    @Description("The suffix of the files to write.")
    @Default.String("")
    ValueProvider<String> getOutputFilenameSuffix();
    void setOutputFilenameSuffix(ValueProvider<String> value);

    @Description("The shard template of the output file. Specified as repeating sequences "
      + "of the letters 'S' or 'N' (example: SSS-NNN). These are replaced with the "
      + "shard number, or number of shards respectively")
    @Default.String("W-P-SS-of-NN")
    ValueProvider<String> getOutputShardTemplate();
    void setOutputShardTemplate(ValueProvider<String> value);

    @Description("The maximum number of output shards produced when writing.")
    @Default.Integer(1)
    Integer getNumShards();
    void setNumShards(Integer value);
  }
  // [END pubsub_to_gcs_options]

  // [START pubsub_to_gcs_main]
  public static void main(String[] args) throws IOException {

    PubSubToGCSOptions options = PipelineOptionsFactory
      .fromArgs(args)
      .withValidation()
      .as(PubSubToGCSOptions.class);

    options.setStreaming(true);

    Pipeline pipeline = Pipeline.create(options);

    /*
     * Steps:
     *   1) Read string messages from PubSub
     *   2) Window the messages into minute intervals
     *   3) Output the windowed files to GCS
     */
    pipeline
      .apply("Read PubSub Events", PubsubIO.readStrings().fromTopic(options.getInputTopic()))
      .apply(Window.into(FixedWindows.of(Duration.standardMinutes(1))))
      // Apply windowed file writes. Use a NestedValueProvider because the filename
      // policy requires a resourceId generated from the input value at runtime.
      .apply(
        "Write File(s)",
        TextIO.write()
          .withWindowedWrites()
          .withNumShards(options.getNumShards())
          .to(
            new WindowedFilenamePolicy(
              options.getOutputDirectory(),
              options.getOutputFilenamePrefix(),
              options.getOutputShardTemplate(),
              options.getOutputFilenameSuffix()))
          .withTempDirectory(NestedValueProvider.of(
            options.getOutputDirectory(),
            (SerializableFunction<String, ResourceId>) input ->
              FileBasedSink.convertToFileResourceIfPossible(input))));

    // Execute the pipeline and return the result.
    PipelineResult result = pipeline.run();
    try {
      result.waitUntilFinish();
    } catch (Exception exc) {
      result.cancel();
    }
  }
  // [END pubsub_to_gcs_main]
}
