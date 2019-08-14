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
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.joda.time.Duration;

import java.io.IOException;


public class PubSubToGCS {
  // [START pubsub_to_gcs_options]
  /*
  * Define your own configuration options. Add your own arguments to be processed
  * by the command-line parser, and specify default values for them.
  */
  public interface PubSubToGCSOptions extends PipelineOptions, StreamingOptions {
    @Description("The Cloud Pub/Sub topic to read from.")
    @Required
    String getInputTopic();
    void setInputTopic(String value);

    @Description("Output file's window size in number of minutes.")
    @Default.Integer(1)
    Integer getWindowSize();
    void setWindowSize(Integer value);

    @Description("Path of the output file including its filename prefix.")
    @Required
    String getOutput();
    void setOutput(String value);
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

    pipeline
      /* 1) Read string messages from Pub/Sub. */
      .apply("Read PubSub Events", PubsubIO.readStrings().fromTopic(options.getInputTopic()))
      /* 2) Window the messages into fixed minute intervals. */
      .apply(Window.into(FixedWindows.of(Duration.standardMinutes(options.getWindowSize()))))
      /* 3) Output the windowed files to GCS with a default value of 1 for numShards. */
      .apply("Write Files to GCS", new WriteOneFilePerWindow(options.getOutput(), 1));

    // Execute the pipeline and return the result.
    PipelineResult result = pipeline.run();
    result.waitUntilFinish();
  }
  // [END pubsub_to_gcs_main]
}
