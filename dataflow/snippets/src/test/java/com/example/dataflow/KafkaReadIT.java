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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.beam.sdk.PipelineResult;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaReadIT {
  private static final String[] TOPIC_NAMES = {
      "topic-" + UUID.randomUUID(),
      "topic-" + UUID.randomUUID()
  };

  // The TextIO connector appends this suffix to the pipeline output file.
  private static final String OUTPUT_FILE_SUFFIX = "-00000-of-00001.txt";

  private static KafkaContainer kafka;
  private static String bootstrapServer;

  @Before
  public void setUp() throws ExecutionException, InterruptedException {
    // Start a containerized Kafka instance.
    kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));
    kafka.start();
    bootstrapServer = kafka.getBootstrapServers();

    // Create topics.
    Properties properties = new Properties();
    properties.put("bootstrap.servers", bootstrapServer);
    AdminClient adminClient = AdminClient.create(properties);
    for (String topicName : TOPIC_NAMES) {
      var topic = new NewTopic(topicName, 1, (short) 1);
      adminClient.createTopics(Arrays.asList(topic));
    }

    // Send messages to the topics.
    properties.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
    properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    KafkaProducer<Long, String> producer = new KafkaProducer<>(properties);
    for (String topicName : TOPIC_NAMES) {
      var record = new ProducerRecord<>(topicName, 0L, topicName + "-event-0");
      Future future = producer.send(record);
      future.get();
    }
  }

  @After
  public void tearDown() throws IOException {
    kafka.stop();
    for (String topicName : TOPIC_NAMES) {
      Files.deleteIfExists(Paths.get(topicName + OUTPUT_FILE_SUFFIX));
    }
  }

  @Test
  public void testApacheKafkaRead() throws IOException {
    PipelineResult.State state = KafkaRead.main(new String[] {
        "--runner=DirectRunner",
        "--bootstrapServer=" + bootstrapServer,
        "--topic=" + TOPIC_NAMES[0],
        "--outputPath=" + TOPIC_NAMES[0] // Use the topic name as the output file name.
    });
    assertEquals(PipelineResult.State.DONE, state);
    verifyOutput(TOPIC_NAMES[0]);
  }

  @Test
  public void testApacheKafkaReadTopics() throws IOException {
    PipelineResult.State state = KafkaReadTopics.main(new String[] {
        "--runner=DirectRunner",
        "--bootstrapServer=" + bootstrapServer,
        "--topic1=" + TOPIC_NAMES[0],
        "--topic2=" + TOPIC_NAMES[1]
    });
    assertEquals(PipelineResult.State.DONE, state);
    verifyOutput(TOPIC_NAMES[0]);
    verifyOutput(TOPIC_NAMES[1]);
  }

  private void verifyOutput(String topic) throws IOException {
    String output = Files.readString(Paths.get(topic + OUTPUT_FILE_SUFFIX));
    assertTrue(output.contains(topic + "-event-0"));
  }
}
