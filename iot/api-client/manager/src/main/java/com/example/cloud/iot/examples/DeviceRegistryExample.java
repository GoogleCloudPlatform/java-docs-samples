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
import com.google.api.services.cloudiot.v1.model.ListDevicesResponse;
import com.google.api.services.cloudiot.v1.model.ModifyCloudToDeviceConfigRequest;
import com.google.api.services.cloudiot.v1.model.PublicKeyCredential;
import com.google.api.services.cloudiot.v1.model.SendCommandToDeviceRequest;
import com.google.api.services.cloudiot.v1.model.SetIamPolicyRequest;
import com.google.api.services.cloudiot.v1.model.UnbindDeviceFromGatewayRequest;
import com.google.api.services.cloudiot.v1.model.UnbindDeviceFromGatewayResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Role;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.common.io.Files;
import com.google.iam.v1.Binding;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.Topic;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import org.apache.commons.cli.HelpFormatter;

/**
 * Example of using Cloud IoT device manager API to administer devices, registries and projects.
 *
 * This example uses the Device Manager API to create, retrieve, disable, list and delete Cloud
 * IoT devices and registries, using both RSA and eliptic curve keys for authentication.
 *
 * To start, follow the instructions on the Developer Guide at cloud.google.com/iot to create a
 * service_account.json file and Cloud Pub/Sub topic as discussed in the guide. You will then need
 * to point to the service_account.json file as described in
 * https://developers.google.com/identity/protocols/application-default-credentials#howtheywork
 *
 * Before running the example, we have to create private and public keys, as described in
 * cloud.google.com/iot. Since we are interacting with the device manager, we will only use the
 * public keys. The private keys are used to sign JWTs to authenticate devices. See the <a
 * href="https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/iot/api-client/mqtt_example">MQTT
 * client example</a> for more information.
 *
 * Finally, compile and run the example with:
 *
 * <pre>
 * <code>
 * $ mvn clean compile assembly:single
 * mvn exec:exec -Dmanager \
 *               -Dcr=--cloud_region=us-central1 \
 *               -Dproj=--project_id=blue-jet-123 \
 *               -Drname=--registry_name=my-registry \
 *               -Dcmd=list-devices
 * </code>
 * </pre>
 */
public class DeviceRegistryExample {

  static final String APP_NAME = "DeviceRegistryExample";

