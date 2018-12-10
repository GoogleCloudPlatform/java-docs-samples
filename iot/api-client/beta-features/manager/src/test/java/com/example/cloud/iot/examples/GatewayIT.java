/*
 * Copyright 2018 Google Inc.
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

import com.google.api.services.cloudiot.v1.model.Device;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.Topic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.paho.client.mqttv3.MqttClient;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for iot "Management" sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class GatewayIT {
  private ByteArrayOutputStream bout;
  private PrintStream out;

  private static final String CLOUD_REGION = "us-central1";
  private static final String ES_PATH = "resources/ec_public.pem";
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGISTRY_ID = "java-reg-" + (System.currentTimeMillis() / 1000L);
  private static final String RSA_PATH = "resources/rsa_cert.pem";
  private static final String PKCS_PATH = "resources/rsa_private_pkcs8";
  private static final String TOPIC_ID = "java-pst-" + (System.currentTimeMillis() / 1000L);
  private static final String MEMBER = "group:dpebot@google.com";
  private static final String ROLE = "roles/viewer";


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

  // Manager tests
  @Test
  public void testCreateGateway() throws Exception {
    final String gatewayName = "rsa-create-gateway";
    topic = DeviceGatewayExample.createIotTopic(PROJECT_ID, TOPIC_ID);
    DeviceGatewayExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceGatewayExample.createGateway(
        PROJECT_ID, CLOUD_REGION, REGISTRY_ID, gatewayName, RSA_PATH, "RS256");

    // Assertions
    String got = bout.toString();
    System.out.println(got);
    Assert.assertTrue(got.contains("Created gateway:"));

    // Clean up
    DeviceGatewayExample.deleteDevice(gatewayName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceGatewayExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  @Test
  public void testListGateways() throws Exception {
    final String gatewayName = "rsa-list-gateway";
    topic = DeviceGatewayExample.createIotTopic(PROJECT_ID, TOPIC_ID);
    DeviceGatewayExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceGatewayExample.createGateway(
        PROJECT_ID, CLOUD_REGION, REGISTRY_ID, gatewayName, RSA_PATH, "RS256");
    DeviceGatewayExample.listGateways(PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

    // Assertions
    String got = bout.toString();
    System.out.println(got);
    Assert.assertTrue(got.contains("Found 1 devices"));
    Assert.assertTrue(got.contains(String.format("Id: %s", gatewayName)));

    // Clean up
    DeviceGatewayExample.deleteDevice(gatewayName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceGatewayExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  @Test
  public void testBindDeviceToGatewayAndUnbind() throws Exception {
    final String gatewayName = "rsa-bind-gateway";
    final String deviceName = "rsa-bind-device";
    topic = DeviceGatewayExample.createIotTopic(PROJECT_ID, TOPIC_ID);
    DeviceGatewayExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceGatewayExample.createGateway(
        PROJECT_ID, CLOUD_REGION, REGISTRY_ID, gatewayName, RSA_PATH, "RS256");
    DeviceGatewayExample.createDevice(PROJECT_ID, CLOUD_REGION, REGISTRY_ID, deviceName);
    DeviceGatewayExample.bindDeviceToGateway(
        PROJECT_ID, CLOUD_REGION, REGISTRY_ID, deviceName, gatewayName);
    DeviceGatewayExample.unbindDeviceFromGateway(
        PROJECT_ID, CLOUD_REGION, REGISTRY_ID, deviceName, gatewayName);

    // Assertions
    String got = bout.toString();
    System.out.println(got);
    Assert.assertTrue(got.contains("Device bound: "));
    Assert.assertTrue(got.contains("Device unbound: "));

    // Clean up
    DeviceGatewayExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceGatewayExample.deleteDevice(gatewayName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceGatewayExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  // MQTT device tests
  @Test
  public void testGatewayListenForDevice() throws Exception {
    final String gatewayName = "rsa-listen-gateway";
    final String deviceName = "rsa-listen-device";
    topic = DeviceGatewayExample.createIotTopic(PROJECT_ID, TOPIC_ID);
    DeviceGatewayExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceGatewayExample.createGateway(
        PROJECT_ID, CLOUD_REGION, REGISTRY_ID, gatewayName, RSA_PATH, "RS256");
    DeviceGatewayExample.createDevice(PROJECT_ID, CLOUD_REGION, REGISTRY_ID, deviceName);
    DeviceGatewayExample.bindDeviceToGateway(
        PROJECT_ID, CLOUD_REGION, REGISTRY_ID, deviceName, gatewayName);

    Thread deviceThread = new Thread() {
      public void run() {
        try {
          DeviceGatewayExample.listenForConfigMessages(
              "mqtt.googleapis.com", (short)443, PROJECT_ID, CLOUD_REGION, REGISTRY_ID, gatewayName,
              PKCS_PATH, "RS256", deviceName);
        } catch (Exception e) {
          // TODO: Fail
          System.out.println("Failure on Exception");
        }
      }
    };
    deviceThread.start();
    Thread.sleep(3000); // Give the device a chance to connect / receive configurations
    deviceThread.join();

    // Assertions
    String got = bout.toString();
    System.out.println(got);
    Assert.assertTrue(got.contains("Payload"));

    // Clean up
    DeviceGatewayExample.unbindDeviceFromGateway(
        PROJECT_ID, CLOUD_REGION, REGISTRY_ID, deviceName, gatewayName);
    DeviceGatewayExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceGatewayExample.deleteDevice(gatewayName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceGatewayExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }

  @Test
  public void testErrorTopic() throws Exception {
    final String gatewayName = "rsa-listen-gateway-test";
    topic = DeviceGatewayExample.createIotTopic(PROJECT_ID, TOPIC_ID);
    DeviceGatewayExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceGatewayExample.createGateway(PROJECT_ID, CLOUD_REGION, REGISTRY_ID,
        gatewayName, RSA_PATH, "RS256");
    MqttClient client = DeviceGatewayExample.startMqtt("mqtt.googleapis.com", (short)443,
        PROJECT_ID, CLOUD_REGION, REGISTRY_ID, gatewayName, PKCS_PATH, "RS256");


    Thread deviceThread = new Thread() {
      public void run() {
        try {
          DeviceGatewayExample.attachDeviceToGateway(client, "garbage-device");
          DeviceGatewayExample.attachCallback(client, "garbage-device");
        } catch (Exception e) {
          // TODO: Fail
          System.out.println("Failure on Exception :" + e.toString());
        }
      }
    };

    deviceThread.start();
    Thread.sleep(4000);

    String got = bout.toString();
    Assert.assertTrue(got.contains("error_type"));

    //Clean up
    DeviceGatewayExample.deleteDevice(gatewayName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceGatewayExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
  }

  @Test
  public void testSendDataForBoundDevice() throws Exception {
    final String gatewayName = "rsa-send-gateway";
    final String deviceName = "rsa-send-device";
    topic = DeviceGatewayExample.createIotTopic(PROJECT_ID, TOPIC_ID);
    DeviceGatewayExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceGatewayExample.createGateway(
        PROJECT_ID, CLOUD_REGION, REGISTRY_ID, gatewayName, RSA_PATH, "RS256");
    DeviceGatewayExample.createDevice(PROJECT_ID, CLOUD_REGION, REGISTRY_ID, deviceName);
    DeviceGatewayExample.bindDeviceToGateway(
        PROJECT_ID, CLOUD_REGION, REGISTRY_ID, deviceName, gatewayName);

    Thread deviceThread = new Thread() {
      public void run() {
        try {
          DeviceGatewayExample.sendDataFromBoundDevice(
              "mqtt.googleapis.com", (short)443, PROJECT_ID, CLOUD_REGION, REGISTRY_ID, gatewayName,
              PKCS_PATH, "RS256", deviceName, "state", "Cookies are delish");
        } catch (Exception e) {
          // TODO: Fail
          System.out.println("Failure on Exception");
        }
      }
    };
    deviceThread.start();
    Thread.sleep(3000); // Give the device a chance to connect / receive configurations
    deviceThread.join();

    // Assertions
    String got = bout.toString();
    System.out.println(got);
    Assert.assertTrue(got.contains("Data sent"));

    // Clean up
    DeviceGatewayExample.unbindDeviceFromGateway(
        PROJECT_ID, CLOUD_REGION, REGISTRY_ID, deviceName, gatewayName);
    DeviceGatewayExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceGatewayExample.deleteDevice(gatewayName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceGatewayExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }
}
