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
  private static final String TOPIC_NAME = "topic-" + UUID.randomUUID();

  private static final String OUTPUT_FILE_NAME_PREFIX = UUID.randomUUID().toString();
  private static final String OUTPUT_FILE_NAME = OUTPUT_FILE_NAME_PREFIX + "-00000-of-00001.txt";

  private static KafkaContainer kafka;
  private static String bootstrapServer;

  @Before
  public void setUp() throws ExecutionException, InterruptedException {
    // Start a containerized Kafka instance.
    kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));
    kafka.start();
    bootstrapServer = kafka.getBootstrapServers();

    // Create a topic.
    Properties properties = new Properties();
    properties.put("bootstrap.servers", bootstrapServer);
    AdminClient adminClient = AdminClient.create(properties);
    var topic = new NewTopic(TOPIC_NAME, 1, (short) 1);
    adminClient.createTopics(Arrays.asList(topic));

    // Send a message to the topic.
    properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
    ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, "key-0", "event-0");
    Future future = producer.send(record);
    future.get();
  }

  @After
  public void tearDown() throws IOException {
    kafka.stop();
    Files.deleteIfExists(Paths.get(OUTPUT_FILE_NAME));
  }

  @Test
  public void testApacheKafkaRead() throws IOException {
    PipelineResult.State state = KafkaRead.main(new String[] {
        "--runner=DirectRunner",
        "--bootstrapServer=" + bootstrapServer,
        "--topic=" + TOPIC_NAME,
        "--outputPath=" + OUTPUT_FILE_NAME_PREFIX
    });
    assertEquals(PipelineResult.State.DONE, state);

    // Verify the pipeline wrote the output.
    String output = Files.readString(Paths.get(OUTPUT_FILE_NAME));
    assertTrue(output.contains("event-0"));
  }
}
