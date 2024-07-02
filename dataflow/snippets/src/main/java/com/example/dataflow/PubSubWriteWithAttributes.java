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
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;
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

  // A custom datatype for the source data.
  @DefaultCoder(AvroCoder.class)
  static class ExampleData {
    public String name;
    public String product;
    public Long timestamp; // Epoch time in milliseconds

    public ExampleData() {}

    public ExampleData(String name, String product, Long timestamp) {
      this.name = name;
      this.product = product;
      this.timestamp = timestamp;
    }
  }
  
  // Write messages to a Pub/Sub topic.
  public static void main(String[] args) {
    // Example source data.
    final List<ExampleData> messages = Arrays.asList(
        new ExampleData("Robert", "TV", 1613141590000L),
        new ExampleData("Maria", "Phone", 1612718280000L),
        new ExampleData("Juan", "Laptop", 1611618000000L),
        new ExampleData("Rebeca", "Videogame", 1610000000000L)
    );

    // Parse the pipeline options passed into the application. Example:
    //   --runner=DirectRunner --topic=projects/MY_PROJECT/topics/MY_TOPIC"
    // For more information, see https://beam.apache.org/documentation/programming-guide/#configuring-pipeline-options
    var options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    var pipeline = Pipeline.create(options);
    pipeline
        // Create some data to write to Pub/Sub.
        .apply(Create.of(messages))
        // Convert the data to Pub/Sub messages.
        .apply(MapElements
            .into(TypeDescriptor.of(PubsubMessage.class))
            .via((message -> {
              byte[] payload = message.product.getBytes(StandardCharsets.UTF_8);
              // Create attributes for each message.
              HashMap<String, String> attributes = new HashMap<String, String>();
              attributes.put("buyer", message.name);
              attributes.put("timestamp", Long.toString(message.timestamp));
              return new PubsubMessage(payload, attributes);
            })))
        // Write the messages to Pub/Sub.
        .apply(PubsubIO.writeMessages().to(options.getTopic()));
    pipeline.run().waitUntilFinish();
  }
}
// [END dataflow_pubsub_write_with_attributes]