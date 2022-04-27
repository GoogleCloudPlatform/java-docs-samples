/*
 * Copyright 2021 Google LLC
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

package compute;

import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.AccessConfig.NetworkTier;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.GlobalOperationsClient;
import com.google.cloud.compute.v1.InsertInstanceTemplateRequest;
import com.google.cloud.compute.v1.InstanceProperties;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.InstanceTemplatesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateInstanceTemplate {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // projectId: project ID or project number of the Cloud project you use.
    // templateName: name of the new template to create.
    String projectId = "your-project-id";
    String templateName = "template-name";
    createInstanceTemplate(projectId, templateName);
  }

  /*
    Create a new instance template with the provided name and a specific
    instance configuration.
   */
  public static void createInstanceTemplate(String projectId, String templateName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create()) {

      String machineType = "e2-standard-4";
      String sourceImage = "projects/debian-cloud/global/images/family/debian-11";

      // The template describes the size and source image of the boot disk
      // to attach to the instance.
      AttachedDisk attachedDisk = AttachedDisk.newBuilder()
          .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
              .setSourceImage(sourceImage)
              .setDiskSizeGb(250).build())
          .setAutoDelete(true)
          .setBoot(true).build();

      // The template connects the instance to the `default` network,
      // without specifying a subnetwork.
      NetworkInterface networkInterface = NetworkInterface.newBuilder()
          .setName("global/networks/default")
          // The template lets the instance use an external IP address.
          .addAccessConfigs(AccessConfig.newBuilder()
              .setName("External NAT")
              .setType(AccessConfig.Type.ONE_TO_ONE_NAT.toString())
              .setNetworkTier(NetworkTier.PREMIUM.toString()).build()).build();

      InstanceProperties instanceProperties = InstanceProperties.newBuilder()
          .addDisks(attachedDisk)
          .setMachineType(machineType)
          .addNetworkInterfaces(networkInterface).build();

      InsertInstanceTemplateRequest insertInstanceTemplateRequest = InsertInstanceTemplateRequest
          .newBuilder()
          .setProject(projectId)
          .setInstanceTemplateResource(InstanceTemplate.newBuilder()
              .setName(templateName)
              .setProperties(instanceProperties).build()).build();

      // Create the Instance Template.
      Operation response = instanceTemplatesClient.insertAsync(insertInstanceTemplateRequest)
          .get(3, TimeUnit.MINUTES);
      ;

      if (response.hasError()) {
        System.out.println("Instance Template creation failed ! ! " + response);
        return;
      }
      System.out
          .printf("Instance Template Operation Status %s: %s", templateName, response.getStatus());
    }
  }

  public static void createInstanceTemplateWithDiskType(String projectId, String templateName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create();
        GlobalOperationsClient globalOperationsClient = GlobalOperationsClient.create()) {

      AttachedDisk disk = AttachedDisk.newBuilder()
          .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
              .setDiskSizeGb(10)
              .setSourceImage("projects/debian-cloud/global/images/family/debian-10").build())
          .setAutoDelete(true)
          .setBoot(true)
          .setType(AttachedDisk.Type.PERSISTENT.toString()).build();

      InstanceTemplate instanceTemplate = InstanceTemplate.newBuilder()
          .setName(templateName)
          .setProperties(InstanceProperties.newBuilder()
              .setMachineType("n1-standard-1")
              .addDisks(disk)
              .addNetworkInterfaces(NetworkInterface.newBuilder()
                  .setName("global/networks/default").build()).build()).build();

      InsertInstanceTemplateRequest insertInstanceTemplateRequest = InsertInstanceTemplateRequest
          .newBuilder()
          .setProject(projectId)
          .setInstanceTemplateResource(instanceTemplate).build();

      Operation response = instanceTemplatesClient.insertAsync(insertInstanceTemplateRequest)
          .get(3, TimeUnit.MINUTES);
      ;

      if (response.hasError()) {
        System.out.println("Instance Template creation failed ! ! " + response);
        return;
      }
      System.out
          .printf("Instance Template Operation Status %s: %s", templateName, response.getStatus());
    }
  }
}