  /** Creates a topic and grants the IoT service account access. */
  protected static Topic createIotTopic(String projectId, String topicId) throws Exception {
    // Create a new topic
    final ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      final Topic topic = topicAdminClient.createTopic(topicName);
      final String topicString = topicName.toString();
      // add role -> members binding
      // create updated policy
      topicAdminClient.setIamPolicy(
          topicString,
          com.google.iam.v1.Policy.newBuilder(topicAdminClient.getIamPolicy(topicString))
              .addBindings(
                  Binding.newBuilder()
                      .addMembers("serviceAccount:cloud-iot@system.gserviceaccount.com")
                      .setRole(Role.owner().toString())
                      .build())
              .build());

      System.out.println("Setup topic / policy for: " + topic.getName());
      return topic;
    }
  }

  // [START iot_create_registry]
  /** Create a registry for Cloud IoT. */
  protected static void createRegistry(
      String cloudRegion, String projectId, String registryName, String pubsubTopicPath)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
  protected static void deleteRegistry(String cloudRegion, String projectId, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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

  /**
   * clearRegistry
   *
   * <ul>
   *   <li>Registries can't be deleted if they contain devices,
   *   <li>Gateways (a type of device) can't be deleted if they have bound devices
   *   <li>Devices can't be deleted if bound to gateways...
   * </ul>
   *
   * To completely remove a registry, you must unbind all devices from gateways, then remove all
   * devices in a registry before removing the registry. As pseudocode: <code>
   *   ForAll gateways
   *     ForAll devicesBoundToGateway
   *       unbindDeviceFromGateway
   *   ForAll devices
   *     Delete device by ID
   *   Delete registry
   *  </code>
   */
  // [START iot_clear_registry]
  protected static void clearRegistry(String cloudRegion, String projectId, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
    final CloudIot service =
        new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init)
            .setApplicationName(APP_NAME)
            .build();
    final String registryPath =
        String.format(
            "projects/%s/locations/%s/registries/%s", projectId, cloudRegion, registryName);

    CloudIot.Projects.Locations.Registries regAlias = service.projects().locations().registries();
    CloudIot.Projects.Locations.Registries.Devices devAlias = regAlias.devices();

    ListDevicesResponse listGatewaysRes =
        devAlias.list(registryPath).setGatewayListOptionsGatewayType("GATEWAY").execute();
    List<Device> gateways = listGatewaysRes.getDevices();

    // Unbind all devices from all gateways
    if (gateways != null) {
      System.out.println("Found " + gateways.size() + " devices");
      for (Device g : gateways) {
        String gatewayId = g.getId();
        System.out.println("Id: " + gatewayId);

        ListDevicesResponse res =
            devAlias
                .list(registryPath)
                .setGatewayListOptionsAssociationsGatewayId(gatewayId)
                .execute();
        List<Device> deviceNumIds = res.getDevices();

        if (deviceNumIds != null) {
          System.out.println("Found " + deviceNumIds.size() + " devices");
          for (Device device : deviceNumIds) {
            String deviceId = device.getId();
            System.out.println(String.format("ID: %s", deviceId));

            // Remove any bindings from the device
            UnbindDeviceFromGatewayRequest request = new UnbindDeviceFromGatewayRequest();
            request.setDeviceId(deviceId);
            request.setGatewayId(gatewayId);
            regAlias.unbindDeviceFromGateway(registryPath, request).execute();
          }
        } else {
          System.out.println("Gateway has no bound devices.");
        }
      }
    }

    // Remove all devices from the regsitry
    List<Device> devices = devAlias.list(registryPath).execute().getDevices();

    if (devices != null) {
      System.out.println("Found " + devices.size() + " devices");
      for (Device d : devices) {
        String deviceId = d.getId();
        String devicePath = String.format("%s/devices/%s", registryPath, deviceId);
        service.projects().locations().registries().devices().delete(devicePath).execute();
      }
    }

    // Delete the registry
    service.projects().locations().registries().delete(registryPath).execute();
  }
  // [END iot_clear_registry]

  // [START iot_list_devices]
  /** Print all of the devices in this registry to standard out. */
  protected static void listDevices(String projectId, String cloudRegion, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
  protected static void createDeviceWithEs256(
      String deviceId,
      String publicKeyFilePath,
      String projectId,
      String cloudRegion,
      String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
    device.setCredentials(Collections.singletonList(devCredential));

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
  protected static void createDeviceWithRs256(
      String deviceId,
      String certificateFilePath,
      String projectId,
      String cloudRegion,
      String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
    device.setCredentials(Collections.singletonList(devCredential));
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
   * This is a valid way to construct a device, however until it is patched with a credential the
   * device will not be able to connect to Cloud IoT.
   */
  protected static void createDeviceWithNoAuth(
      String deviceId, String projectId, String cloudRegion, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
  protected static void deleteDevice(
      String deviceId, String projectId, String cloudRegion, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
  protected static Device getDevice(
      String deviceId, String projectId, String cloudRegion, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
  protected static List<DeviceState> getDeviceStates(
      String deviceId, String projectId, String cloudRegion, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
  protected static DeviceRegistry getRegistry(
      String projectId, String cloudRegion, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
  protected static void listDeviceConfigs(
      String deviceId, String projectId, String cloudRegion, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
  protected static void listRegistries(String projectId, String cloudRegion)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
  protected static void patchEs256ForAuth(
      String deviceId,
      String publicKeyFilePath,
      String projectId,
      String cloudRegion,
      String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
    device.setCredentials(Collections.singletonList(devCredential));

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
  protected static void patchRsa256ForAuth(
      String deviceId,
      String publicKeyFilePath,
      String projectId,
      String cloudRegion,
      String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
    device.setCredentials(Collections.singletonList(devCredential));

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
  protected static void setDeviceConfiguration(
      String deviceId,
      String projectId,
      String cloudRegion,
      String registryName,
      String data,
      long version)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
    String encPayload = encoder.encodeToString(data.getBytes(StandardCharsets.UTF_8.name()));
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
  protected static void getIamPermissions(String projectId, String cloudRegion, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
  protected static void setIamPermissions(
      String projectId, String cloudRegion, String registryName, String member, String role)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
  // [START iot_send_command]
  protected static void sendCommand(
      String deviceId, String projectId, String cloudRegion, String registryName, String data)
      throws GeneralSecurityException, IOException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
    String encPayload = encoder.encodeToString(data.getBytes(StandardCharsets.UTF_8.name()));
    req.setBinaryData(encPayload);
    System.out.printf("Sending command to %s%n", devicePath);

    service
        .projects()
        .locations()
        .registries()
        .devices()
        .sendCommandToDevice(devicePath, req)
        .execute();

    System.out.println("Command response: sent");
  }
  // [END iot_send_command]

  protected static void bindDeviceToGateway(
      String projectId, String cloudRegion, String registryName, String deviceId, String gatewayId)
      throws GeneralSecurityException, IOException {
    // [START iot_bind_device_to_gateway]
    createDevice(projectId, cloudRegion, registryName, deviceId);

    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
    // [END iot_bind_device_to_gateway]
  }

  protected static void unbindDeviceFromGateway(
      String projectId, String cloudRegion, String registryName, String deviceId, String gatewayId)
      throws GeneralSecurityException, IOException {
    // [START iot_unbind_device_from_gateway]
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
    // [END iot_unbind_device_from_gateway]
  }

  /** Create a device to bind to a gateway. */
  protected static void createDevice(
      String projectId, String cloudRegion, String registryName, String deviceId)
      throws GeneralSecurityException, IOException {
    // [START iot_create_device]
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
    // [END iot_create_device]
  }

  /** Create a gateway to bind devices to. */
  protected static void createGateway(
      String projectId,
      String cloudRegion,
      String registryName,
      String gatewayId,
      String certificateFilePath,
      String algorithm)
      throws GeneralSecurityException, IOException {
    // [START iot_create_gateway]
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
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
    if ("ES256".equals(algorithm)) {
      keyFormat = "ES256_PEM";
    }

    PublicKeyCredential publicKeyCredential = new PublicKeyCredential();

    byte[] keyBytes = java.nio.file.Files.readAllBytes(Paths.get(certificateFilePath));
    publicKeyCredential.setKey(new String(keyBytes, StandardCharsets.US_ASCII));
    publicKeyCredential.setFormat(keyFormat);
    DeviceCredential deviceCredential = new DeviceCredential();
    deviceCredential.setPublicKey(publicKeyCredential);

    device.setGatewayConfig(gwConfig);
    device.setCredentials(Collections.singletonList(deviceCredential));
    Device createdDevice =
        service
            .projects()
            .locations()
            .registries()
            .devices()
            .create(registryPath, device)
            .execute();

    System.out.println("Created gateway: " + createdDevice.toPrettyString());
    // [END iot_create_gateway]
  }

  protected static void listGateways(String projectId, String cloudRegion, String registryName)
      throws IOException, GeneralSecurityException {
    // [START iot_list_gateways]
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
    final CloudIot service =
        new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init)
            .setApplicationName(APP_NAME)
            .build();

    final String registryPath =
        String.format(
            "projects/%s/locations/%s/registries/%s", projectId, cloudRegion, registryName);

    List<Device> gateways =
        service
            .projects()
            .locations()
            .registries()
            .devices()
            .list(registryPath)
            .setGatewayListOptionsGatewayType("GATEWAY")
            .execute()
            .getDevices();

    if (gateways != null) {
      System.out.println("Found " + gateways.size() + " devices");
      for (Device d : gateways) {
        System.out.println("Id: " + d.getId());
        if (d.getConfig() != null) {
          // Note that this will show the device config in Base64 encoded format.
          System.out.println("Config: " + d.getGatewayConfig().toPrettyString());
        }
        System.out.println();
      }
    } else {
      System.out.println("Registry has no devices.");
    }
    // [END iot_list_gateways]
  }

  /** List devices bound to a gateway. */
  protected static void listDevicesForGateway(
      String projectId, String cloudRegion, String registryName, String gatewayId)
      throws IOException, GeneralSecurityException {
    // [START iot_list_devices_for_gateway]
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
    final CloudIot service =
        new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init)
            .setApplicationName(APP_NAME)
            .build();

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
    // [END iot_list_devices_for_gateway]
  }

  /** Entry poit for CLI. */
  protected static void mainCreate(DeviceRegistryExampleOptions options) throws Exception {
    if ("create-iot-topic".equals(options.command)) {
      System.out.println("Create IoT Topic:");
      createIotTopic(options.projectId, options.pubsubTopic);
    } else if ("create-es".equals(options.command)) {
      System.out.println("Create ES Device:");
      createDeviceWithEs256(
          options.deviceId,
          options.ecPublicKeyFile,
          options.projectId,
          options.cloudRegion,
          options.registryName);
    } else if ("create-rsa".equals(options.command)) {
      System.out.println("Create RSA Device:");
      createDeviceWithRs256(
          options.deviceId,
          options.rsaCertificateFile,
          options.projectId,
          options.cloudRegion,
          options.registryName);
    } else if ("create-unauth".equals(options.command)) {
      System.out.println("Create Unauth Device");
      createDeviceWithNoAuth(
          options.deviceId, options.projectId, options.cloudRegion, options.registryName);
    } else if ("create-registry".equals(options.command)) {
      System.out.println("Create registry");
      createRegistry(
          options.cloudRegion, options.projectId, options.registryName, options.pubsubTopic);
    } else if ("create-gateway".equals(options.command)) {
      System.out.println("Bind device to gateway:");
      String algorithm = "ES256";
      String certificateFilePath = options.ecPublicKeyFile;
      if (options.rsaCertificateFile != null) {
        algorithm = "RS256";
        certificateFilePath = options.rsaCertificateFile;
      }

      createGateway(
          options.projectId,
          options.cloudRegion,
          options.registryName,
          options.gatewayId,
          certificateFilePath,
          algorithm);
    }
  }

  protected static void mainGet(DeviceRegistryExampleOptions options) throws Exception {
    if ("get-device".equals(options.command)) {
      System.out.println("Get device");
      System.out.println(
          getDevice(options.deviceId, options.projectId, options.cloudRegion, options.registryName)
              .toPrettyString());
    } else if ("get-iam-permissions".equals(options.command)) {
      System.out.println("Get iam permissions");
      getIamPermissions(options.projectId, options.cloudRegion, options.registryName);
    } else if ("get-device-state".equals(options.command)) {
      System.out.println("Get device state");
      List<DeviceState> states =
          getDeviceStates(
              options.deviceId, options.projectId, options.cloudRegion, options.registryName);
      for (DeviceState state : states) {
        System.out.println(state.toPrettyString());
      }
    } else if ("get-registry".equals(options.command)) {
      System.out.println("Get registry");
      System.out.println(getRegistry(options.projectId, options.cloudRegion, options.registryName));
    }
  }

  public static void main(String[] args) throws Exception {
    DeviceRegistryExampleOptions options = DeviceRegistryExampleOptions.fromFlags(args);
    if (options == null) {
      // Could not parse.
      System.out.println("Issue parsing the options");
      return;
    }

    if (options.command.startsWith("create")) {
      mainCreate(options);
    }

    if (options.command.startsWith("get")) {
      mainGet(options);
    }

    if ("clear-registry".equals(options.command)) {
      System.out.println("Clear registry");
      clearRegistry(options.cloudRegion, options.projectId, options.registryName);
    } else if ("delete-device".equals(options.command)) {
      System.out.println("Delete device");
      deleteDevice(options.deviceId, options.projectId, options.cloudRegion, options.registryName);
    } else if ("delete-registry".equals(options.command)) {
      System.out.println("Delete registry");
      deleteRegistry(options.cloudRegion, options.projectId, options.registryName);
    } else if ("list-devices".equals(options.command)) {
      System.out.println("List devices");
      listDevices(options.projectId, options.cloudRegion, options.registryName);
    } else if ("list-registries".equals(options.command)) {
      System.out.println("List registries");
      listRegistries(options.projectId, options.cloudRegion);
    } else if ("patch-device-es".equals(options.command)) {
      System.out.println("Patch device with ES");
      patchEs256ForAuth(
          options.deviceId,
          options.ecPublicKeyFile,
          options.projectId,
          options.cloudRegion,
          options.registryName);
    } else if ("patch-device-rsa".equals(options.command)) {
      System.out.println("Patch device with RSA");
      patchRsa256ForAuth(
          options.deviceId,
          options.rsaCertificateFile,
          options.projectId,
          options.cloudRegion,
          options.registryName);
    } else if ("set-config".equals(options.command)) {
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
    } else if ("set-iam-permissions".equals(options.command)) {
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
    } else if ("bind-device-to-gateway".equals(options.command)) {
      System.out.println("Bind device to gateway:");
      bindDeviceToGateway(
          options.projectId,
          options.cloudRegion,
          options.registryName,
          options.deviceId,
          options.gatewayId);
    } else if ("unbind-device-from-gateway".equals(options.command)) {
      System.out.println("Unbind device from gateway:");
      unbindDeviceFromGateway(
          options.projectId,
          options.cloudRegion,
          options.registryName,
          options.deviceId,
          options.gatewayId);
    } else if ("list-gateways".equals(options.command)) {
      System.out.println("Listing gateways: ");
      listGateways(options.projectId, options.cloudRegion, options.registryName);
    } else if ("list-devices-for-gateway".equals(options.command)) {
      System.out.println("Listing devices for a gateway: ");
      listDevicesForGateway(
          options.projectId, options.cloudRegion, options.registryName, options.gatewayId);
    } else if ("send-command".equals(options.command)) {
      System.out.println("Sending command to device:");
      sendCommand(
          options.deviceId,
          options.projectId,
          options.cloudRegion,
          options.registryName,
          options.commandData);
    } else {
      String header = "Cloud IoT Core Commandline Example (Device / Registry management): \n\n";
      String footer = "\nhttps://cloud.google.com/iot-core";

      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("DeviceRegistryExample", header, options.options, footer, true);
    }
  }
}
