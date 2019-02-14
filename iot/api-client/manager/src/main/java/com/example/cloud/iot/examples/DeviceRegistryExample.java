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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Charsets;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.cloudiot.v1.CloudIotScopes;
import com.google.api.services.cloudiot.v1.model.BindDeviceToGatewayRequest;
import com.google.api.services.cloudiot.v1.model.BindDeviceToGatewayResponse;
import com.google.api.services.cloudiot.v1.model.Device;
import com.google.api.services.cloudiot.v1.model.DeviceConfig;
import com.google.api.services.cloudiot.v1.model.DeviceCredential;
import com.google.api.services.cloudiot.v1.model.DeviceRegistry;
import com.google.api.services.cloudiot.v1.model.DeviceState;
import com.google.api.services.cloudiot.v1.model.EventNotificationConfig;
import com.google.api.services.cloudiot.v1.model.GatewayConfig;
import com.google.api.services.cloudiot.v1.model.GetIamPolicyRequest;
import com.google.api.services.cloudiot.v1.model.ListDeviceStatesResponse;
import com.google.api.services.cloudiot.v1.model.ModifyCloudToDeviceConfigRequest;
import com.google.api.services.cloudiot.v1.model.PublicKeyCredential;
import com.google.api.services.cloudiot.v1.model.SendCommandToDeviceRequest;
import com.google.api.services.cloudiot.v1.model.SendCommandToDeviceResponse;
import com.google.api.services.cloudiot.v1.model.SetIamPolicyRequest;
import com.google.api.services.cloudiot.v1.model.UnbindDeviceFromGatewayRequest;
import com.google.api.services.cloudiot.v1.model.UnbindDeviceFromGatewayResponse;
import com.google.cloud.Role;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.common.io.Files;
import com.google.iam.v1.Binding;
import com.google.pubsub.v1.Topic;
import com.google.pubsub.v1.TopicName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.apache.commons.cli.HelpFormatter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

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
 * public keys. The private keys are used to sign JWTs to authenticate devices. See the <a
 * href="https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/iot/api-client/mqtt_example">MQTT
 * client example</a> for more information.
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

  // [START iot_create_registry]
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
  // [END iot_create_registry]

  // [START iot_delete_registry]
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
  // [END iot_delete_registry]

  // [START iot_list_devices]
  /** Print all of the devices in this registry to standard out. */
  public static void listDevices(String projectId, String cloudRegion, String registryName)
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

    List<Device> devices =
        service
            .projects()
            .locations()
            .registries()
            .devices()
            .list(registryPath)
            .execute()
            .getDevices();

    if (devices != null) {
      System.out.println("Found " + devices.size() + " devices");
      for (Device d : devices) {
        System.out.println("Id: " + d.getId());
        if (d.getConfig() != null) {
          // Note that this will show the device config in Base64 encoded format.
          System.out.println("Config: " + d.getConfig().toPrettyString());
        }
        System.out.println();
      }
    } else {
      System.out.println("Registry has no devices.");
    }
  }
  // [END iot_list_devices]

  // [START iot_create_es_device]
  /** Create a device that is authenticated using ES256. */
  public static void createDeviceWithEs256(
      String deviceId,
      String publicKeyFilePath,
      String projectId,
      String cloudRegion,
      String registryName)
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

    PublicKeyCredential publicKeyCredential = new PublicKeyCredential();
    final String key = Files.toString(new File(publicKeyFilePath), Charsets.UTF_8);
    publicKeyCredential.setKey(key);
    publicKeyCredential.setFormat("ES256_PEM");

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
  // [END iot_create_es_device]

  // [START iot_create_rsa_device]
  /** Create a device that is authenticated using RS256. */
  public static void createDeviceWithRs256(
      String deviceId,
      String certificateFilePath,
      String projectId,
      String cloudRegion,
      String registryName)
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
  // [END iot_create_rsa_device]

  // [START iot_create_unauth_device]
  /**
   * Create a device that has no credentials.
   *
   * <p>This is a valid way to construct a device, however until it is patched with a credential the
   * device will not be able to connect to Cloud IoT.
   */
  public static void createDeviceWithNoAuth(
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

    final String registryPath =
        "projects/" + projectId + "/locations/" + cloudRegion + "/registries/" + registryName;

    System.out.println("Creating device with id: " + deviceId);
    Device device = new Device();
    device.setId(deviceId);
    device.setCredentials(new ArrayList<DeviceCredential>());
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
  // [END iot_create_unauth_device]

  // [START iot_delete_device]
  /** Delete the given device from the registry. */
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
  // [END iot_delete_device]

  // [START iot_get_device]
  /** Retrieves device metadata from a registry. * */
  public static Device getDevice(
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

    System.out.println("Retrieving device " + devicePath);
    return service.projects().locations().registries().devices().get(devicePath).execute();
  }
  // [END iot_get_device]

  // [START iot_get_device_state]
  /** Retrieves device metadata from a registry. * */
  public static List<DeviceState> getDeviceStates(
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

    System.out.println("Retrieving device states " + devicePath);

    ListDeviceStatesResponse resp =
        service.projects().locations().registries().devices().states().list(devicePath).execute();

    return resp.getDeviceStates();
  }
  // [END iot_get_device_state]

  // [START iot_get_registry]
  /** Retrieves registry metadata from a project. * */
  public static DeviceRegistry getRegistry(
      String projectId, String cloudRegion, String registryName)
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

    return service.projects().locations().registries().get(registryPath).execute();
  }
  // [END iot_get_registry]

  // [START iot_get_device_configs]
  /** List all of the configs for the given device. */
  public static void listDeviceConfigs(
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

    System.out.println("Listing device configs for " + devicePath);
    List<DeviceConfig> deviceConfigs =
        service
            .projects()
            .locations()
            .registries()
            .devices()
            .configVersions()
            .list(devicePath)
            .execute()
            .getDeviceConfigs();

    for (DeviceConfig config : deviceConfigs) {
      System.out.println("Config version: " + config.getVersion());
      System.out.println("Contents: " + config.getBinaryData());
      System.out.println();
    }
  }
  // [END iot_get_device_configs]

  // [START iot_list_registries]
  /** Lists all of the registries associated with the given project. */
  public static void listRegistries(String projectId, String cloudRegion)
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

    List<DeviceRegistry> registries =
        service
            .projects()
            .locations()
            .registries()
            .list(projectPath)
            .execute()
            .getDeviceRegistries();

    if (registries != null) {
      System.out.println("Found " + registries.size() + " registries");
      for (DeviceRegistry r : registries) {
        System.out.println("Id: " + r.getId());
        System.out.println("Name: " + r.getName());
        if (r.getMqttConfig() != null) {
          System.out.println("Config: " + r.getMqttConfig().toPrettyString());
        }
        System.out.println();
      }
    } else {
      System.out.println("Project has no registries.");
    }
  }
  // [END iot_list_registries]

  // [START iot_patch_es]
  /** Patch the device to add an ES256 key for authentication. */
  public static void patchEs256ForAuth(
      String deviceId,
      String publicKeyFilePath,
      String projectId,
      String cloudRegion,
      String registryName)
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

    PublicKeyCredential publicKeyCredential = new PublicKeyCredential();
    String key = Files.toString(new File(publicKeyFilePath), Charsets.UTF_8);
    publicKeyCredential.setKey(key);
    publicKeyCredential.setFormat("ES256_PEM");

    DeviceCredential devCredential = new DeviceCredential();
    devCredential.setPublicKey(publicKeyCredential);

    Device device = new Device();
    device.setCredentials(Arrays.asList(devCredential));

    Device patchedDevice =
        service
            .projects()
            .locations()
            .registries()
            .devices()
            .patch(devicePath, device)
            .setUpdateMask("credentials")
            .execute();

    System.out.println("Patched device is " + patchedDevice.toPrettyString());
  }
  // [END iot_patch_es]

  // [START iot_patch_rsa]
  /** Patch the device to add an RSA256 key for authentication. */
  public static void patchRsa256ForAuth(
      String deviceId,
      String publicKeyFilePath,
      String projectId,
      String cloudRegion,
      String registryName)
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

    PublicKeyCredential publicKeyCredential = new PublicKeyCredential();
    String key = Files.toString(new File(publicKeyFilePath), Charsets.UTF_8);
    publicKeyCredential.setKey(key);
    publicKeyCredential.setFormat("RSA_X509_PEM");

    DeviceCredential devCredential = new DeviceCredential();
    devCredential.setPublicKey(publicKeyCredential);

    Device device = new Device();
    device.setCredentials(Arrays.asList(devCredential));

    Device patchedDevice =
        service
            .projects()
            .locations()
            .registries()
            .devices()
            .patch(devicePath, device)
            .setUpdateMask("credentials")
            .execute();

    System.out.println("Patched device is " + patchedDevice.toPrettyString());
  }
  // [END iot_patch_rsa]

  // [START iot_set_device_config]
  /** Set a device configuration to the specified data (string, JSON) and version (0 for latest). */
  public static void setDeviceConfiguration(
      String deviceId,
      String projectId,
      String cloudRegion,
      String registryName,
      String data,
      long version)
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

    ModifyCloudToDeviceConfigRequest req = new ModifyCloudToDeviceConfigRequest();
    req.setVersionToUpdate(version);

    // Data sent through the wire has to be base64 encoded.
    Base64.Encoder encoder = Base64.getEncoder();
    String encPayload = encoder.encodeToString(data.getBytes("UTF-8"));
    req.setBinaryData(encPayload);

    DeviceConfig config =
        service
            .projects()
            .locations()
            .registries()
            .devices()
            .modifyCloudToDeviceConfig(devicePath, req)
            .execute();

    System.out.println("Updated: " + config.getVersion());
  }
  // [END iot_set_device_config]

  // [START iot_get_iam_policy]
  /** Retrieves IAM permissions for the given registry. */
  public static void getIamPermissions(String projectId, String cloudRegion, String registryName)
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

    com.google.api.services.cloudiot.v1.model.Policy policy =
        service
            .projects()
            .locations()
            .registries()
            .getIamPolicy(registryPath, new GetIamPolicyRequest())
            .execute();

    System.out.println("Policy ETAG: " + policy.getEtag());

    if (policy.getBindings() != null) {
      for (com.google.api.services.cloudiot.v1.model.Binding binding : policy.getBindings()) {
        System.out.println(String.format("Role: %s", binding.getRole()));
        System.out.println("Binding members: ");
        for (String member : binding.getMembers()) {
          System.out.println(String.format("\t%s", member));
        }
      }
    } else {
      System.out.println(String.format("No policy bindings for %s", registryName));
    }
  }
  // [END iot_get_iam_policy]

  // [START iot_set_iam_policy]
  /** Sets IAM permissions for the given registry. */
  public static void setIamPermissions(
      String projectId, String cloudRegion, String registryName, String member, String role)
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

    com.google.api.services.cloudiot.v1.model.Policy policy =
        service
            .projects()
            .locations()
            .registries()
            .getIamPolicy(registryPath, new GetIamPolicyRequest())
            .execute();

    List<com.google.api.services.cloudiot.v1.model.Binding> bindings = policy.getBindings();

    boolean addNewRole = true;
    if (bindings != null) {
      for (com.google.api.services.cloudiot.v1.model.Binding binding : bindings) {
        if (binding.getRole().equals(role)) {
          List<String> members = binding.getMembers();
          members.add(member);
          binding.setMembers(members);
          addNewRole = false;
        }
      }
    } else {
      bindings = new ArrayList<>();
    }

    if (addNewRole) {
      com.google.api.services.cloudiot.v1.model.Binding bind =
          new com.google.api.services.cloudiot.v1.model.Binding();
      bind.setRole(role);
      List<String> members = new ArrayList<>();
      members.add(member);
      bind.setMembers(members);

      bindings.add(bind);
    }

    policy.setBindings(bindings);
    SetIamPolicyRequest req = new SetIamPolicyRequest().setPolicy(policy);

    policy = service.projects().locations().registries().setIamPolicy(registryPath, req).execute();

    System.out.println("Policy ETAG: " + policy.getEtag());
    for (com.google.api.services.cloudiot.v1.model.Binding binding : policy.getBindings()) {
      System.out.println(String.format("Role: %s", binding.getRole()));
      System.out.println("Binding members: ");
      for (String mem : binding.getMembers()) {
        System.out.println(String.format("\t%s", mem));
      }
    }
  }
  // [END iot_set_iam_policy]

  /** Send a command to a device. * */
  // [START send_command]
  public static void sendCommand(
      String deviceId, String projectId, String cloudRegion, String registryName, String data)
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
            .sendCommandToDevice(devicePath, req)
            .execute();

    System.out.println("Command response: " + res.toString());
  }
  // [END send_command]

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

    byte[] keyBytes = java.nio.file.Files.readAllBytes(Paths.get(certificateFilePath));
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

  /** Entry poit for CLI. */
  public static void main(String[] args) throws Exception {
    DeviceRegistryExampleOptions options = DeviceRegistryExampleOptions.fromFlags(args);
    if (options == null) {
      // Could not parse.
      return;
    }

    switch (options.command) {
      case "create-iot-topic":
        System.out.println("Create IoT Topic:");
        createIotTopic(options.projectId, options.pubsubTopic);
        break;
      case "create-es":
        System.out.println("Create ES Device:");
        createDeviceWithEs256(
            options.deviceId,
            options.ecPublicKeyFile,
            options.projectId,
            options.cloudRegion,
            options.registryName);
        break;
      case "create-rsa":
        System.out.println("Create RSA Device:");
        createDeviceWithRs256(
            options.deviceId,
            options.rsaCertificateFile,
            options.projectId,
            options.cloudRegion,
            options.registryName);
        break;
      case "create-unauth":
        System.out.println("Create Unauth Device");
        createDeviceWithNoAuth(
            options.deviceId, options.projectId, options.cloudRegion, options.registryName);
        break;
      case "create-registry":
        System.out.println("Create registry");
        createRegistry(
            options.cloudRegion, options.projectId, options.registryName, options.pubsubTopic);
        break;
      case "delete-device":
        System.out.println("Delete device");
        deleteDevice(
            options.deviceId, options.projectId, options.cloudRegion, options.registryName);
        break;
      case "delete-registry":
        System.out.println("Delete registry");
        deleteRegistry(options.cloudRegion, options.projectId, options.registryName);
        break;
      case "get-device":
        System.out.println("Get device");
        System.out.println(
            getDevice(
                    options.deviceId, options.projectId, options.cloudRegion, options.registryName)
                .toPrettyString());
        break;
      case "get-iam-permissions":
        System.out.println("Get iam permissions");
        getIamPermissions(options.projectId, options.cloudRegion, options.registryName);
        break;
      case "get-device-state":
        System.out.println("Get device state");
        List<DeviceState> states =
            getDeviceStates(
                options.deviceId, options.projectId, options.cloudRegion, options.registryName);
        for (DeviceState state : states) {
          System.out.println(state.toPrettyString());
        }
        break;
      case "get-registry":
        System.out.println("Get registry");
        System.out.println(
            getRegistry(options.projectId, options.cloudRegion, options.registryName));
        break;
      case "list-devices":
        System.out.println("List devices");
        listDevices(options.projectId, options.cloudRegion, options.registryName);
        break;
      case "list-registries":
        System.out.println("List registries");
        listRegistries(options.projectId, options.cloudRegion);
        break;
      case "patch-device-es":
        System.out.println("Patch device with ES");
        patchEs256ForAuth(
            options.deviceId,
            options.ecPublicKeyFile,
            options.projectId,
            options.cloudRegion,
            options.registryName);
        break;
      case "patch-device-rsa":
        System.out.println("Patch device with RSA");
        patchRsa256ForAuth(
            options.deviceId,
            options.rsaCertificateFile,
            options.projectId,
            options.cloudRegion,
            options.registryName);
        break;
      case "set-config":
        if (options.deviceId == null) {
          System.out.println("Specify device_id for the device you are updating.");
        } else {
          System.out.println("Setting device configuration");
          setDeviceConfiguration(
              options.deviceId,
              options.projectId,
              options.cloudRegion,
              options.registryName,
              options.configuration,
              options.version);
        }
        break;
      case "set-iam-permissions":
        if (options.member == null || options.role == null) {
          System.out.println("Specify member and role for the policy you are updating.");
        } else {
          System.out.println("Setting iam permissions");
          setIamPermissions(
              options.projectId,
              options.cloudRegion,
              options.registryName,
              options.member,
              options.role);
        }
        break;
      case "send-command":
        System.out.println("Sending command to device:");
        sendCommand(
            options.deviceId,
            options.projectId,
            options.cloudRegion,
            options.registryName,
            options.commandData);
        break;
      default:
        String header = "Cloud IoT Core Commandline Example (Device / Registry management): \n\n";
        String footer = "\nhttps://cloud.google.com/iot-core";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("DeviceRegistryExample", header, options.options, footer, true);
        break;
    }
  }
}
