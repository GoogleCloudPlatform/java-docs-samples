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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Charsets;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.cloudiot.v1.CloudIotScopes;
import com.google.api.services.cloudiot.v1.model.Device;
import com.google.api.services.cloudiot.v1.model.DeviceCredential;
import com.google.api.services.cloudiot.v1.model.DeviceRegistry;
import com.google.api.services.cloudiot.v1.model.EventNotificationConfig;
import com.google.api.services.cloudiot.v1.model.PublicKeyCredential;
import com.google.api.services.cloudiot.v1.model.SendCommandToDeviceRequest;
import com.google.api.services.cloudiot.v1.model.SendCommandToDeviceResponse;
import com.google.cloud.Role;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.common.io.Files;
import com.google.iam.v1.Binding;
import com.google.pubsub.v1.Topic;
import com.google.pubsub.v1.TopicName;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.apache.commons.cli.HelpFormatter;
/**
 * Example of using Cloud IoT device manager API to administer devices, registries and projects.
 *
 * <p>This example uses the Device Manager API to create, retrieve, disable, list and delete Cloud
 * IoT devices and registries, using both RSA and eliptic curve keys for authentication.
 *
 * <p>To start, follow the instructions on the Developer Guide at cloud.google.com/iot to create a
 * service_account.json file and Cloud Pub/Sub topic as discussed in the guide. You will then need
 * to point to the service_account.json file as described in
 * https://developers.google.com/identity/protocols/application-default-credentials#howtheywork
 *
 * <p>Before running the example, we have to create private and public keys, as described in
 * cloud.google.com/iot. Since we are interacting with the device manager, we will only use the
 * public keys. The private keys are used to sign JWTs to authenticate devices. See the
 * <a href="https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/iot/api-client/mqtt_example">MQTT client example</a>
 * for more information.
 *
 * <p>Finally, compile and run the example with:
 *
 * <pre>
 * <code>
 * $ mvn clean compile assembly:single
 * $ mvn exec:java \
 *       -Dexec.mainClass="com.google.cloud.iot.examples.DeviceRegistryExample" \
 *       -Dexec.args="-project_id=my-project-id \
 *                    -pubsub_topic=projects/my-project-id/topics/my-topic-id \
 *                    -ec_public_key_file=/path/to/ec_public.pem \
 *                    -rsa_certificate_file=/path/to/rsa_cert.pem"
 * </code>
 * </pre>
 */

public class DeviceRegistryExample {

  static final String APP_NAME = "DeviceRegistryExample";


