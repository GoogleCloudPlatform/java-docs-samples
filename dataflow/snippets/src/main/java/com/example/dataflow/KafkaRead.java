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

// [START dataflow_kafka_read]
import com.google.common.collect.ImmutableMap;
import java.io.UnsupportedEncodingException;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.managed.Managed;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.TypeDescriptors;

public class KafkaRead {

  // [END dataflow_kafka_read]
  public interface Options extends StreamingOptions {
    @Description("The Kafka bootstrap server. Example: localhost:9092")
    String getBootstrapServer();

    void setBootstrapServer(String value);

    @Description("The Kafka topic to read from.")
    String getTopic();

    void setTopic(String value);

    @Description("Path to write the output file")
    String getOutputPath();

    void setOutputPath(String value);
  }

  public static PipelineResult.State main(String[] args) {
    // Parse the pipeline options passed into the application. Example:
    //   --bootstrap_servers=$BOOTSTRAP_SERVERS --topic=$KAFKA_TOPIC --outputPath=$OUTPUT_FILE
    // For more information, see https://beam.apache.org/documentation/programming-guide/#configuring-pipeline-options
    var options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
    options.setStreaming(true);

    Pipeline pipeline = createPipeline(options);
    return pipeline.run().waitUntilFinish();
  }

  // [START dataflow_kafka_read]
  public static Pipeline createPipeline(Options options) {

    // Create configuration parameters for the Managed I/O transform.
    ImmutableMap<String, Object> config = ImmutableMap.<String, Object>builder()
        .put("bootstrap_servers", options.getBootstrapServer())
        .put("topic", options.getTopic())
        .put("data_format", "RAW")
        .put("max_read_time_seconds", 15)
        .put("auto_offset_reset_config", "earliest")
        .build();

    // Build the pipeline.
    var pipeline = Pipeline.create(options);
    pipeline
        // Read messages from Kafka.
        .apply(Managed.read(Managed.KAFKA).withConfig(config)).getSinglePCollection()
        // Get the payload of each message and convert to a string.
        .apply(MapElements
            .into(TypeDescriptors.strings())
            .via((row -> {
              var bytes = row.getBytes("payload");
              try {
                return new String(bytes, "UTF-8");
              } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
              }
            })))
        // Write the payload to a text file.
        .apply(TextIO
            .write()
            .to(options.getOutputPath())
            .withSuffix(".txt")
            .withNumShards(1));
    return pipeline;
  }
}
// [END dataflow_kafka_read]
