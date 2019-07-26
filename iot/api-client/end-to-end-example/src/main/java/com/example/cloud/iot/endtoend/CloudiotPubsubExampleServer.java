/*
 * Copyright 2019 Google Inc.
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

package com.example.cloud.iot.endtoend;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.cloudiot.v1.CloudIotScopes;
import com.google.api.services.cloudiot.v1.model.Device;
import com.google.api.services.cloudiot.v1.model.DeviceRegistry;
import com.google.api.services.cloudiot.v1.model.EventNotificationConfig;
import com.google.api.services.cloudiot.v1.model.GatewayConfig;
import com.google.api.services.cloudiot.v1.model.ModifyCloudToDeviceConfigRequest;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Sample server that pushes configuration to Google Cloud IoT devices.
 *
 * <p>This example represents a server that consumes telemetry data from multiple Cloud IoT devices.
 * The devices report telemetry data, which the server consumes from a Cloud Pub/Sub topic. The
 * server then decides whether to turn on or off individual devices fans.
 *
 * <p>If you are running this example from a Compute Engine VM, you will have to enable the Cloud
 * Pub/Sub API for your project, which you can do from the Cloud Console. Create a pubsub topic, for
 * example projects/my-project-id/topics/my-topic-name, and a subscription, for example
 * projects/my-project-id/subscriptions/my-topic-subscription.
 *
 * <p>You can then run the example with <prev> <code>
 * $ mvn clean compile assembly:single
 *
 * $ mvn exec:java \
 *       -Dexec.mainClass="com.example.cloud.iot.endtoend.CloudiotPubsubExampleServer" \
 *       -Dexec.args="-project_id=<your-iot-project> \
 *                 -pubsub_subscription=<your-pubsub-subscription"
 *
 * </code> </prev>
 */
public class CloudiotPubsubExampleServer {

  static final String APP_NAME = "CloudiotPubsubExampleServer";

  CloudIot service;

