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

// [START dataflow_kafka_read_multi_topic]
import java.util.List;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.joda.time.Duration;
import org.joda.time.Instant;

public class KafkaReadTopics {

  // [END dataflow_kafka_read_multi_topic]
  public interface Options extends StreamingOptions {
    @Description("The Kafka bootstrap server. Example: localhost:9092")
    String getBootstrapServer();

    void setBootstrapServer(String value);

    @Description("The first Kafka topic to read from.")
    String getTopic1();

    void setTopic1(String value);

    @Description("The second Kafka topic to read from.")
    String getTopic2();

    void setTopic2(String value);
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

  // [START dataflow_kafka_read_multi_topic]
  public static Pipeline createPipeline(Options options) {
    String topic1 = options.getTopic1();
    String topic2 = options.getTopic2();

    // Build the pipeline.
    var pipeline = Pipeline.create(options);
    var allTopics = pipeline
        .apply(KafkaIO.<Long, String>read()
            .withTopics(List.of(topic1, topic2))
            .withBootstrapServers(options.getBootstrapServer())
            .withKeyDeserializer(LongDeserializer.class)
            .withValueDeserializer(StringDeserializer.class)
            .withMaxReadTime(Duration.standardSeconds(10))
            .withStartReadTime(Instant.EPOCH)
        );

    // Create separate pipeline branches for each topic.
    // The first branch filters on topic1.
    allTopics
        .apply(Filter.by(record -> record.getTopic().equals(topic1)))
        .apply(MapElements
            .into(TypeDescriptors.strings())
            .via(record -> record.getKV().getValue()))
        .apply(TextIO.write()
            .to(topic1)
            .withSuffix(".txt")
            .withNumShards(1)
        );

    // The second branch filters on topic2.
    allTopics
        .apply(Filter.by(record -> record.getTopic().equals(topic2)))
        .apply(MapElements
            .into(TypeDescriptors.strings())
            .via(record -> record.getKV().getValue()))
        .apply(TextIO.write()
            .to(topic2)
            .withSuffix(".txt")
            .withNumShards(1)
        );
    return pipeline;
  }
}
// [END dataflow_kafka_read_multi_topic]
