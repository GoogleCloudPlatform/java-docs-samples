/*
 * Copyright 2024 Google LLC
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

// [START compute_regional_template_create]

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.InsertRegionInstanceTemplateRequest;
import com.google.cloud.compute.v1.InstanceProperties;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionInstanceTemplatesClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateRegionalInstanceTemplate {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the instance you want to create.
    String instanceName = "YOUR_INSTANCE_NAME";
    // Name of the region.
    String region = "us-central1";

    createRegionalInstanceTemplate(projectId, region, instanceName);
  }

  //  Create a new regional instance template with the provided name and a specific
  //  instance configuration.
  public static void createRegionalInstanceTemplate(
      String projectId, String region, String templateName)
      throws  IOException, InterruptedException, ExecutionException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (RegionInstanceTemplatesClient templatesClientRegion =
             RegionInstanceTemplatesClient.create()) {

      String machineType = "n1-standard-1"; // Example machine type
      String sourceImage = "projects/debian-cloud/global/images/family/debian-11"; // Example image

      // Define the boot disk for the instance template
      AttachedDisk attachedDisk = AttachedDisk.newBuilder()
          .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
              .setSourceImage(sourceImage)
              .setDiskType("pd-balanced") // Example disk type
              .setDiskSizeGb(100L) // Example disk size
              .build())
          .setAutoDelete(true)
          .setBoot(true)
          .build();

      // Define the network interface for the instance template
      // Note: The subnetwork must be in the same region as the instance template.
      NetworkInterface networkInterface = NetworkInterface.newBuilder()
          .setName("my-network-test")
          .setSubnetwork(String.format("projects/%s/regions/%s/subnetworks/default",
              projectId, region))
          .build();

      // Define the instance properties for the template
      InstanceProperties instanceProperties = InstanceProperties.newBuilder()
          .addDisks(attachedDisk)
          .setMachineType(machineType)
          .addNetworkInterfaces(networkInterface)
          .build();

      // Build the instance template object
      InstanceTemplate instanceTemplate = InstanceTemplate.newBuilder()
          .setName(templateName)
          .setProperties(instanceProperties)
          .build();

      // Create the request to insert the instance template
      InsertRegionInstanceTemplateRequest insertInstanceTemplateRequest =
          InsertRegionInstanceTemplateRequest
              .newBuilder()
              .setProject(projectId)
              .setRegion(region)
              .setInstanceTemplateResource(instanceTemplate)
              .build();

      // Send the request and wait for the operation to complete
      Operation response = templatesClientRegion.insertAsync(insertInstanceTemplateRequest)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance Template creation failed! " + response);
        return;
      }
      System.out.printf("Instance Template Operation Status: %s%n", response.getStatus());
    }
  }
}
// [END compute_regional_template_create]
