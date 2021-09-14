// Copyright 2021 Google Inc.
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

package examples;

// [START pubsublite_to_gcs]

import com.google.cloud.pubsublite.SubscriptionPath;
import com.google.cloud.pubsublite.proto.SequencedMessage;
import org.apache.beam.examples.common.WriteOneFilePerWindow;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsublite.PubsubLiteIO;
import org.apache.beam.sdk.io.gcp.pubsublite.SubscriberOptions;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Repeatedly;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PubsubliteToGcs {
  /*
   * Define your own configuration options. Add your arguments to be processed
   * by the command-line parser.
   */
  public interface PubsubliteToGcsOptions extends PipelineOptions, StreamingOptions {
    @Description("Your Pub/Sub Lite subscription.")
    @Required
    String getSubscription();

    void setSubscription(String value);

    @Description("Window size of output files in minutes.")
    @Default.Integer(1)
    Integer getWindowSize();

    void setWindowSize(Integer value);

    @Description("Filename prefix of output files.")
    @Required
    String getOutput();

    void setOutput(String value);
  }

  private static final Logger LOG = LoggerFactory.getLogger(PubsubliteToGcs.class);

  public static void main(String[] args) throws InterruptedException {
    // The maximum number of shards when writing output files.
    int numShards = 1;

    PubsubliteToGcsOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(PubsubliteToGcsOptions.class);

    options.setStreaming(true);

    SubscriberOptions subscriberOptions =
        SubscriberOptions.newBuilder()
            .setSubscriptionPath(SubscriptionPath.parse(options.getSubscription()))
            .build();

    Pipeline pipeline = Pipeline.create(options);
    pipeline
        .apply("Read From Pub/Sub Lite", PubsubLiteIO.read(subscriberOptions))
        .apply(
            "Convert messages",
            MapElements.into(TypeDescriptors.strings())
                .via(
                    (SequencedMessage sequencedMessage) -> {
                      String data = sequencedMessage.getMessage().getData().toStringUtf8();
                      LOG.info("Received: " + data);
                      long publishTime = sequencedMessage.getPublishTime().getSeconds();
                      return data + "\t" + publishTime;
                    }))
        .apply(
            "Apply windowing function",
            Window
                // Group the elements using fixed-sized time intervals based on the element
                // timestamp. The element timestamp is the publish time associated with a message.
                .<String>into(FixedWindows.of(Duration.standardMinutes(options.getWindowSize())))
                // Fire a trigger every 30 seconds after receiving the first element.
                .triggering(
                    Repeatedly.forever(
                        AfterProcessingTime.pastFirstElementInPane()
                            .plusDelayOf(Duration.standardSeconds(30))))
                // Ignore late elements.
                .withAllowedLateness(Duration.ZERO)
                // Accumulate elements in fired panes. This will make sure that elements collected
                // in an earlier pane by an earlier trigger will not be overwritten by those
                // arriving later due to a later trigger fired in the same window.
                .accumulatingFiredPanes())
        .apply("Write elements to GCS", new WriteOneFilePerWindow(options.getOutput(), numShards));

    // Execute the pipeline. You may add `.waitUntilFinish()` to observe logs in your console, but
    // `waitUntilFinish()` will not work in Dataflow Flex Templates.
    pipeline.run();
  }
}
// [END pubsublite_to_gcs]
