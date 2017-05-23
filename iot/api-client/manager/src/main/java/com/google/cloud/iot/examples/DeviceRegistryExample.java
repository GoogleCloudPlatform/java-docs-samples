/**
 * Copyright 2017, Google, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.iot.examples;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Charsets;
import com.google.api.services.cloudiot.v1beta1.CloudIot;
import com.google.api.services.cloudiot.v1beta1.CloudIotScopes;
import com.google.api.services.cloudiot.v1beta1.model.Device;
import com.google.api.services.cloudiot.v1beta1.model.DeviceConfig;
import com.google.api.services.cloudiot.v1beta1.model.DeviceConfigData;
import com.google.api.services.cloudiot.v1beta1.model.DeviceCredential;
import com.google.api.services.cloudiot.v1beta1.model.DeviceRegistry;
import com.google.api.services.cloudiot.v1beta1.model.ModifyCloudToDeviceConfigRequest;
import com.google.api.services.cloudiot.v1beta1.model.NotificationConfig;
import com.google.api.services.cloudiot.v1beta1.model.PublicKeyCredential;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

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
  // Service for administering Cloud IoT Core devices, registries and projects.
  private CloudIot service;
  // Path to the project and location: "projects/my-project-id/locations/us-central1"
  private String projectPath;
  // Path to the registry: "projects/my-project-id/location/us-central1/registries/my-registry-id"
  private String registryPath;

  /** Construct a new registry with the given name and pubsub topic inside the given project. */
  public DeviceRegistryExample(
      String projectId, String location, String registryName, String pubsubTopicPath)
      throws IOException, GeneralSecurityException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    service = new CloudIot(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init);
    projectPath = "projects/" + projectId + "/locations/" + location;
    registryPath = projectPath + "/registries/" + registryName;

    NotificationConfig notificationConfig = new NotificationConfig();
    notificationConfig.setPubsubTopicName(pubsubTopicPath);

    DeviceRegistry registry = new DeviceRegistry();
    registry.setEventNotificationConfig(notificationConfig);
    registry.setId(registryName);
    service.projects().locations().registries().create(projectPath, registry).execute();
  }

  /** Delete this registry from Cloud IoT. */
  public void delete() throws IOException {
    System.out.println("Deleting: " + registryPath);
    service.projects().locations().registries().delete(registryPath).execute();
  }

  /** Print all of the devices in this registry to standard out. */
  public void listDevices() throws IOException {
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

  private DeviceCredential createDeviceCredential(String publicKeyFilePath, String keyFormat)
      throws IOException {
    PublicKeyCredential publicKeyCredential = new PublicKeyCredential();
    String key = Files.toString(new File(publicKeyFilePath), Charsets.UTF_8);
    publicKeyCredential.setKey(key);
    publicKeyCredential.setFormat(keyFormat);

    DeviceCredential credential = new DeviceCredential();
    credential.setPublicKey(publicKeyCredential);
    return credential;
  }

  /**
   * Create a new device in this registry with the given id. The device will be authenticated with
   * the given public key file.
   */
  private void createDevice(String deviceId, List<DeviceCredential> credentials)
      throws IOException {
    System.out.println("Creating device with id: " + deviceId);
    Device device = new Device();
    device.setId(deviceId);
    device.setCredentials(credentials);
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

  /** Create a device that is authenticated using RS256. */
  public void createDeviceWithRs256(String deviceId, String certificateFilePath)
      throws IOException {
    createDevice(
        deviceId, Arrays.asList(createDeviceCredential(certificateFilePath, "RSA_X509_PEM")));
  }

  /** Create a device that is authenticated using ES256. */
  public void createDeviceWithEs256(String deviceId, String publicKeyFilePath) throws IOException {
    createDevice(deviceId, Arrays.asList(createDeviceCredential(publicKeyFilePath, "ES256_PEM")));
  }

  /**
   * Create a device that has no credentials.
   *
   * <p>This is a valid way to construct a device, however until it is patched with a credential the
   * device will not be able to connect to Cloud IoT.
   */
  public void createDeviceWithNoAuth(String deviceId) throws IOException {
    createDevice(deviceId, new ArrayList<DeviceCredential>());
  }

  /** Patch the device to add an ES256 key for authentication. */
  public void patchEs256ForAuth(String deviceId, String publicKeyFilePath) throws IOException {
    String devicePath = registryPath + "/devices/" + deviceId;
    PublicKeyCredential publicKeyCredential = new PublicKeyCredential();
    String key = Files.toString(new File(publicKeyFilePath), Charsets.UTF_8);
    publicKeyCredential.setKey(key);
    publicKeyCredential.setFormat("ES256_PEM");

    DeviceCredential credential = new DeviceCredential();
    credential.setPublicKey(publicKeyCredential);

    Device device = new Device();
    device.setCredentials(Arrays.asList(credential));

    Device patchedDevice =
        service
            .projects()
            .locations()
            .registries()
            .devices()
            .patch(devicePath, device)
            .setFields("credentials")
            .execute();

    System.out.println("Patched device is " + patchedDevice.toPrettyString());
  }

  /** Delete the given device from the registry. */
  public void deleteDevice(String deviceId) throws IOException {
    String devicePath = registryPath + "/devices/" + deviceId;
    System.out.println("Deleting device " + devicePath);
    service.projects().locations().registries().devices().delete(devicePath).execute();
  }

  /** List all of the configs for the given device. */
  public void listDeviceConfigs(String deviceId) throws IOException {
    String devicePath = registryPath + "/devices/" + deviceId;
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
      System.out.println("Contents: " + config.getData().getBinaryData());
      System.out.println();
    }
  }

  /** Modify the latest cloud to device config for the given device, with the config data. */
  public void modifyCloudToDeviceConfig(String deviceId, String configData) throws IOException {
    String devicePath = registryPath + "/devices/" + deviceId;
    ModifyCloudToDeviceConfigRequest request = new ModifyCloudToDeviceConfigRequest();
    DeviceConfigData data = new DeviceConfigData();
    data.setBinaryData(DatatypeConverter.printBase64Binary(configData.getBytes(Charsets.UTF_8)));
    request.setVersionToUpdate(0L); // 0L indicates update all versions
    request.setData(data);
    DeviceConfig config =
        service
            .projects()
            .locations()
            .registries()
            .devices()
            .modifyCloudToDeviceConfig(devicePath, request)
            .execute();

    System.out.println("Created device config: " + config.toPrettyString());
  }

  public static void main(String[] args) throws IOException, GeneralSecurityException {
    DeviceRegistryExampleOptions options = DeviceRegistryExampleOptions.fromFlags(args);
    if (options == null) {
      // Could not parse.
      return;
    }

    // Simple example of interacting with the Cloud IoT API.
    String registryName = "cloudiot_device_manager_example_registry_" + System.currentTimeMillis();

    // Create a new registry with the above name.
    DeviceRegistryExample registry =
        new DeviceRegistryExample(
            options.projectId, options.cloudRegion, registryName, options.pubsubTopic);

    // List the devices in the registry. Since we haven't created any yet, this should be empty.
    registry.listDevices();

    // Create a device that is authenticated using RSA.
    String rs256deviceId = "rs256-device";
    registry.createDeviceWithRs256(rs256deviceId, options.rsaCertificateFile);

    // Create a device without an authentication credential. We'll patch it to use elliptic curve
    // cryptography.
    String es256deviceId = "es256-device";
    registry.createDeviceWithNoAuth(es256deviceId);

    // List the devices again. This should show the above two devices.
    registry.listDevices();

    // Give the device without an authentication credential an elliptic curve credential.
    registry.patchEs256ForAuth(es256deviceId, options.ecPublicKeyFile);

    // List the devices in the registry again, still showing the two devices.
    registry.listDevices();

    // List the device configs for the RSA authenticated device. Since we haven't pushed any, this
    // list will only contain the default empty config.
    registry.listDeviceConfigs(rs256deviceId);

    // Push two new configs to the device.
    registry.modifyCloudToDeviceConfig(rs256deviceId, "config v1");
    registry.modifyCloudToDeviceConfig(rs256deviceId, "config v2");

    // List the configs again. This will show the two configs that we just pushed.
    registry.listDeviceConfigs(rs256deviceId);

    // Delete the elliptic curve device.
    registry.deleteDevice(es256deviceId);

    // Since we deleted the elliptic curve device, this will only show the RSA device.
    registry.listDevices();

    try {
      // Try to delete the registry. However, since the registry is not empty, this will fail and
      // throw an exception.
      registry.delete();
    } catch (IOException e) {
      System.out.println("Exception: " + e.getMessage());
    }

    // Delete the RSA device. The registry is now empty.
    registry.deleteDevice(rs256deviceId);

    // Since the registry has no devices in it, the delete will succeed.
    registry.delete();
  }
}
