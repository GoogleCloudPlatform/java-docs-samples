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

// [START compute_disk_create_from_snapshot]

import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.InsertDiskRequest;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateDiskFromSnapshot {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.

    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";

    // Name of the zone in which you want to create the disk.
    String zone = "europe-central2-b";

    // Name of the disk you want to create.
    String diskName = "YOUR_DISK_NAME";

    // The type of disk you want to create. This value uses the following format:
    // "zones/{zone}/diskTypes/(pd-standard|pd-ssd|pd-balanced|pd-extreme)".
    // For example: "zones/us-west3-b/diskTypes/pd-ssd"
    String diskType = String.format("zones/%s/diskTypes/pd-ssd", zone);

    // Size of the new disk in gigabytes.
    long diskSizeGb = 10;

    // A link to the snapshot you want to use as a source for the new disk.
    // This value uses the following format:
    // "projects/{projectName}/global/snapshots/{snapshotName}"
    String snapshotLink = String.format("projects/%s/global/snapshots/%s", projectId,
        "SNAPSHOT_NAME");

    createDiskFromSnapshot(projectId, zone, diskName, diskType, diskSizeGb, snapshotLink);
  }

  // Creates a new disk in a project in given zone, using a snapshot.
  public static void createDiskFromSnapshot(String projectId, String zone, String diskName,
      String diskType, long diskSizeGb, String snapshotLink)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `disksClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (DisksClient disksClient = DisksClient.create()) {

      // Set the disk properties and the source snapshot.
      Disk disk = Disk.newBuilder()
          .setName(diskName)
          .setZone(zone)
          .setSizeGb(diskSizeGb)
          .setType(diskType)
          .setSourceSnapshot(snapshotLink)
          .build();

      // Create the Insert disk request.
      InsertDiskRequest insertDiskRequest = InsertDiskRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setDiskResource(disk)
          .build();

      // Wait for the create disk operation to complete.
      Operation response = disksClient.insertAsync(insertDiskRequest)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Disk creation failed!" + response);
        return;
      }
      System.out.println("Disk created. Operation Status: " + response.getStatus());
    }
  }
}
// [END compute_disk_create_from_snapshot]