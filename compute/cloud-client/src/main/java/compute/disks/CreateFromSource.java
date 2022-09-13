// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package compute.disks;

// [START compute_disk_create_from_disk]

import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateFromSource {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String project = "YOUR_PROJECT_ID";

    // Name of the zone in which you want to create the disk.
    String zone = "europe-central2-b";

    // Name of the disk you want to create.
    String diskName = "YOUR_DISK_NAME";

    // The type of disk you want to create. This value uses the following format:
    // "zones/{zone}/diskTypes/(pd-standard|pd-ssd|pd-balanced|pd-extreme)".
    // For example: "zones/us-west3-b/diskTypes/pd-ssd"
    String diskType = String.format("zones/%s/diskTypes/pd-ssd", zone);

    // Size of the new disk in gigabytes.
    int diskSizeGb = 10;

    // A link to the disk you want to use as a source for the new disk.
    //     This value uses the following format:
    //     "projects/{project_name}/zones/{zone}/disks/{disk_name}"
    String diskLink = String.format("projects/%s/zones/%s/disks/%s", "PROJECT_NAME", "ZONE",
        "DISK_NAME");

    createDiskFromDisk(project, zone, diskName, diskType, diskSizeGb, diskLink);
  }

  // Creates a disk in a project in a given zone.
  public static void createDiskFromDisk(String project, String zone, String diskName,
      String diskType, int diskSizeGb, String diskLink)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `disksClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (DisksClient disksClient = DisksClient.create()) {

      // Create the disk.
      Disk disk = Disk.newBuilder()
          .setZone(zone)
          .setSizeGb(diskSizeGb)
          .setSourceDisk(diskLink)
          .setType(diskType)
          .setName(diskName)
          .build();

      // Wait for the insert instance operation to complete.
      Operation operation = disksClient.insertAsync(project, zone, disk).get(3, TimeUnit.MINUTES);

      if (operation.hasError()) {
        System.out.println("Disk creation failed!" + operation);
        return;
      }
      System.out.println(
          "Disk created from source. Operation Status: " + operation.getStatus());
    }
  }
}
// [END compute_disk_create_from_disk]