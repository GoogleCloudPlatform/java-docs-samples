/*
 * Copyright 2020 Google Inc.
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

import static com.example.cloud.iot.endtoend.CloudiotPubsubExampleServer.APP_NAME;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.cloudiot.v1.CloudIotScopes;
import com.google.api.services.cloudiot.v1.model.Device;
import com.google.api.services.cloudiot.v1.model.DeviceRegistry;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class CleanUpHelper {
  protected static List<DeviceRegistry> getRegisteries(String project, String cloudRegion)
      throws IOException, GeneralSecurityException {
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
    final CloudIot service =
        new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init)
            .setApplicationName(APP_NAME)
            .build();
    final String projectPath = String.format("projects/%s/locations/%s", project, cloudRegion);

    List<DeviceRegistry> registries =
        service
            .projects()
            .locations()
            .registries()
            .list(projectPath)
            .execute()
            .getDeviceRegistries();
    return registries;
  }

  /**
   * clearRegistry
   *
   * <ul>
   *   <li>Registries can't be deleted if they contain devices,
   *   <li>Gateways (a type of device) can't be deleted if they have bound devices
   *   <li>Devices can't be deleted if bound to gateways...
   * </ul>
   *
   * <p>To completely remove a registry, you must unbind all devices from gateways, then remove all
   * devices in a registry before removing the registry. As pseudocode: <code>
   *   ForAll gateways
   *     ForAll devicesBoundToGateway
   *       unbindDeviceFromGateway
   *   ForAll devices
   *     Delete device by ID
   *   Delete registry
   *  </code>
   */
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
}
