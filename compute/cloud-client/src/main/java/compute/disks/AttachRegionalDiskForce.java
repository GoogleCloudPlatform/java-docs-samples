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

package compute.disks;

// [START compute_instance_attach_regional_disk_force]
import com.google.cloud.compute.v1.AttachDiskInstanceRequest;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AttachRegionalDiskForce {
  public static void main(String[] args)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the zone in which the instance you want to use resides.
    String zone = "zone-name";
    // Name of the compute instance you want to attach a disk to.
    String instanceName = "YOUR_INSTANCE_NAME";
    // Full or partial URL of a persistent disk that you want to attach.
    String diskLink = String.format(
            "projects/%s/regions/DISK_REGION/disks/YOUR_DISK_NAME", projectId);

    attachRegionalDiskForce(projectId, zone, instanceName, diskLink);
  }

  // Attaches a regional disk to the instance,
  // forcing the attachment even if other VMs are using the disk.
  public static void attachRegionalDiskForce(
          String projectId, String zone, String instanceName, String diskLink)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (InstancesClient instancesClient = InstancesClient.create()) {
      AttachedDisk attachedDisk = AttachedDisk.newBuilder()
              .setSource(diskLink)
              .setMode(AttachedDisk.Mode.READ_WRITE.toString())
              .build();

      AttachDiskInstanceRequest attachDiskRequest = AttachDiskInstanceRequest.newBuilder()
              .setProject(projectId)
              .setZone(zone)
              .setInstance(instanceName)
              .setAttachedDiskResource(attachedDisk)
              .setForceAttach(true) //Force the attachment
              .build();


      Operation response = instancesClient.attachDiskAsync(attachDiskRequest)
              .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.printf("Error attaching regional disk: %s%n", response.getError());
        return;
      }
      System.out.println("Regional disk attached successfully.");
    }
  }
}
// [END compute_instance_attach_regional_disk_force]
