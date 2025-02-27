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

// [START compute_disk_create_secondary]
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DiskAsyncReplication;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateDiskSecondaryZonal {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // The project that contains the primary disk.
    String primaryProjectId = "PRIMARY_PROJECT_ID";
    // The project that contains the secondary disk.
    String secondaryProjectId = "SECONDARY_PROJECT_ID";
    // Name of the primary disk you want to use.
    String primaryDiskName = "PRIMARY_DISK_NAME";
    // Name of the zone in which your primary disk is located.
    // Learn more about zones and regions:
    // https://cloud.google.com/compute/docs/disks/async-pd/about#supported_region_pairs
    String primaryDiskZone = "us-central1-a";
    // Name of the disk you want to create.
    String secondaryDiskName = "SECONDARY_DISK_NAME";
    // Name of the zone in which you want to create the secondary disk.
    String secondaryDiskZone = "us-east1-c";
    // Size of the new disk in gigabytes.
    long diskSizeGb = 30L;
    // The type of the disk you want to create. This value uses the following format:
    // "projects/{projectId}/zones/{zone}/diskTypes/
    // (pd-standard|pd-ssd|pd-balanced|pd-extreme)".
    String diskType = String.format(
        "projects/%s/zones/%s/diskTypes/pd-balanced", secondaryProjectId, secondaryDiskZone);

    createDiskSecondaryZonal(primaryProjectId, secondaryProjectId, primaryDiskName,
        secondaryDiskName, primaryDiskZone, secondaryDiskZone, diskSizeGb,  diskType);
  }

  // Creates a secondary disk in a specified zone.
  public static Operation.Status createDiskSecondaryZonal(String primaryProjectId,
       String secondaryProjectId, String primaryDiskName, String secondaryDiskName,
       String primaryDiskZone, String secondaryDiskZone, long diskSizeGb, String diskType)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (DisksClient disksClient = DisksClient.create()) {
      String primaryDiskSource = String.format("projects/%s/zones/%s/disks/%s",
          primaryProjectId, primaryDiskZone, primaryDiskName);

      DiskAsyncReplication asyncReplication = DiskAsyncReplication.newBuilder()
          .setDisk(primaryDiskSource)
          .build();
      Disk disk = Disk.newBuilder()
          .setName(secondaryDiskName)
          .setZone(secondaryDiskZone)
          .setSizeGb(diskSizeGb)
          .setType(diskType)
          .setAsyncPrimaryDisk(asyncReplication)
          .build();

      Operation response = disksClient.insertAsync(secondaryProjectId, secondaryDiskZone, disk)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        throw new Error("Error creating secondary disks! " + response.getError());
      }
      return response.getStatus();
    }
  }
}
// [END compute_disk_create_secondary]

