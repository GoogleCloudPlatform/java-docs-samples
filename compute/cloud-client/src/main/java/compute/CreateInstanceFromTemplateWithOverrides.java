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

// [START compute_instances_create_from_template_with_overrides]

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.InstanceTemplatesClient;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateInstanceFromTemplateWithOverrides {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    /* TODO(developer): Replace these variables before running the sample.
     * projectId - ID or number of the project you want to use.
     * zone - Name of the zone you want to check, for example: us-west3-b
     * instanceName - Name of the new instance.
     * instanceTemplateName - Name of the instance template to use when creating the new instance.
     * machineType - Machine type you want to set in following format:
     *    "zones/{zone}/machineTypes/{type_name}". For example:
     *    "zones/europe-west3-c/machineTypes/f1-micro"
     *    You can find the list of available machine types using:
     *    https://cloud.google.com/sdk/gcloud/reference/compute/machine-types/list
     * newDiskSourceImage - Path the the disk image you want to use for your new
     *    disk. This can be one of the public images
     *    (like "projects/debian-cloud/global/images/family/debian-10")
     *    or a private image you have access to.
     *    You can check the list of available public images using the doc:
     *    http://cloud.google.com/compute/docs/images
     */
    String projectId = "your-project-id";
    String zone = "zone-name";
    String instanceName = "instance-name";
    String instanceTemplateName = "instance-template-name";

    createInstanceFromTemplateWithOverrides(projectId, zone, instanceName, instanceTemplateName);
  }

  // Creates a Compute Engine VM instance from an instance template,
  // but overrides the disk and machine type options in the template.
  public static void createInstanceFromTemplateWithOverrides(String projectId, String zone,
      String instanceName, String instanceTemplateName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    try (InstancesClient instancesClient = InstancesClient.create();
        InstanceTemplatesClient instanceTemplatesClient = InstanceTemplatesClient.create()) {

      String machineType = "n1-standard-1";
      String newDiskSourceImage = "projects/debian-cloud/global/images/family/debian-10";

      // Retrieve an instance template.
      InstanceTemplate instanceTemplate = instanceTemplatesClient
          .get(projectId, instanceTemplateName);

      AttachedDisk newdisk = AttachedDisk.newBuilder()
          .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
              .setDiskSizeGb(10)
              .setSourceImage(newDiskSourceImage).build())
          .setAutoDelete(true)
          .setBoot(false)
          .setType(AttachedDisk.Type.PERSISTENT.toString()).build();

      Instance instance = Instance.newBuilder()
          .setName(instanceName)
          .setMachineType(String.format("zones/%s/machineTypes/%s", zone, machineType))
          // If you override a repeated field, all repeated values
          // for that property are replaced with the
          // corresponding values provided in the request.
          // When adding a new disk to existing disks,
          // insert all existing disks as well.
          .addAllDisks(instanceTemplate.getProperties().getDisksList())
          .addDisks(newdisk)
          .build();

      InsertInstanceRequest insertInstanceRequest = InsertInstanceRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setInstanceResource(instance)
          .setSourceInstanceTemplate(instanceTemplate.getSelfLink()).build();

      Operation response = instancesClient.insertAsync(insertInstanceRequest)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance creation from template with overrides failed ! ! " + response);
        return;
      }
      System.out
          .printf("Instance creation from template with overrides: Operation Status %s: %s ",
              instanceName, response.getStatus());
    }

  }
}
// [END compute_instances_create_from_template_with_overrides]