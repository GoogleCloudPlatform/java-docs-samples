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
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.cloudiot.v1.CloudIotScopes;
import com.google.api.services.cloudiot.v1.model.BindDeviceToGatewayRequest;
import com.google.api.services.cloudiot.v1.model.BindDeviceToGatewayResponse;
import com.google.api.services.cloudiot.v1.model.Device;
import com.google.api.services.cloudiot.v1.model.DeviceCredential;
import com.google.api.services.cloudiot.v1.model.DeviceRegistry;
import com.google.api.services.cloudiot.v1.model.EventNotificationConfig;
import com.google.api.services.cloudiot.v1.model.GatewayConfig;
import com.google.api.services.cloudiot.v1.model.PublicKeyCredential;
import com.google.api.services.cloudiot.v1.model.UnbindDeviceFromGatewayRequest;
import com.google.api.services.cloudiot.v1.model.UnbindDeviceFromGatewayResponse;
import com.google.cloud.Role;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.iam.v1.Binding;
import com.google.pubsub.v1.Topic;
import com.google.pubsub.v1.TopicName;

// [START iot_mqtt_includes]
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.HelpFormatter;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.joda.time.DateTime;
// [END iot_mqtt_includes]

/**
 * Example of a Device Gateway using Cloud IoT Core
 *
 * <pre>
 * <code>
 * $ mvn clean compile assembly:single
 * $ mvn exec:java \
 *       -Dexec.mainClass="com.google.cloud.iot.examples.DeviceGatewayExample" \
 *       -Dexec.args="-project_id=my-project-id \
 *                    -pubsub_topic=projects/my-project-id/topics/my-topic-id \
 *                    -ec_public_key_file=/path/to/ec_public.pem \
 *                    -rsa_certificate_file=/path/to/rsa_cert.pem"
 * </code>
 * </pre>
 */
public class DeviceGatewayExample {

  static final String APP_NAME = "DeviceGatewayExample";

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
  // TODO: END methods required for tests

  // [START iot_mqtt_jwt]
  /** Create a Cloud IoT Core JWT for the given project id, signed with the given RSA key. */
  private static String createJwtRsa(String projectId, String privateKeyFile)
      throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
    DateTime now = new DateTime();
    // Create a JWT to authenticate this device. The device will be disconnected after the token
    // expires, and will have to reconnect with a new token. The audience field should always be set
    // to the GCP project id.
    JwtBuilder jwtBuilder =
        Jwts.builder()
            .setIssuedAt(now.toDate())
            .setExpiration(now.plusMinutes(20).toDate())
            .setAudience(projectId);

    byte[] keyBytes = Files.readAllBytes(Paths.get(privateKeyFile));
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");

