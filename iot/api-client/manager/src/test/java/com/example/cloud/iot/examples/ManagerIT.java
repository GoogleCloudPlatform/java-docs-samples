/*
 * Copyright 2017 Google Inc.
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

package com.example.cloud.iot.examples;

import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.Topic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


/** Tests for iot "Management" sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class ManagerIT {
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private DeviceRegistryExample app;

  private static final String CLOUD_REGION = "us-central1";
  private static final String ES_PATH = "resources/ec_public.pem";
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGISTRY_ID = "java-reg-" + (System.currentTimeMillis() / 1000L);
  private static final String RSA_PATH = "resources/rsa_cert.pem";
  private static final String PKCS_PATH = "resources/rsa_private_pkcs8";
  private static final String TOPIC_ID = "java-pst-" + (System.currentTimeMillis() / 1000L);

  private static Topic topic;

  @Before
  public void setUp() throws Exception {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() throws Exception {
    System.setOut(null);
  }

  @Test
  public void testPatchRsa() throws Exception {
    final String deviceName = "patchme-device-rsa";
    topic = DeviceRegistryExample.createIotTopic(
            PROJECT_ID,
            TOPIC_ID);
    try {
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.createDeviceWithNoAuth(deviceName, PROJECT_ID, CLOUD_REGION,
          REGISTRY_ID);
      DeviceRegistryExample.patchRsa256ForAuth(deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION,
          REGISTRY_ID);

      String got = bout.toString();
      Assert.assertTrue(got.contains("Created device: {"));
    } finally {
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  @Test
  public void testPatchEs() throws Exception {
    final String deviceName = "patchme-device-es";
    topic = DeviceRegistryExample.createIotTopic(
            PROJECT_ID,
            TOPIC_ID);

    try {
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.createDeviceWithNoAuth(deviceName, PROJECT_ID, CLOUD_REGION,
          REGISTRY_ID);
      DeviceRegistryExample.patchEs256ForAuth(deviceName, ES_PATH, PROJECT_ID, CLOUD_REGION,
          REGISTRY_ID);

      String got = bout.toString();
      Assert.assertTrue(got.contains("Created device: {"));
    } finally {
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  @Test
  public void testCreateDeleteUnauthDevice() throws Exception {
    final String deviceName = "noauth-device";
    topic = DeviceRegistryExample.createIotTopic(
        PROJECT_ID,
        TOPIC_ID);

    DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceRegistryExample.createDeviceWithNoAuth(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

    String got = bout.toString();
    Assert.assertTrue(got.contains("Created device: {"));

    DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  @Test
  public void testCreateDeleteEsDevice() throws Exception {
    final String deviceName = "es-device";
    topic = DeviceRegistryExample.createIotTopic(
        PROJECT_ID,
        TOPIC_ID);
    DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceRegistryExample.createDeviceWithEs256(deviceName, ES_PATH, PROJECT_ID, CLOUD_REGION,
        REGISTRY_ID);
    DeviceRegistryExample.getDeviceStates(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

    String got = bout.toString();
    Assert.assertTrue(got.contains("Created device: {"));

    DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  @Test
  public void testCreateDeleteRsaDevice() throws Exception {
    final String deviceName = "rsa-device";
    topic = DeviceRegistryExample.createIotTopic(
        PROJECT_ID,
        TOPIC_ID);
    DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceRegistryExample.createDeviceWithRs256(
        deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.getDeviceStates(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

    String got = bout.toString();
    Assert.assertTrue(got.contains("Created device: {"));

    DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  @Test
  public void testCreateGetDevice() throws Exception {
    final String deviceName = "rsa-device";
    topic = DeviceRegistryExample.createIotTopic(
        PROJECT_ID,
        TOPIC_ID);
    DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceRegistryExample.createDeviceWithRs256(
        deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.getDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

    String got = bout.toString();
    Assert.assertTrue(got.contains("Created device: {"));
    Assert.assertTrue(got.contains("Retrieving device"));

    DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  @Test
  public void testCreateConfigureDevice() throws Exception {
    final String deviceName = "rsa-device-config";
    topic = DeviceRegistryExample.createIotTopic(
        PROJECT_ID,
        TOPIC_ID);
    DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceRegistryExample.createDeviceWithRs256(
        deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.setDeviceConfiguration(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID,
        "some-test-data", 0L);

    String got = bout.toString();
    Assert.assertTrue(got.contains("Updated: 2"));

    DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }


  @Test
  public void testCreateListDevices() throws Exception {
    final String deviceName = "rsa-device";
    topic = DeviceRegistryExample.createIotTopic(
        PROJECT_ID,
        TOPIC_ID);
    DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceRegistryExample.createDeviceWithRs256(
        deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.listDevices(PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

    String got = bout.toString();
    Assert.assertTrue(got.contains("Created device: {"));
    Assert.assertTrue(got.contains("Found"));

    DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  @Test
  public void testCreateGetRegistry() throws Exception {

    topic = DeviceRegistryExample.createIotTopic(
        PROJECT_ID,
        TOPIC_ID);
    DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceRegistryExample.getRegistry(PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

    String got = bout.toString();
    Assert.assertFalse(got.contains("eventNotificationConfigs"));

    DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  // HTTP device tests

  @Test
  public void testHttpDeviceEvent() throws Exception {
    final String deviceName = "rsa-device-http-event";
    topic = DeviceRegistryExample.createIotTopic(
        PROJECT_ID,
        TOPIC_ID);
    DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceRegistryExample.createDeviceWithRs256(
        deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.listDevices(PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

    // Device bootstrapped, time to connect and run.
    String[] testArgs = {
      "-project_id=" + PROJECT_ID,
      "-registry_id=" + REGISTRY_ID,
      "-device_id=" + deviceName,
      "-private_key_file=" + PKCS_PATH,
      "-num_messages=1",
      "-message_type=event",
      "-algorithm=RS256"
    };
    com.example.cloud.iot.examples.HttpExample.main(testArgs);
    // End device test.

    // Assertions
    String got = bout.toString();
    Assert.assertTrue(got.contains("200"));
    Assert.assertTrue(got.contains("OK"));

    // Clean up
    DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  @Test
  public void testHttpDeviceState() throws Exception {
    final String deviceName = "rsa-device-http-state";
    topic = DeviceRegistryExample.createIotTopic(
        PROJECT_ID,
        TOPIC_ID);
    DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceRegistryExample.createDeviceWithRs256(
        deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.listDevices(PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

    // Device bootstrapped, time to connect and run.
    String[] testArgs = {
      "-project_id=" + PROJECT_ID,
      "-registry_id=" + REGISTRY_ID,
      "-device_id=" + deviceName,
      "-private_key_file=" + PKCS_PATH,
      "-num_messages=1",
      "-message_type=state",
      "-algorithm=RS256"
    };
    com.example.cloud.iot.examples.HttpExample.main(testArgs);
    // End device test.

    // Assertions
    String got = bout.toString();
    Assert.assertTrue(got.contains("200"));
    Assert.assertTrue(got.contains("OK"));

    // Clean up
    DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  @Test
  public void testHttpDeviceConfig() throws Exception {
    final String deviceName = "rsa-device-http-state";
    topic = DeviceRegistryExample.createIotTopic(
        PROJECT_ID,
        TOPIC_ID);
    DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceRegistryExample.createDeviceWithRs256(
        deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.listDevices(PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

    // Device bootstrapped, time to connect and run.
    String[] testArgs = {
      "-project_id=" + PROJECT_ID,
      "-registry_id=" + REGISTRY_ID,
      "-device_id=" + deviceName,
      "-private_key_file=" + PKCS_PATH,
      "-num_messages=1",
      "-message_type=event",
      "-algorithm=RS256"
    };
    com.example.cloud.iot.examples.HttpExample.main(testArgs);
    // End device test.

    // Assertions
    String got = bout.toString();
    Assert.assertTrue(got.contains("200"));
    Assert.assertTrue(got.contains("OK"));
    Assert.assertTrue(got.contains("\"binaryData\": \"\""));

    // Clean up
    DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }


  // MQTT device tests
  @Test
  public void testMqttDeviceConfig() throws Exception {
    final String deviceName = "rsa-device-mqtt-config";
    topic = DeviceRegistryExample.createIotTopic(
        PROJECT_ID,
        TOPIC_ID);
    DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceRegistryExample.createDeviceWithRs256(
        deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.listDevices(PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

    // Device bootstrapped, time to connect and run.
    String[] testArgs = {
        "-project_id=" + PROJECT_ID,
        "-registry_id=" + REGISTRY_ID,
        "-device_id=" + deviceName,
        "-private_key_file=" + PKCS_PATH,
        "-message_type=events",
        "-num_messages=1",
        "-algorithm=RS256"
    };
    com.example.cloud.iot.examples.MqttExample.main(testArgs);
    // End device test.

    // Assertions
    String got = bout.toString();
    System.out.println(got);
    Assert.assertTrue(got.contains("Payload :"));

    // Clean up
    DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  @Test
  public void testMqttDeviceEvents() throws Exception {
    final String deviceName = "rsa-device-mqtt-events";
    topic = DeviceRegistryExample.createIotTopic(
        PROJECT_ID,
        TOPIC_ID);
    DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceRegistryExample.createDeviceWithRs256(
        deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.listDevices(PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

    // Device bootstrapped, time to connect and run.
    String[] testArgs = {
        "-project_id=" + PROJECT_ID,
        "-registry_id=" + REGISTRY_ID,
        "-device_id=" + deviceName,
        "-private_key_file=" + PKCS_PATH,
        "-message_type=events",
        "-num_messages=1",
        "-algorithm=RS256"
    };
    com.example.cloud.iot.examples.MqttExample.main(testArgs);
    // End device test.

    // Assertions
    String got = bout.toString();
    //
    //Finished loop successfully. Goodbye!

    Assert.assertTrue(got.contains("Publishing events message 1"));
    Assert.assertTrue(got.contains("Finished loop successfully. Goodbye!"));

    // Clean up
    DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  @Test
  public void testMqttDeviceState() throws Exception {
    final String deviceName = "rsa-device-mqtt-state";
    topic = DeviceRegistryExample.createIotTopic(
        PROJECT_ID,
        TOPIC_ID);
    DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceRegistryExample.createDeviceWithRs256(
        deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.listDevices(PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

    // Device bootstrapped, time to connect and run.
    String[] testArgs = {
        "-project_id=" + PROJECT_ID,
        "-registry_id=" + REGISTRY_ID,
        "-device_id=" + deviceName,
        "-private_key_file=" + PKCS_PATH,
        "-message_type=state",
        "-algorithm=RS256"
    };
    com.example.cloud.iot.examples.MqttExample.main(testArgs);
    // End device test.

    // Assertions
    String got = bout.toString();
    Assert.assertTrue(got.contains("Publishing state message 1"));
    Assert.assertTrue(got.contains("Finished loop successfully. Goodbye!"));

    // Clean up
    DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }
}
