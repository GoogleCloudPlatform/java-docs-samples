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
    // Name of the region in which you want to create the secondary disk.
    String disksRegion = "us-east1";
    // Name of the disk you want to create.
    String secondaryDiskName = "SECONDARY_DISK_NAME";
    // Size of the new disk in gigabytes.
    long diskSizeGb = 10;
    // Name of the primary disk you want to use as a source for the new disk.
    String primaryDiskName = "PRIMARY_DISK_NAME";
    // The type of regional disk you want to create. This value uses the following format:
    // "projects/{projectId}/regions/{region}/diskTypes/
    // (pd-standard|pd-ssd|pd-balanced|pd-extreme)".
    String diskType = String.format(
        "projects/%s/regions/%s/diskTypes/pd-balanced", projectId, disksRegion);

    createDiskSecondaryRegional(projectId, secondaryDiskName, disksRegion,
          diskSizeGb, primaryDiskName,  diskType);
  }

  // Creates a secondary disk in a specified region with the source disk information.
  public static Disk createDiskSecondaryRegional(String projectId, String secondaryDiskName,
       String disksRegion, long diskSizeGb, String primaryDiskName, String diskType)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // An iterable collection of zone names in which you want to keep
    // the new disks' replicas. One of the replica zones of the clone must match
    // the zone of the source disk.
    List<String> replicaZones = Arrays.asList(
        String.format("projects/%s/zones/%s-a", projectId, disksRegion),
        String.format("projects/%s/zones/%s-b", projectId, disksRegion));

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (RegionDisksClient disksClient = RegionDisksClient.create()) {
      String primaryDiskSource = String.format("projects/%s/regions/%s/disks/%s",
          projectId, disksRegion, primaryDiskName);
      // Create the disk object with the source disk information.
      Disk disk = Disk.newBuilder()
          .addAllReplicaZones(replicaZones)
          .setName(secondaryDiskName)
              .setSizeGb(diskSizeGb)
              .setType(diskType)
              .setRegion(disksRegion)
              .setSourceDisk(primaryDiskSource).build();

      // Wait for the create disk operation to complete.
      Operation response = disksClient.insertAsync(projectId, disksRegion, disk)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        return null;
      }
      return disksClient.get(projectId, disksRegion, secondaryDiskName);
    }
  }
}
// [END compute_disk_create_secondary_regional]