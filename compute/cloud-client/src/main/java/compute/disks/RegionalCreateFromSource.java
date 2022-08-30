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

// [START compute_regional_disk_create_from_disk]

import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RegionalCreateFromSource {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String project = "YOUR_PROJECT_ID";

    // Name of the zone in which you want to create the disk.
    String region = "europe-central2";

    // An iterable collection of zone names in which you want to keep
    // the new disks' replicas. One of the replica zones of the clone must match
    // the zone of the source disk.
    List<String> replicaZones = new ArrayList<>();

    // Name of the disk you want to create.
    String diskName = "YOUR_DISK_NAME";

    // The type of disk you want to create. This value uses the following format:
    // "zones/{zone}/diskTypes/(pd-standard|pd-ssd|pd-balanced|pd-extreme)".
    // For example: "zones/us-west3-b/diskTypes/pd-ssd"
    String diskType = String.format("zones/%s/diskTypes/pd-ssd", "ZONE_NAME");

    // Size of the new disk in gigabytes.
    int diskSizeGb = 10;

    // A link to the disk you want to use as a source for the new disk.
    //     This value uses the following format: "projects/{project_name}/zones/{zone}/disks/{disk_name}"
    String diskLink = String.format("projects/%s/zones/%s/disks/%s", "PROJECT_NAME", "ZONE",
        "DISK_NAME");

    // A link to the snapshot you want to use as a source for the new disk.
    //     This value uses the following format: "projects/{project_name}/global/snapshots/{snapshot_name}"
    String snapshotLink = String.format("projects/%s/global/snapshots/%s", "PROJECT_NAME",
        "SNAPSHOT_NAME");

    createRegionalDisk(project, region, replicaZones, diskName, diskType, diskSizeGb, diskLink,
        snapshotLink);
  }

  // Creates a regional disk from an existing zonal disk in a given project.
  public static void createRegionalDisk(String project, String region, List<String> replicaZones,
      String diskName, String diskType, int diskSizeGb,
      String diskLink, String snapshotLink)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `regionDisksClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (RegionDisksClient regionDisksClient = RegionDisksClient.create()) {

      Disk.Builder diskBuilder = Disk.newBuilder()
          .addAllReplicaZones(replicaZones)
          .setName(diskName)
          .setType(diskType)
          .setSizeGb(diskSizeGb)
          .setRegion(region);

      // Set source disk if diskLink is not empty.
      if (!diskLink.isEmpty()) {
        diskBuilder.setSourceDisk(diskLink);
      }

      // Set source snapshot if the snapshot link is not empty.
      if (!snapshotLink.isEmpty()) {
        diskBuilder.setSourceSnapshot(snapshotLink);
      }

      // Wait for the operation to complete.
      Operation operation = regionDisksClient.insertAsync(project, region, diskBuilder.build())
          .get(3, TimeUnit.MINUTES);

      if (operation.hasError()) {
        System.out.println("Disk creation failed!" + operation);
        return;
      }
      System.out.println(
          "Regional disk created. Operation Status: " + operation.getStatus());
    }
  }
}
// [END compute_regional_disk_create_from_disk]