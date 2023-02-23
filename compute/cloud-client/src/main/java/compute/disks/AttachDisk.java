/*
 * Copyright 2023 Google LLC
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

// [START compute_regional_disk_attach]
// [START compute_disk_attach]

import com.google.cloud.compute.v1.AttachDiskInstanceRequest;
import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AttachDisk {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-project-id";

    // Name of the zone in which the instance you want to use resides.
    String zone = "zone-name";

    // Name of the compute instance you want to attach a disk to.
    String instanceName = "instance-name";

    // Full or partial URL of a persistent disk that you want to attach. This can be either
    // be a regional or zonal disk.
    // Valid formats:
    //     * https://www.googleapis.com/compute/v1/projects/{project}/zones/{zone}/disks/{disk_name}
    //     * /projects/{project}/zones/{zone}/disks/{disk_name}
    //     * /projects/{project}/regions/{region}/disks/{disk_name}
    String diskLink = String.format("/projects/%s/zones/%s/disks/%s",
        "project", "zone", "disk_name");

    // Specifies in what mode the disk will be attached to the instance. Available options are
    // `READ_ONLY` and `READ_WRITE`. Disk in `READ_ONLY` mode can be attached to
    // multiple instances at once.
    String mode = "READ_ONLY";

    attachDisk(projectId, zone, instanceName, diskLink, mode);
  }

  // Attaches a non-boot persistent disk to a specified compute instance.
  // The disk might be zonal or regional.
  // You need following permissions to execute this action:
  // https://cloud.google.com/compute/docs/disks/regional-persistent-disk#expandable-1
  public static void attachDisk(String projectId, String zone, String instanceName, String diskLink,
      String mode)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `instancesClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (InstancesClient instancesClient = InstancesClient.create()) {

      AttachDiskInstanceRequest attachDiskInstanceRequest = AttachDiskInstanceRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setInstance(instanceName)
          .setAttachedDiskResource(AttachedDisk.newBuilder()
              .setSource(diskLink)
              .setMode(mode)
              .build())
          .build();

      Operation response = instancesClient.attachDiskAsync(attachDiskInstanceRequest)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Attach disk failed! " + response);
        return;
      }
      System.out.println("Attach disk - operation status: " + response.getStatus());
    }
  }
}
// [END compute_regional_disk_attach]
// [END compute_disk_attach]