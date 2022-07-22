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

// [START compute_snapshot_create]

import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.Snapshot;
import com.google.cloud.compute.v1.SnapshotsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateSnapshot {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // You need to pass `zone` or `region` parameter relevant to the disk you want to
    // snapshot, but not both. Pass `zone` parameter for zonal disks and `region` for
    // regional disks.

    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";

    // Name of the disk you want to create.
    String diskName = "YOUR_DISK_NAME";

    // Name of the snapshot to be created.
    String snapshotName = "YOUR_SNAPSHOT_NAME";

    // Name of the zone in which is the disk you want to snapshot (for zonal disks).
    String zone = "europe-central2-b";

    // Name of the region in which is the disk you want to snapshot (for regional disks).
    String region = "your-disk-region";

    // The Cloud Storage multi-region or the Cloud Storage region where you
    // want to store your snapshot.
    // You can specify only one storage location. Available locations:
    // https://cloud.google.com/storage/docs/locations#available-locations
    String location = "europe-central2";

    // project ID or project number of the Cloud project that
    // hosts the disk you want to snapshot. If not provided, will look for
    // the disk in the `projectId` project.
    String diskProjectId = "YOUR_DISK_PROJECT_ID";

    createSnapshot(projectId, diskName, snapshotName, zone, region, location, diskProjectId);
  }

  // Creates a snapshot of a disk.
  public static void createSnapshot(String projectId, String diskName, String snapshotName,
      String zone, String region, String location, String diskProjectId)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `snapshotsClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (SnapshotsClient snapshotsClient = SnapshotsClient.create()) {

      if (zone.isEmpty() && region.isEmpty()) {
        throw new Error("You need to specify 'zone' or 'region' for this function to work");
      }

      if (!zone.isEmpty() && !region.isEmpty()) {
        throw new Error("You can't set both 'zone' and 'region' parameters");
      }

      // If Disk's project id is not specified, then the projectId parameter will be used.
      if (diskProjectId.isEmpty()) {
        diskProjectId = projectId;
      }

      // If zone is not empty, use the DisksClient to create a disk.
      // Else, use the RegionDisksClient.
      Disk disk;
      if (!zone.isEmpty()) {
        DisksClient disksClient = DisksClient.create();
        disk = disksClient.get(projectId, zone, diskName);
      } else {
        RegionDisksClient regionDisksClient = RegionDisksClient.create();
        disk = regionDisksClient.get(diskProjectId, region, diskName);
      }

      // Set the snapshot properties.
      Snapshot snapshotResource;
      if (!location.isEmpty()) {
        snapshotResource = Snapshot.newBuilder()
            .setName(snapshotName)
            .setSourceDisk(disk.getSelfLink())
            .addStorageLocations(location)
            .build();
      } else {
        snapshotResource = Snapshot.newBuilder()
            .setName(snapshotName)
            .setSourceDisk(disk.getSelfLink())
            .build();
      }

      // Wait for the operation to complete.
      Operation operation = snapshotsClient.insertAsync(projectId, snapshotResource)
          .get(3, TimeUnit.MINUTES);

      if (operation.hasError()) {
        System.out.println("Snapshot creation failed!" + operation);
        return;
      }

      // Retrieve the created snapshot.
      Snapshot snapshot = snapshotsClient.get(projectId, snapshotName);
      System.out.printf("Snapshot created: %s", snapshot.getName());

    }
  }
}
// [END compute_snapshot_create]