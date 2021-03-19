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

import com.google.cloud.ServiceOptions;
import com.google.cloud.pubsublite.CloudZone;
import com.google.cloud.pubsublite.ProjectId;
import com.google.cloud.pubsublite.SequencedMessage;
import com.google.cloud.pubsublite.SubscriptionName;
import com.google.cloud.pubsublite.SubscriptionPath;
import com.google.cloud.pubsublite.beam.PubsubLiteIO;
import com.google.cloud.pubsublite.beam.SubscriberOptions;
import com.google.protobuf.ByteString;
import java.io.IOException;
import org.apache.beam.examples.common.WriteOneFilePerWindow;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.joda.time.Duration;

// [START pubsublite_to_gcs]

public class PubsubliteToGcs {
  /*
   * Define your own configuration options. Add your own arguments to be processed
   * by the command-line parser, and specify default values for them.
   */
  public interface PubsubliteToGcsOptions extends PipelineOptions, StreamingOptions {
    @Description("The Cloud Pub/Sub Lite subscription to read from.")
    @Required
    String getInputSubscription();

    void setInputSubscription(String value);

    @Description("The Cloud Pub/Sub Lite subscription's location.")
    @Required
    String getLocation();

    void setLocation(String value);

    @Description("Output file's window size in number of minutes.")
    @Default.Integer(1)
    Integer getWindowSize();

    void setWindowSize(Integer value);

    @Description("Path of the output file including its filename prefix.")
    @Required
    String getOutput();

    void setOutput(String value);
  }

  public static void main(String[] args) throws IOException {
    // The maximum number of shards when writing output.
    int numShards = 1;

    PubsubliteToGcsOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(PubsubliteToGcsOptions.class);

    options.setStreaming(true);

    SubscriberOptions subscriberOpitons= SubscriberOptions.newBuilder()
        .setSubscriptionPath(
            SubscriptionPath.newBuilder()
                .setLocation(CloudZone.parse(options.getLocation()))
                .setProject(ProjectId.of(ServiceOptions.getDefaultProjectId()))
                .setName(SubscriptionName.of(options.getInputSubscription()))
                .build()
        )
        .build();

    Pipeline pipeline = Pipeline.create(options);

    pipeline
        // 1) Read string messages from a Pub/Sub topic.
        .apply("Read From a Pub/Sub Lite Subscription", PubsubLiteIO.read(subscriberOpitons))
        // .apply("Convert messages", ParDo.of(new DoFn<SequencedMessage, ByteString>() {
        //   @ProcessElement
        //   public void processMessage(ProcessContext context) {
        //     SequencedMessage m = context.element();
        //     ByteString k = m.message().data();
        //     context.output(k);
        //   }
        // }))
        // 2) Group the messages into fixed-sized minute intervals.
        .apply(Window.into(FixedWindows.of(Duration.standardMinutes(options.getWindowSize()))))
        // 3) Write one file to GCS for every window of messages.
        .apply("Write Files to GCS", new WriteOneFilePerWindow(options.getOutput(), numShards));

    // Execute the pipeline and wait until it finishes running.
    pipeline.run().waitUntilFinish();
  }
}
// [END pubsublite_to_gcs]