    return jwtBuilder.signWith(SignatureAlgorithm.RS256, kf.generatePrivate(spec)).compact();
  }

  /** Create a Cloud IoT Core JWT for the given project id, signed with the given ES key. */
  private static String createJwtEs(String projectId, String privateKeyFile)
      throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
    DateTime now = new DateTime();
    // Create a JWT to authenticate this device. The device will be disconnected after the token
    // expires, and will have to reconnect with a new token. The audience field should always be set
    // to the GCP project id.
    JwtBuilder jwtBuilder =
        Jwts.builder()
            .setIssuedAt(now.toDate())
            .setExpiration(now.plusMinutes(20).toDate())
            .setAudience(projectId);

    byte[] keyBytes = Files.readAllBytes(Paths.get(privateKeyFile));
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory kf = KeyFactory.getInstance("EC");

    return jwtBuilder.signWith(SignatureAlgorithm.ES256, kf.generatePrivate(spec)).compact();
  }
  // [END iot_mqtt_jwt]

  // [START iot_mqtt_configcallback]
  static MqttCallback mCallback;

  /** Attaches the callback used when configuration changes occur. */
  public static void attachCallback(MqttClient client, String deviceId) throws MqttException {
    mCallback =
        new MqttCallback() {
          @Override
          public void connectionLost(Throwable cause) {
            // Do nothing...
          }

          @Override
          public void messageArrived(String topic, MqttMessage message) throws Exception {
            String payload = new String(message.getPayload());
            System.out.println("Payload : " + payload);
            // TODO: Insert your parsing / handling of the configuration message here.
          }

          @Override
          public void deliveryComplete(IMqttDeliveryToken token) {
            // Do nothing;
          }
        };

    String configTopic = String.format("/devices/%s/config", deviceId);
    System.out.println(String.format("Listening on %s", configTopic));
    // The topic gateways receive error updates on. QoS must be 0.
    String errorTopic = String.format("/devices/%s/errors", deviceId);
    System.out.println(String.format("Listening on %s", errorTopic));
    client.subscribe(errorTopic, 0);
    client.subscribe(configTopic, 1);
    client.setCallback(mCallback);
  }
  // [END iot_mqtt_configcallback]

  /** Connects the gateway to the MQTT bridge. */
  public static MqttClient startMqtt(
      String mqttBridgeHostname,
      int mqttBridgePort,
      String projectId,
      String cloudRegion,
      String registryId,
      String gatewayId,
      String privateKeyFile,
      String algorithm)
      throws NoSuchAlgorithmException, IOException, MqttException, InterruptedException,
          InvalidKeySpecException {
    // [START iot_gateway_start_mqtt]

    // Build the connection string for Google's Cloud IoT Core MQTT server. Only SSL
    // connections are accepted. For server authentication, the JVM's root certificates
    // are used.
    final String mqttServerAddress =
        String.format("ssl://%s:%s", mqttBridgeHostname, mqttBridgePort);

    // Create our MQTT client. The mqttClientId is a unique string that identifies this device. For
    // Google Cloud IoT Core, it must be in the format below.
    final String mqttClientId =
        String.format(
            "projects/%s/locations/%s/registries/%s/devices/%s",
            projectId, cloudRegion, registryId, gatewayId);

    MqttConnectOptions connectOptions = new MqttConnectOptions();
    // Note that the Google Cloud IoT Core only supports MQTT 3.1.1, and Paho requires that we
    // explictly set this. If you don't set MQTT version, the server will immediately close its
    // connection to your device.
    connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

    Properties sslProps = new Properties();
    sslProps.setProperty("com.ibm.ssl.protocol", "TLSv1.2");
    connectOptions.setSSLProperties(sslProps);

    // With Google Cloud IoT Core, the username field is ignored, however it must be set for the
    // Paho client library to send the password field. The password field is used to transmit a JWT
    // to authorize the device.
    connectOptions.setUserName("unused");

    DateTime iat = new DateTime();
    if (algorithm.equals("RS256")) {
      connectOptions.setPassword(createJwtRsa(projectId, privateKeyFile).toCharArray());
    } else if (algorithm.equals("ES256")) {
      connectOptions.setPassword(createJwtEs(projectId, privateKeyFile).toCharArray());
    } else {
      throw new IllegalArgumentException(
          "Invalid algorithm " + algorithm + ". Should be one of 'RS256' or 'ES256'.");
    }

    System.out.println(String.format(mqttClientId));

    // Create a client, and connect to the Google MQTT bridge.
    MqttClient client = new MqttClient(mqttServerAddress, mqttClientId, new MemoryPersistence());

    // Both connect and publish operations may fail. If they do, allow retries but with an
    // exponential backoff time period.
    long initialConnectIntervalMillis = 500L;
    long maxConnectIntervalMillis = 6000L;
    long maxConnectRetryTimeElapsedMillis = 900000L;
    float intervalMultiplier = 1.5f;

    long retryIntervalMs = initialConnectIntervalMillis;
    long totalRetryTimeMs = 0;

    while (!client.isConnected() && totalRetryTimeMs < maxConnectRetryTimeElapsedMillis) {
      try {
        client.connect(connectOptions);
      } catch (MqttException e) {
        int reason = e.getReasonCode();

        // If the connection is lost or if the server cannot be connected, allow retries, but with
        // exponential backoff.
        System.out.println("An error occurred: " + e.getMessage());
        if (reason == MqttException.REASON_CODE_CONNECTION_LOST
            || reason == MqttException.REASON_CODE_SERVER_CONNECT_ERROR) {
          System.out.println("Retrying in " + retryIntervalMs / 1000.0 + " seconds.");
          Thread.sleep(retryIntervalMs);
          totalRetryTimeMs += retryIntervalMs;
          retryIntervalMs *= intervalMultiplier;
          if (retryIntervalMs > maxConnectIntervalMillis) {
            retryIntervalMs = maxConnectIntervalMillis;
          }
        } else {
          throw e;
        }
      }
    }

    attachCallback(client, gatewayId);
    return client;
    // [END iot_gateway_start_mqtt]
  }

  public static void bindDeviceToGateway(
      String projectId, String cloudRegion, String registryName, String deviceId, String gatewayId)
      throws GeneralSecurityException, IOException {
    // [START bind_device_to_gateway]
    createDevice(projectId, cloudRegion, registryName, deviceId);

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

    BindDeviceToGatewayRequest request = new BindDeviceToGatewayRequest();
    request.setDeviceId(deviceId);
    request.setGatewayId(gatewayId);

    BindDeviceToGatewayResponse response =
        service
            .projects()
            .locations()
            .registries()
            .bindDeviceToGateway(registryPath, request)
            .execute();

    System.out.println(String.format("Device bound: %s", response.toPrettyString()));
    // [END bind_device_to_gateway]
  }

  public static void unbindDeviceFromGateway(
      String projectId, String cloudRegion, String registryName, String deviceId, String gatewayId)
      throws GeneralSecurityException, IOException {
    // [START unbind_device_from_gateway]
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

    UnbindDeviceFromGatewayRequest request = new UnbindDeviceFromGatewayRequest();
    request.setDeviceId(deviceId);
    request.setGatewayId(gatewayId);

    UnbindDeviceFromGatewayResponse response =
        service
            .projects()
            .locations()
            .registries()
            .unbindDeviceFromGateway(registryPath, request)
            .execute();

    System.out.println(String.format("Device unbound: %s", response.toPrettyString()));
    // [END unbind_device_from_gateway]
  }

  public static void sendDataFromDevice(
      MqttClient client, String deviceId, String messageType, String data) throws MqttException {
    // [START send_data_from_bound_device]
    if (!messageType.equals("events") && !messageType.equals("state")) {
      System.err.println("Invalid message type, must ether be 'state' or events'");
      return;
    }
    final String dataTopic = String.format("/devices/%s/%s", deviceId, messageType);
    MqttMessage message = new MqttMessage(data.getBytes());
    message.setQos(1);
    client.publish(dataTopic, message);
    System.out.println("Data sent");
    // [END send_data_from_bound_device]
  }

  public static void attachDeviceToGateway(MqttClient client, String deviceId)
      throws MqttException {
    // [START attach_device]
    final String attachTopic = String.format("/devices/%s/attach", deviceId);
    System.out.println(String.format("Attaching: %s", attachTopic));
    String attachPayload = "{}";
    MqttMessage message = new MqttMessage(attachPayload.getBytes());
    message.setQos(1);
    client.publish(attachTopic, message);
    // [END attach_device]
  }

  /** Detaches a bound device from the Gateway. */
  public static void detachDeviceFromGateway(MqttClient client, String deviceId)
      throws MqttException {
    // [START detach_device]
    final String detachTopic = String.format("/devices/%s/detach", deviceId);
    System.out.println(String.format("Detaching: %s", detachTopic));
    String attachPayload = "{}";
    MqttMessage message = new MqttMessage(attachPayload.getBytes());
    message.setQos(1);
    client.publish(detachTopic, message);
    // [END detach_device]
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

  /** Create a gateway to bind devices to. */
  public static void createGateway(
      String projectId,
      String cloudRegion,
      String registryName,
      String gatewayId,
      String certificateFilePath,
      String algorithm)
      throws GeneralSecurityException, IOException {
    // [START create_gateway]
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

    System.out.println("Creating gateway with id: " + gatewayId);
    Device device = new Device();
    device.setId(gatewayId);

    GatewayConfig gwConfig = new GatewayConfig();
    gwConfig.setGatewayType("GATEWAY");
    gwConfig.setGatewayAuthMethod("ASSOCIATION_ONLY");

    String keyFormat = "RSA_X509_PEM";
    if (algorithm == "ES256") {
      keyFormat = "ES256_PEM";
    }

    PublicKeyCredential publicKeyCredential = new PublicKeyCredential();

    byte[] keyBytes = Files.readAllBytes(Paths.get(certificateFilePath));
    publicKeyCredential.setKey(new String(keyBytes));
    publicKeyCredential.setFormat(keyFormat);
    DeviceCredential deviceCredential = new DeviceCredential();
    deviceCredential.setPublicKey(publicKeyCredential);

    device.setGatewayConfig(gwConfig);
    device.setCredentials(Arrays.asList(deviceCredential));
    Device createdDevice =
        service
            .projects()
            .locations()
            .registries()
            .devices()
            .create(registryPath, device)
            .execute();

    System.out.println("Created gateway: " + createdDevice.toPrettyString());
    // [END create_gateway]
  }

  public static void listGateways(String projectId, String cloudRegion, String registryName)
      throws IOException, GeneralSecurityException {
    // [START list_gateways]
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
        if (d.getGatewayConfig() != null
            && d.getGatewayConfig().getGatewayType() != null
            && d.getGatewayConfig().getGatewayType().equals("GATEWAY")) {
          System.out.println("Id: " + d.getId());
          if (d.getConfig() != null) {
            // Note that this will show the device config in Base64 encoded format.
            System.out.println("Config: " + d.getGatewayConfig().toPrettyString());
          }
          System.out.println();
        }
      }
    } else {
      System.out.println("Registry has no devices.");
    }
    // [END list_gateways]
  }

  /** List devices bound to a gateway. */
  public static void listDevicesForGateway(
      String projectId, String cloudRegion, String registryName, String gatewayId)
      throws IOException, GeneralSecurityException {
    // [START list_devices_for_gateway]
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service =
        new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init)
            .setApplicationName(APP_NAME)
            .build();

    final String gatewayPath =
        String.format(
            "projects/%s/locations/%s/registries/%s/devices/%s",
            projectId, cloudRegion, registryName, gatewayId);

    final String registryPath =
        String.format(
            "projects/%s/locations/%s/registries/%s", projectId, cloudRegion, registryName);

    List<Device> deviceNumIds =
        service
            .projects()
            .locations()
            .registries()
            .devices()
            .list(registryPath)
            .setGatewayListOptionsAssociationsGatewayId(gatewayId)
            .execute()
            .getDevices();

    if (deviceNumIds != null) {
      System.out.println("Found " + deviceNumIds.size() + " devices");
      for (Device device : deviceNumIds) {
        System.out.println(String.format("ID: %s", device.getId()));
      }
    } else {
      System.out.println("Gateway has no bound devices.");
    }
    // [END list_devices_for_gateway]
  }

  /** Sends data on behalf of a bound device using the Gateway. */
  public static void sendDataFromBoundDevice(
      String mqttBridgeHostname,
      short mqttBridgePort,
      String projectId,
      String cloudRegion,
      String registryName,
      String gatewayId,
      String privateKeyFile,
      String algorithm,
      String deviceId,
      String messageType,
      String telemetryData)
      throws MqttException, IOException, InvalidKeySpecException, InterruptedException,
          NoSuchAlgorithmException {
    // [START send_data_from_bound_device]
    MqttClient client =
        startMqtt(
            mqttBridgeHostname,
            mqttBridgePort,
            projectId,
            cloudRegion,
            registryName,
            gatewayId,
            privateKeyFile,
            algorithm);
    attachDeviceToGateway(client, deviceId);
    sendDataFromDevice(client, deviceId, messageType, telemetryData);
    detachDeviceFromGateway(client, deviceId);
    // [END send_data_from_bound_device]
  }

  public static void listenForConfigMessages(
      String mqttBridgeHostname,
      short mqttBridgePort,
      String projectId,
      String cloudRegion,
      String registryName,
      String gatewayId,
      String privateKeyFile,
      String algorithm,
      String deviceId)
      throws MqttException, IOException, InvalidKeySpecException, InterruptedException,
          NoSuchAlgorithmException {
    // Connect the Gateway
    MqttClient client =
        startMqtt(
            mqttBridgeHostname,
            mqttBridgePort,
            projectId,
            cloudRegion,
            registryName,
            gatewayId,
            privateKeyFile,
            algorithm);
    // Connect the bound device and listen for configuration messages.
    attachDeviceToGateway(client, deviceId);
    attachCallback(client, deviceId);

    detachDeviceFromGateway(client, deviceId);
  }

  /** Entry poit for CLI. */
  public static void main(String[] args) throws Exception {
    DeviceGatewayExampleOptions options = DeviceGatewayExampleOptions.fromFlags(args);

    if (options == null) {
      // Could not parse.
      return;
    }

    switch (options.command) {
      case "bind-device-to-gateway":
        System.out.println("Binding device to gateway:");
        bindDeviceToGateway(
            options.projectId,
            options.cloudRegion,
            options.registryName,
            options.deviceId,
            options.gatewayId);
        break;
      case "create-gateway":
        System.out.println("Creating Gateway:");
        createGateway(
            options.projectId,
            options.cloudRegion,
            options.registryName,
            options.gatewayId,
            options.publicKeyFile,
            options.algorithm);
        break;
      case "list-gateways":
        System.out.println("Listing gateways:");
        listGateways(options.projectId, options.cloudRegion, options.registryName);
        break;
      case "list-devices-for-gateway":
        System.out.println("Listing devices for gateway:");
        listDevicesForGateway(
            options.projectId, options.cloudRegion, options.registryName, options.gatewayId);
        break;
      case "listen-for-config-messages":
        System.out.println(
            String.format("Listening for configuration messages for %s:", options.deviceId));
        listenForConfigMessages(
            options.mqttBridgeHostname,
            options.mqttBridgePort,
            options.projectId,
            options.cloudRegion,
            options.registryName,
            options.gatewayId,
            options.privateKeyFile,
            options.algorithm,
            options.deviceId);
        break;
      case "send-data-from-bound-device":
        System.out.println("Sending data on behalf of device:");
        sendDataFromBoundDevice(
            options.mqttBridgeHostname,
            options.mqttBridgePort,
            options.projectId,
            options.cloudRegion,
            options.registryName,
            options.gatewayId,
            options.privateKeyFile,
            options.algorithm,
            options.deviceId,
            options.messageType,
            options.telemetryData);
        break;
      case "unbind-device-from-gateway":
        System.out.println("Unbinding device from gateway:");
        unbindDeviceFromGateway(
            options.projectId,
            options.cloudRegion,
            options.registryName,
            options.deviceId,
            options.gatewayId);
        break;
      default:
        String header = "Cloud IoT Core Commandline Example (Device Gateway Example): \n\n";
        String footer = "\nhttps://cloud.google.com/iot-core";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("DeviceGatewayExample", header, options.options, footer, true);
        break;
    }
  }
}
