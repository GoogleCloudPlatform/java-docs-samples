/*
 * Copyright 2023 Google LLC
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

// [START dataflow_pubsub_write_with_attributes]
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.TypeDescriptor;

public class PubSubWriteWithAttributes {
  public interface Options extends PipelineOptions {
    @Description("The Pub/Sub topic to write to. Format: projects/<PROJECT>/topics/<TOPIC>")
    String getTopic();

    void setTopic(String value);
  }

  // Write messages to a Pub/Sub topic.
  public static void main(String[] args) {
    final List<String> messages = Arrays.asList("message1", "message2", "message3", "message4");

    var options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    var pipeline = Pipeline.create(options);
    pipeline
        // Create some data to write to Pub/Sub.
        .apply(Create.of(messages))
        // Convert the data to Pub/Sub messages.
        .apply(MapElements
            .into(TypeDescriptor.of(PubsubMessage.class))
            .via((payload -> {
              // Create attributes for each message.
              HashMap<String, String> attributes = new HashMap<String, String>();
              attributes.put("key1", payload + " value");
              attributes.put("key2", payload + " value2");
              return new PubsubMessage(payload.getBytes(StandardCharsets.UTF_8), attributes);
            })))
        // Write the messages to Pub/Sub.
        .apply(PubsubIO.writeMessages().to(options.getTopic()));
    pipeline.run().waitUntilFinish();
  }
}
// [END dataflow_pubsub_write_with_attributes]