  // TODO: ---- The following are required for testing until merged with GA. (START REMOVEME)
  /** Creates a topic and grants the IoT service account access. */
  public static Topic createIotTopic(String projectId, String topicId) throws Exception {
    // Create a new topic
    final TopicName topicName = TopicName.create(projectId, topicId);

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      final Topic topic = topicAdminClient.createTopic(topicName);
      com.google.iam.v1.Policy policy = topicAdminClient.getIamPolicy(topicName.toString());
      // add role -> members binding
      Binding binding =
          Binding.newBuilder()
              .addMembers("serviceAccount:cloud-iot@system.gserviceaccount.com")
              .setRole(Role.owner().toString())
              .build();

      // create updated policy
      com.google.iam.v1.Policy updatedPolicy =
          com.google.iam.v1.Policy.newBuilder(policy).addBindings(binding).build();
      topicAdminClient.setIamPolicy(topicName.toString(), updatedPolicy);

      System.out.println("Setup topic / policy for: " + topic.getName());
      return topic;
    }
  }

  /** Create a registry for Cloud IoT. */
  public static void createRegistry(String cloudRegion, String projectId, String registryName,
                                    String pubsubTopicPath)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),jsonFactory, init)
        .setApplicationName(APP_NAME).build();

    final String projectPath = "projects/" + projectId + "/locations/" + cloudRegion;
    final String fullPubsubPath = "projects/" + projectId + "/topics/" + pubsubTopicPath;

    DeviceRegistry registry = new DeviceRegistry();
    EventNotificationConfig notificationConfig = new EventNotificationConfig();
    notificationConfig.setPubsubTopicName(fullPubsubPath);
    List<EventNotificationConfig> notificationConfigs = new ArrayList<EventNotificationConfig>();
    notificationConfigs.add(notificationConfig);
    registry.setEventNotificationConfigs(notificationConfigs);
    registry.setId(registryName);

    DeviceRegistry reg = service.projects().locations().registries().create(projectPath,
        registry).execute();
    System.out.println("Created registry: " + reg.getName());
  }

  /** Delete this registry from Cloud IoT. */
  public static void deleteRegistry(String cloudRegion, String projectId, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),jsonFactory, init)
        .setApplicationName(APP_NAME).build();

    final String registryPath = String.format("projects/%s/locations/%s/registries/%s",
        projectId, cloudRegion, registryName);

    System.out.println("Deleting: " + registryPath);
    service.projects().locations().registries().delete(registryPath).execute();
  }

  public static void createDeviceWithRs256(String deviceId, String certificateFilePath,
                                           String projectId, String cloudRegion,
                                           String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),jsonFactory, init)
        .setApplicationName(APP_NAME).build();

    final String registryPath = String.format("projects/%s/locations/%s/registries/%s",
        projectId, cloudRegion, registryName);

    PublicKeyCredential publicKeyCredential = new PublicKeyCredential();
    String key = Files.toString(new File(certificateFilePath), Charsets.UTF_8);
    publicKeyCredential.setKey(key);
    publicKeyCredential.setFormat("RSA_X509_PEM");

    DeviceCredential devCredential = new DeviceCredential();
    devCredential.setPublicKey(publicKeyCredential);

    System.out.println("Creating device with id: " + deviceId);
    Device device = new Device();
    device.setId(deviceId);
    device.setCredentials(Arrays.asList(devCredential));
    Device createdDevice =
        service
            .projects()
            .locations()
            .registries()
            .devices()
            .create(registryPath, device)
            .execute();

    System.out.println("Created device: " + createdDevice.toPrettyString());
  }

  /** Delete the given device from the registry. */
  public static void deleteDevice(String deviceId, String projectId, String cloudRegion,
                                  String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),jsonFactory, init)
            .setApplicationName(APP_NAME).build();

    final String devicePath = String.format("projects/%s/locations/%s/registries/%s/devices/%s",
        projectId, cloudRegion, registryName, deviceId);

    System.out.println("Deleting device " + devicePath);
    service.projects().locations().registries().devices().delete(devicePath).execute();
  }
  // TODO: (END REMOVEME) The following methods are new -----

  /** Send a command to a device. **/
  // [START send_command]
  public static void sendCommand(
      String deviceId, String projectId, String cloudRegion, String registryName, String data)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),jsonFactory, init)
        .setApplicationName(APP_NAME).build();

    final String devicePath = String.format("projects/%s/locations/%s/registries/%s/devices/%s",
        projectId, cloudRegion, registryName, deviceId);

    SendCommandToDeviceRequest req = new SendCommandToDeviceRequest();

    // Data sent through the wire has to be base64 encoded.
    Base64.Encoder encoder = Base64.getEncoder();
    String encPayload = encoder.encodeToString(data.getBytes("UTF-8"));
    req.setBinaryData(encPayload);
    System.out.printf("Sending command to %s\n", devicePath);

    SendCommandToDeviceResponse res =
        service
            .projects()
            .locations()
            .registries()
            .devices()
            .sendCommandToDevice(devicePath, req).execute();

    System.out.println("Command response: " + res.toString());
    // [END send_command]
  }


  /** Entry poit for CLI. */
  public static void main(String[] args) throws Exception {
    DeviceRegistryExampleOptions options = DeviceRegistryExampleOptions.fromFlags(args);
    if (options == null) {
      // Could not parse.
      return;
    }

    switch (options.command) {
      case "send-command":
        System.out.println("Sending command to device:");
        sendCommand(options.deviceId, options.projectId, options.cloudRegion, options.registryName,
            options.commandData);
        break;
      default:
        String header = "Cloud IoT Core Commandline Example (Device / Registry management): \n\n";
        String footer = "\nhttps://cloud.google.com/iot-core";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("DeviceRegistryExample", header, options.options, footer,
            true);
        break;
    }
  }
}
