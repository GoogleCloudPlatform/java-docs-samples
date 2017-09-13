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

package com.example.cloud.iot.examples;

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
import com.google.cloud.Role;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.common.io.Files;
import com.google.iam.v1.Binding;
import com.google.iam.v1.Policy;
import com.google.pubsub.v1.Topic;
import com.google.pubsub.v1.TopicName;

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

  /** Creates a topic and grants the IoT service account access. */
  public static void createIotTopic(String projectId, String topicId) throws Exception {
    // Create a new topic
    final TopicName topicName = TopicName.create(projectId, topicId);

    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      final Topic topic = topicAdminClient.createTopic(topicName);
      Policy policy = topicAdminClient.getIamPolicy(topicName.toString());
      // add role -> members binding
      Binding binding =
          Binding.newBuilder()
              .addMembers("serviceAccount:cloud-iot@system.gserviceaccount.com")
              .setRole(Role.owner().toString())
              .build();
      // create updated policy
      Policy updatedPolicy = Policy.newBuilder(policy).addBindings(binding).build();
      topicAdminClient.setIamPolicy(topicName.toString(), updatedPolicy);

      System.out.println("Setup topic / policy for: " + topic.getName());
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
    final CloudIot service = new CloudIot(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
        init);

    final String projectPath = "projects/" + projectId + "/locations/" + cloudRegion;
    final String fullPubsubPath = "projects/" + projectId + "/topics/" + pubsubTopicPath;

    DeviceRegistry registry = new DeviceRegistry();
    NotificationConfig notificationConfig = new NotificationConfig();
    notificationConfig.setPubsubTopicName(fullPubsubPath);
    registry.setEventNotificationConfig(notificationConfig);
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
    final CloudIot service = new CloudIot(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
        init);

    String projectPath = "projects/" + projectId + "/locations/" + cloudRegion;
    String registryPath = projectPath + "/registries/" + registryName;

    System.out.println("Deleting: " + registryPath);
    service.projects().locations().registries().delete(registryPath).execute();
  }

  /** Print all of the devices in this registry to standard out. */
  public static void listDevices(String projectId, String cloudRegion, String registryName) throws
      GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
        init);

    String registryPath = "projects/" + projectId + "/locations/" + cloudRegion + "/registries/"
        + registryName;

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

  /** Create a device that is authenticated using ES256. */
  public static void createDeviceWithEs256(String deviceId, String publicKeyFilePath,
      String projectId, String cloudRegion, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
        init);

    final String registryPath = "projects/" + projectId + "/locations/" + cloudRegion
        + "/registries/" + registryName;

    PublicKeyCredential publicKeyCredential = new PublicKeyCredential();
    final String key = Files.toString(new File(publicKeyFilePath), Charsets.UTF_8);
    publicKeyCredential.setKey(key);
    publicKeyCredential.setFormat("ES256_PEM");

    DeviceCredential devCredential  = new DeviceCredential();
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

  /** Create a device that is authenticated using RS256. */
  public static void createDeviceWithRs256(String deviceId, String certificateFilePath,
                                           String projectId, String cloudRegion,
                                           String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
        init);

    final String registryPath = "projects/" + projectId + "/locations/" + cloudRegion
        + "/registries/" + registryName;

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

  /**
   * Create a device that has no credentials.
   *
   * <p>This is a valid way to construct a device, however until it is patched with a credential the
   * device will not be able to connect to Cloud IoT.
   */
  public static void createDeviceWithNoAuth(String deviceId, String projectId, String cloudRegion,
                                            String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
        init);

    final String registryPath = "projects/" + projectId + "/locations/" + cloudRegion
        + "/registries/" + registryName;

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

  /** Delete the given device from the registry. */
  public static void deleteDevice(String deviceId, String projectId, String cloudRegion,
                                  String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
        init);

    final String registryPath = "projects/" + projectId + "/locations/" + cloudRegion
        + "/registries/" + registryName;

    final String devicePath = registryPath + "/devices/" + deviceId;
    System.out.println("Deleting device " + devicePath);
    service.projects().locations().registries().devices().delete(devicePath).execute();
  }

  /** Retrieves device metadata from a registry. **/
  public static Device getDevice(String deviceId, String projectId, String cloudRegion,
                                 String registryName) throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    CloudIot service = new CloudIot(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
        init);

    String registryPath = "projects/" + projectId + "/locations/" + cloudRegion + "/registries/"
        + registryName;

    String devicePath = registryPath + "/devices/" + deviceId;
    System.out.println("Retrieving device " + devicePath);
    return service.projects().locations().registries().devices().get(devicePath).execute();
  }

  /** Retrieves registry metadata from a project. **/
  public static DeviceRegistry getRegistry(
      String projectId, String cloudRegion, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
        init);

    final String registryPath = "projects/" + projectId + "/locations/" + cloudRegion
        + "/registries/" + registryName;
    return service.projects().locations().registries().get(registryPath).execute();
  }

  /** List all of the configs for the given device. */
  public static void listDeviceConfigs(
      String deviceId, String projectId, String cloudRegion, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
        init);

    final String registryPath = "projects/" + projectId + "/locations/" + cloudRegion
        + "/registries/" + registryName;

    final String devicePath = registryPath + "/devices/" + deviceId;
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

  /** Lists all of the registries associated with the given project. */
  public static void listRegistries(String projectId, String cloudRegion)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
        init);

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
      for (DeviceRegistry r: registries) {
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

  /** Modify the latest cloud to device config for the given device, with the config data. */
  public static void modifyCloudToDeviceConfig(String deviceId, String configData, String projectId,
                                        String cloudRegion, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
        init);

    final String registryPath = "projects/" + projectId + "/locations/" + cloudRegion
        + "/registries/" + registryName;
    final String devicePath = registryPath + "/devices/" + deviceId;
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

  /** Patch the device to add an ES256 key for authentication. */
  public static void patchEs256ForAuth(String deviceId, String publicKeyFilePath, String projectId,
                                       String cloudRegion, String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
        init);

    final String registryPath = "projects/" + projectId + "/locations/" + cloudRegion
        + "/registries/" + registryName;

    final String devicePath = registryPath + "/devices/" + deviceId;
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
            .setFields("credentials")
            .execute();

    System.out.println("Patched device is " + patchedDevice.toPrettyString());
  }

  /** Patch the device to add an RSA256 key for authentication. */
  public static void patchRsa256ForAuth(String deviceId, String publicKeyFilePath, String projectId,
                                        String cloudRegion,
                                        String registryName)
      throws GeneralSecurityException, IOException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new RetryHttpInitializerWrapper(credential);
    final CloudIot service = new CloudIot(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
        init);

    final String registryPath = "projects/" + projectId + "/locations/" + cloudRegion
        + "/registries/" + registryName;

    final String devicePath = registryPath + "/devices/" + deviceId;
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
            .setFields("credentials")
            .execute();

    System.out.println("Patched device is " + patchedDevice.toPrettyString());
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
        createDeviceWithEs256(options.deviceId, options.ecPublicKeyFile, options.projectId,
            options.cloudRegion, options.registryName);
        break;
      case "create-rsa":
        System.out.println("Create RSA Device:");
        createDeviceWithRs256(options.deviceId, options.rsaCertificateFile, options.projectId,
            options.cloudRegion, options.registryName);
        break;
      case "create-unauth":
        System.out.println("Create Unauth Device");
        createDeviceWithNoAuth(options.deviceId, options.projectId, options.cloudRegion,
            options.registryName);
        break;
      case "create-registry":
        System.out.println("Create registry");
        createRegistry(options.cloudRegion, options.projectId, options.registryName,
            options.pubsubTopic);
        break;
      case "delete-device":
        System.out.println("Delete device");
        deleteDevice(options.deviceId, options.projectId, options.cloudRegion,
            options.registryName);
        break;
      case "delete-registry":
        System.out.println("Delete registry");
        deleteRegistry(options.cloudRegion, options.projectId, options.registryName);
        break;
      case "get-device":
        System.out.println("Get device");
        System.out.println(getDevice(options.deviceId, options.projectId, options.cloudRegion,
            options.registryName)
            .toPrettyString());
        break;
      case "get-registry":
        System.out.println("Get registry");
        System.out.println(getRegistry(options.projectId, options.cloudRegion,
            options.registryName));
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
        patchEs256ForAuth(options.deviceId, options.ecPublicKeyFile, options.projectId,
            options.cloudRegion, options.registryName);
        break;
      case "patch-device-rsa":
        System.out.println("Patch device with RSA");
        patchRsa256ForAuth(options.deviceId, options.rsaCertificateFile, options.projectId,
            options.cloudRegion, options.registryName);
        break;
      default:
        System.out.println("You entered: " + options.command);
        System.out.println("Wrong, wrong, wrong. Usage is like this:"); // TODO:
        break;
    }


    /*
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
      registry.deleteRegistry();
    } catch (IOException e) {
      System.out.println("Exception: " + e.getMessage());
    }

    // Delete the RSA device. The registry is now empty.
    registry.deleteDevice(rs256deviceId);

    // Since the registry has no devices in it, the delete will succeed.
    registry.deleteRegistry();
    */
  }
}
