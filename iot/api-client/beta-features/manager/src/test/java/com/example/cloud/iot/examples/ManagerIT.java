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

  // MQTT device tests
  @Test
  public void testMqttDeviceConfig() throws Exception {
    final String deviceName = "rsa-device-mqtt-commands";
    topic = DeviceRegistryExample.createIotTopic(
        PROJECT_ID,
        TOPIC_ID);
    DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
    DeviceRegistryExample.createDeviceWithRs256(
        deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

    // Device bootstrapped, time to connect and run.
    String[] testArgs = {
        "-project_id=" + PROJECT_ID,
        "-registry_id=" + REGISTRY_ID,
        "-cloud_region=" + CLOUD_REGION,
        "-device_id=" + deviceName,
        "-private_key_file=" + PKCS_PATH,
        "-wait_time=" + 10,
        "-algorithm=RS256"
    };

    Thread deviceThread = new Thread() {
      public void run() {
        try {
          com.example.cloud.iot.examples.MqttExample.main(testArgs);
        } catch (Exception e) {
          // TODO: Fail
          System.out.println("Failure on Exception");
        }
      }
    };
    deviceThread.start();

    Thread.sleep(500); // Give the device a chance to connect
    com.example.cloud.iot.examples.DeviceRegistryExample.sendCommand(
        deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID, "me want cookie!");

    deviceThread.join();
    // End device test.

    // Assertions
    String got = bout.toString();
    System.out.println(got);
    Assert.assertTrue(got.contains("Finished loop successfully."));
    Assert.assertTrue(got.contains("me want cookie"));
    Assert.assertFalse(got.contains("Failure on Exception"));

    // Clean up
    DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
    DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(topic.getNameAsTopicName());
    }
  }
}
