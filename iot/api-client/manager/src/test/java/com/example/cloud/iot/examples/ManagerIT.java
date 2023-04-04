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

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.cloudiot.v1.CloudIotScopes;
import com.google.api.services.cloudiot.v1.model.DeviceRegistry;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.Topic;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for iot "Management" sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class ManagerIT {
  private static final String CLOUD_REGION = "us-central1";
  private static final String ES_PATH = "resources/ec_public.pem";
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String REGISTRY_ID =
      "java-reg-"
              + UUID.randomUUID().toString().substring(0, 10) + "-"
          + System.currentTimeMillis();
  private static final String RSA_PATH = "resources/rsa_cert.pem";
  private static final String PKCS_PATH = "resources/rsa_private_pkcs8";
  private static final String TOPIC_ID =
      "java-pst-"
          + UUID.randomUUID().toString().substring(0, 10) + "-" + System.currentTimeMillis();
  private static final String MEMBER = "group:dpebot@google.com";
  private static final String ROLE = "roles/viewer";
  private static Topic topic;
  private static boolean hasCleared = false;
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private DeviceRegistryExample app;

  @Before
  public void setUp() throws Exception {
    if (!hasCleared) {
      clearTestRegistries(); // Remove old / unused registries
      hasCleared = true;
    }

    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout, true, StandardCharsets.UTF_8.name()));
  }

  @After
  public void tearDown() throws Exception {
    System.setOut(null);
  }

  public void clearTestRegistries() throws Exception {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
    final CloudIot service =
        new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init)
            .setApplicationName("TEST")
            .build();

    final String projectPath = "projects/" + PROJECT_ID + "/locations/" + CLOUD_REGION;

    List<DeviceRegistry> registries =
        service
            .projects()
            .locations()
            .registries()
            .list(projectPath)
            .execute()
            .getDeviceRegistries();

    if (registries != null) {
      for (DeviceRegistry r : registries) {
        String registryId = r.getId();
        if (registryId.startsWith("java-reg-")) {
          long currMilliSecs = System.currentTimeMillis();
          long regMilliSecs =
              Long.parseLong(registryId.substring("java-reg-".length() + 11, registryId.length()));
          long diffMilliSecs = currMilliSecs - regMilliSecs;
          // remove registries which are older than one week.
          if (diffMilliSecs > (1000 * 60 * 60 * 24 * 7)) {
            System.out.println("Remove Id: " + r.getId());
            DeviceRegistryExample.clearRegistry(CLOUD_REGION, PROJECT_ID, registryId);
          }
        }
        // Also remove the current test registry
        try {
          DeviceRegistryExample.clearRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException gjre) {
          if (gjre.getStatusCode() == 404) {
            // Expected, the registry resource is available for creation.
          } else {
            throw gjre;
          }
        }
      }
    } else {
      System.out.println("Project has no registries.");
    }
  }

  @Test
  public void testPatchRsa() throws Exception {
    final String deviceName = "patchme-device-rsa";

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.createDeviceWithNoAuth(
          deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.patchRsa256ForAuth(
          deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertTrue(got.contains("Created device: {"));
    } finally {
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Test
  public void testPatchEs() throws Exception {
    final String deviceName = "patchme-device-es";

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.createDeviceWithNoAuth(
          deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.patchEs256ForAuth(
          deviceName, ES_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertTrue(got.contains("Created device: {"));
    } finally {
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Test
  public void testCreateDeleteUnauthDevice() throws Exception {
    final String deviceName = "noauth-device";

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.createDeviceWithNoAuth(deviceName, PROJECT_ID, CLOUD_REGION,
          REGISTRY_ID);

      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertTrue(got.contains("Created device: {"));
    } finally {
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Test
  public void testCreateDeleteEsDevice() throws Exception {
    final String deviceName = "es-device";

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.createDeviceWithEs256(
          deviceName, ES_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.getDeviceStates(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertTrue(got.contains("Created device: {"));
    } finally {
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Test
  public void testCreateDeleteRsaDevice() throws Exception {
    final String deviceName = "rsa-device";

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.createDeviceWithRs256(
          deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.getDeviceStates(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertTrue(got.contains("Created device: {"));
    } finally {
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Test
  public void testCreateGetDevice() throws Exception {
    final String deviceName = "rsa-device";

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.createDeviceWithRs256(
          deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.getDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertTrue(got.contains("Created device: {"));
      Assert.assertTrue(got.contains("Retrieving device"));
    } finally {
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Test
  public void testCreateConfigureDevice() throws Exception {
    final String deviceName = "rsa-device-config";
    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.createDeviceWithRs256(
          deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.setDeviceConfiguration(
          deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID, "some-test-data", 0L);

      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertTrue(got.contains("Updated: 2"));
    } finally {
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Test
  public void testCreateListDevices() throws Exception {
    final String deviceName = "rsa-device";

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.createDeviceWithRs256(
          deviceName, RSA_PATH, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.listDevices(PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertTrue(got.contains("Created device: {"));
      Assert.assertTrue(got.contains("Found"));
    } finally {
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Test
  public void testCreateGetRegistry() throws Exception {

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.getRegistry(PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertFalse(got.contains("eventNotificationConfigs"));
    } finally {
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Test
  public void testGetIam() throws Exception {
    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.getIamPermissions(PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertTrue(got.contains("ETAG"));
    } finally {
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Test
  public void testSetIam() throws Exception {
    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.setIamPermissions(PROJECT_ID, CLOUD_REGION, REGISTRY_ID, MEMBER, ROLE);
      DeviceRegistryExample.getIamPermissions(PROJECT_ID, CLOUD_REGION, REGISTRY_ID);

      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertTrue(got.contains("ETAG"));
    } finally {
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  // HTTP device tests

  @Test
  public void testHttpDeviceEvent() throws Exception {
    final String deviceName = "rsa-device-http-event";

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
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
      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertTrue(got.contains("200"));
      Assert.assertTrue(got.contains("OK"));

    } finally {
      // Clean up
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Test
  public void testHttpDeviceState() throws Exception {
    final String deviceName = "rsa-device-http-state";

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
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
      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertTrue(got.contains("200"));
      Assert.assertTrue(got.contains("OK"));

    } finally {
      // Clean up
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Test
  public void testHttpDeviceConfig() throws Exception {
    final String deviceName = "rsa-device-http-state";

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
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
      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertTrue(got.contains("200"));
      Assert.assertTrue(got.contains("OK"));
      Assert.assertTrue(got.contains("\"binaryData\": \"\""));

    } finally {
      // Clean up
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  // MQTT device tests
  @Test
  public void testMqttDeviceConfig() throws Exception {
    final String deviceName = "rsa-device-mqtt-config";

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
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
      String got = bout.toString(StandardCharsets.UTF_8.name());
      System.out.println(got);
      Assert.assertTrue(got.contains("Payload :"));

    } finally {
      // Clean up
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Ignore
  @Test
  public void testMqttDeviceCommand() throws Exception {
    final String deviceName = "rsa-device-mqtt-commands";

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
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

      Thread deviceThread =
          new Thread() {
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
      String got = bout.toString(StandardCharsets.UTF_8.name());
      System.out.println(got);
      Assert.assertTrue(got.contains("Finished loop successfully."));
      Assert.assertTrue(got.contains("me want cookie"));
      Assert.assertFalse(got.contains("Failure on Exception"));

    } finally {
      // Clean up
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Test
  public void testMqttDeviceEvents() throws Exception {
    final String deviceName = "rsa-device-mqtt-events";
    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
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
      String got = bout.toString(StandardCharsets.UTF_8.name());
      //
      // Finished loop successfully. Goodbye!

      Assert.assertTrue(got.contains("Publishing events message 1"));
      Assert.assertTrue(got.contains("Finished loop successfully. Goodbye!"));

    } finally {
      // Clean up
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Test
  public void testMqttDeviceState() throws Exception {
    final String deviceName = "rsa-device-mqtt-state";

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
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
        "-num_messages=10",
        "-algorithm=RS256"
      };
      com.example.cloud.iot.examples.MqttExample.main(testArgs);
      // End device test.

      // Assertions
      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertTrue(got.contains("Publishing state message 1"));
      Assert.assertTrue(got.contains("Finished loop successfully. Goodbye!"));

    } finally {
      // Clean up
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Ignore
  @Test
  public void testGatewayListenForDevice() throws Exception {
    final String gatewayName = "rsa-listen-gateway";
    final String deviceName = "rsa-listen-device";

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.createGateway(
          PROJECT_ID, CLOUD_REGION, REGISTRY_ID, gatewayName, RSA_PATH, "RS256");
      DeviceRegistryExample.createDevice(PROJECT_ID, CLOUD_REGION, REGISTRY_ID, deviceName);
      DeviceRegistryExample.bindDeviceToGateway(
          PROJECT_ID, CLOUD_REGION, REGISTRY_ID, deviceName, gatewayName);

      Thread deviceThread =
          new Thread() {
            public void run() {
              try {
                MqttExample.listenForConfigMessages(
                    "mqtt.googleapis.com",
                    (short) 443,
                    PROJECT_ID,
                    CLOUD_REGION,
                    REGISTRY_ID,
                    gatewayName,
                    PKCS_PATH,
                    "RS256",
                    deviceName);
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
      String got = bout.toString(StandardCharsets.UTF_8.name());
      System.out.println(got);
      Assert.assertTrue(got.contains("Payload"));

    } finally {
      // Clean up
      DeviceRegistryExample.unbindDeviceFromGateway(
          PROJECT_ID, CLOUD_REGION, REGISTRY_ID, deviceName, gatewayName);
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteDevice(gatewayName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Ignore
  @Test
  public void testErrorTopic() throws Exception {
    final String gatewayName = "rsa-listen-gateway-test";
    
    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.createGateway(
          PROJECT_ID, CLOUD_REGION, REGISTRY_ID, gatewayName, RSA_PATH, "RS256");
      MqttClient client =
          MqttExample.startMqtt(
              "mqtt.googleapis.com",
              (short) 443,
              PROJECT_ID,
              CLOUD_REGION,
              REGISTRY_ID,
              gatewayName,
              PKCS_PATH,
              "RS256");

      Thread deviceThread =
          new Thread() {
            public void run() {
              try {
                MqttExample.attachDeviceToGateway(client, "garbage-device");
                MqttExample.attachCallback(client, "garbage-device");
              } catch (Exception e) {
                // TODO: Fail
                StringBuilder builder = new StringBuilder();
                builder.append("Failure on exception: ").append(e);
                System.out.println(builder);
              }
            }
          };

      deviceThread.start();
      Thread.sleep(4000);

      String got = bout.toString(StandardCharsets.UTF_8.name());
      Assert.assertTrue(got.contains("error_type"));

    } finally {
      // Clean up
      DeviceRegistryExample.deleteDevice(gatewayName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }

  @Ignore
  @Test
  public void testSendDataForBoundDevice() throws Exception {
    final String gatewayName = "rsa-send-gateway";
    final String deviceName = "rsa-send-device";

    try {
      topic = DeviceRegistryExample.createIotTopic(PROJECT_ID, TOPIC_ID);
      DeviceRegistryExample.createRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID, TOPIC_ID);
      DeviceRegistryExample.createGateway(
          PROJECT_ID, CLOUD_REGION, REGISTRY_ID, gatewayName, RSA_PATH, "RS256");
      DeviceRegistryExample.createDevice(PROJECT_ID, CLOUD_REGION, REGISTRY_ID, deviceName);
      DeviceRegistryExample.bindDeviceToGateway(
          PROJECT_ID, CLOUD_REGION, REGISTRY_ID, deviceName, gatewayName);

      Thread deviceThread =
          new Thread() {
            public void run() {
              try {
                MqttExample.sendDataFromBoundDevice(
                    "mqtt.googleapis.com",
                    (short) 443,
                    PROJECT_ID,
                    CLOUD_REGION,
                    REGISTRY_ID,
                    gatewayName,
                    PKCS_PATH,
                    "RS256",
                    deviceName,
                    "state",
                    "Cookies are delish");
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
      String got = bout.toString("UTF-8");
      System.out.println(got);
      Assert.assertTrue(got.contains("Data sent"));

    } finally {
      // Clean up
      DeviceRegistryExample.unbindDeviceFromGateway(
          PROJECT_ID, CLOUD_REGION, REGISTRY_ID, deviceName, gatewayName);
      DeviceRegistryExample.deleteDevice(deviceName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteDevice(gatewayName, PROJECT_ID, CLOUD_REGION, REGISTRY_ID);
      DeviceRegistryExample.deleteRegistry(CLOUD_REGION, PROJECT_ID, REGISTRY_ID);
    }

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      topicAdminClient.deleteTopic(ProjectTopicName.of(PROJECT_ID, TOPIC_ID));
    }
  }
}