  /** Represents the state of the server. */
  public CloudiotPubsubExampleServer() throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    this.service =
        new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init)
            .setApplicationName(APP_NAME)
            .build();
  }

  /** Create a registry for Cloud IoT. */
  public static void createRegistry(
      String cloudRegion, String projectId, String registryName, String pubsubTopicPath)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service =
        new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init)
            .setApplicationName(APP_NAME)
            .build();

    final String projectPath = "projects/" + projectId + "/locations/" + cloudRegion;
    final String fullPubsubPath = "projects/" + projectId + "/topics/" + pubsubTopicPath;

    DeviceRegistry registry = new DeviceRegistry();
    EventNotificationConfig notificationConfig = new EventNotificationConfig();
    notificationConfig.setPubsubTopicName(fullPubsubPath);
    List<EventNotificationConfig> notificationConfigs = new ArrayList<EventNotificationConfig>();
    notificationConfigs.add(notificationConfig);
    registry.setEventNotificationConfigs(notificationConfigs);
    registry.setId(registryName);

    DeviceRegistry reg =
        service.projects().locations().registries().create(projectPath, registry).execute();
    System.out.println("Created registry: " + reg.getName());
  }

  /** Delete this registry from Cloud IoT. */
  public static void deleteRegistry(String cloudRegion, String projectId, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service =
        new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init)
            .setApplicationName(APP_NAME)
            .build();

    final String registryPath =
        String.format(
            "projects/%s/locations/%s/registries/%s", projectId, cloudRegion, registryName);

    System.out.println("Deleting: " + registryPath);
    service.projects().locations().registries().delete(registryPath).execute();
  }

  /** Delete this device from Cloud IoT. */
  public static void deleteDevice(
      String deviceId, String projectId, String cloudRegion, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service =
        new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init)
            .setApplicationName(APP_NAME)
            .build();

    final String devicePath =
        String.format(
            "projects/%s/locations/%s/registries/%s/devices/%s",
            projectId, cloudRegion, registryName, deviceId);

    System.out.println("Deleting device " + devicePath);
    service.projects().locations().registries().devices().delete(devicePath).execute();
  }

  /** Create a device to bind to a gateway. */
  public static void createDevice(
      String projectId, String cloudRegion, String registryName, String deviceId)
      throws GeneralSecurityException, IOException {
    // [START create_device]
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service =
        new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init)
            .setApplicationName(APP_NAME)
            .build();

    final String registryPath =
        String.format(
            "projects/%s/locations/%s/registries/%s", projectId, cloudRegion, registryName);

    List<Device> devices =
        service
            .projects()
            .locations()
            .registries()
            .devices()
            .list(registryPath)
            .setFieldMask("config,gatewayConfig")
            .execute()
            .getDevices();

    if (devices != null) {
      System.out.println("Found " + devices.size() + " devices");
      for (Device d : devices) {
        if ((d.getId() != null && d.getId().equals(deviceId))
            || (d.getName() != null && d.getName().equals(deviceId))) {
          System.out.println("Device exists, skipping.");
          return;
        }
      }
    }

    System.out.println("Creating device with id: " + deviceId);
    Device device = new Device();
    device.setId(deviceId);

    GatewayConfig gwConfig = new GatewayConfig();
    gwConfig.setGatewayType("NON_GATEWAY");
    gwConfig.setGatewayAuthMethod("ASSOCIATION_ONLY");

    device.setGatewayConfig(gwConfig);
    Device createdDevice =
        service
            .projects()
            .locations()
            .registries()
            .devices()
            .create(registryPath, device)
            .execute();

    System.out.println("Created device: " + createdDevice.toPrettyString());
    // [END create_device]
  }

  /** Push the data to the given device as configuration. */
  public void updateDeviceConfig(
      String projectId, String region, String registryId, String deviceId, JSONObject data)
      throws JSONException, UnsupportedEncodingException {
    // Push the data to the given device as configuration.
    JSONObject configData = new JSONObject();
    System.out.println(
        String.format("Device %s has temperature of: %d", deviceId, data.getInt("temperature")));
    if (data.getInt("temperature") < 0) {
      // Turn off the fan
      configData.put("fan_on", false);
      System.out.println("Setting fan state for device " + deviceId + " to off.");
    } else if (data.getInt("temperature") > 10) {
      // Turn on the fan
      configData.put("fan_on", true);
      System.out.println("Setting fan state for device " + deviceId + " to on.");
    } else {
      // temperature is okay, don't need to push new config
      return;
    }
    // Data sent through the wire has to be base64 encoded.
    Base64.Encoder encoder = Base64.getEncoder();
    String encPayload = encoder.encodeToString(configData.toString().getBytes("UTF-8"));

    ModifyCloudToDeviceConfigRequest request = new ModifyCloudToDeviceConfigRequest();
    request.setBinaryData(encPayload);

    String deviceName =
        String.format(
            "projects/%s/locations/%s/" + "registries/%s/devices/%s",
            projectId, region, registryId, deviceId);

    try {
      service
          .projects()
          .locations()
          .registries()
          .devices()
          .modifyCloudToDeviceConfig(deviceName, request)
          .execute();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** The main loop. Consumes messages from the Pub/Sub subscription. */
  public void run(String projectId, String subscriptionId) {
    ProjectSubscriptionName subscriptionName =
        ProjectSubscriptionName.of(projectId, subscriptionId);
    // Instantiate an asynchronous message receiver
    MessageReceiver receiver =
        new MessageReceiver() {
          @Override
          public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
            // handle incoming message, then ack/nack the received message
            try {
              JSONObject data = new JSONObject(message.getData().toStringUtf8());
              String projectId = message.getAttributesOrThrow("projectId");
              String region = message.getAttributesOrThrow("deviceRegistryLocation");
              String registryId = message.getAttributesOrThrow("deviceRegistryId");
              String deviceId = message.getAttributesOrThrow("deviceId");

              CloudiotPubsubExampleServer.this.updateDeviceConfig(
                  projectId, region, registryId, deviceId, data);
              consumer.ack();
            } catch (JSONException e) {
              e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
              e.printStackTrace();
            }
          }
        };

    Subscriber subscriber = null;
    try {
      subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
      subscriber.addListener(
          new Subscriber.Listener() {
            @Override
            public void failed(Subscriber.State from, Throwable failure) {
              // Handle failure. This is called when the Subscriber encountered a fatal error and is
              // shutting down.
              System.err.println(failure);
            }
          },
          MoreExecutors.directExecutor());
      subscriber.startAsync().awaitRunning();
      System.out.println(
          String.format("Listening for messages on %s", subscriber.getSubscriptionNameString()));
      while (true) {
        Thread.sleep(60000);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      if (subscriber != null) {
        subscriber.stopAsync().awaitTerminated();
      }
    }
  }

  /** Entry point for CLI. */
  public static void main(String[] args) throws Exception {
    CloudiotPubsubExampleServerOptions options = CloudiotPubsubExampleServerOptions.fromFlags(args);
    if (options == null) {
      System.exit(1);
    }

    CloudiotPubsubExampleServer server = new CloudiotPubsubExampleServer();
    String projectId = options.projectId;
    String pubsubscription = options.pubsubSubscription;
    server.run(projectId, pubsubscription);
  }
}
