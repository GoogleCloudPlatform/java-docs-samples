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

// [START compute_disk_create_secondary_regional]
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DiskAsyncReplication;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateDiskSecondaryRegional {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // The project that contains the primary disk.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the primary disk you want to use.
    String primaryDiskName = "PRIMARY_DISK_NAME";
    // Name of the disk you want to create.
    String secondaryDiskName = "SECONDARY_DISK_NAME";
    // Name of the region in which your primary disk is located.
    // Learn more about zones and regions:
    // https://cloud.google.com/compute/docs/disks/async-pd/about#supported_region_pairs
    String primaryDiskRegion = "us-central1";
    // Name of the region in which you want to create the secondary disk.
    String secondaryDiskRegion = "us-east1";
    // Size of the new disk in gigabytes.
    // Learn more about disk requirements:
    // https://cloud.google.com/compute/docs/disks/async-pd/configure?authuser=0#disk_requirements
    long diskSizeGb = 30L;
    // The type of the disk you want to create. This value uses the following format:
    // "projects/{projectId}/zones/{zone}/diskTypes/
    // (pd-standard|pd-ssd|pd-balanced|pd-extreme)".
    String diskType = String.format(
        "projects/%s/regions/%s/diskTypes/pd-balanced", projectId, secondaryDiskRegion);

    createDiskSecondaryRegional(projectId, primaryDiskName, secondaryDiskName,
        primaryDiskRegion, secondaryDiskRegion, diskSizeGb, diskType);
  }

  // Creates a secondary disk in a specified region.
  public static Disk createDiskSecondaryRegional(String projectId, String primaryDiskName,
      String secondaryDiskName, String primaryDiskRegion,  String secondaryDiskRegion,
      long diskSizeGb,  String diskType)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // An iterable collection of zone names in which you want to keep
    // the new disks' replicas. One of the replica zones of the clone must match
    // the zone of the source disk.
    List<String> replicaZones = Arrays.asList(
        String.format("projects/%s/zones/%s-c", projectId, secondaryDiskRegion),
        String.format("projects/%s/zones/%s-b", projectId, secondaryDiskRegion));

    String primaryDiskSource = String.format("projects/%s/regions/%s/disks/%s",
          projectId, primaryDiskRegion, primaryDiskName);

    DiskAsyncReplication asyncReplication = DiskAsyncReplication.newBuilder()
        .setDisk(primaryDiskSource)
        .build();

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (RegionDisksClient disksClient = RegionDisksClient.create()) {

      Disk disk = Disk.newBuilder()
          .addAllReplicaZones(replicaZones)
          .setName(secondaryDiskName)
          .setSizeGb(diskSizeGb)
          .setType(diskType)
          .setRegion(secondaryDiskRegion)
          .setAsyncPrimaryDisk(asyncReplication)
          .build();

      // Wait for the create disk operation to complete.
      Operation response = disksClient.insertAsync(projectId, secondaryDiskRegion, disk)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        return null;
      }
      return disksClient.get(projectId, secondaryDiskRegion, secondaryDiskName);
    }
  }
}
// [END compute_disk_create_secondary_regional]