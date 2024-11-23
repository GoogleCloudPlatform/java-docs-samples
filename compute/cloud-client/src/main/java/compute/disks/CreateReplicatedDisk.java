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

// [START compute_disk_regional_replicated]
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.InsertRegionDiskRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateReplicatedDisk {

  public static void main(String[] args)
          throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // The region for the replicated disk to reside in.
    // The disk must be in the same region as the VM that you plan to attach it to.
    String region = "us-central1";
    // The zones within the region where the two disk replicas are located
    List<String> replicaZones = new ArrayList<>();
    replicaZones.add(String.format("projects/%s/zones/%s", projectId, "us-central1-a"));
    replicaZones.add(String.format("projects/%s/zones/%s", projectId, "us-central1-b"));
    // Name of the disk you want to create.
    String diskName = "YOUR_DISK_NAME";
    // Size of the new disk in gigabytes.
    int diskSizeGb = 200;
    // The type of replicated disk. This value uses the following format:
    // "regions/{region}/diskTypes/(pd-standard|pd-ssd|pd-balanced|pd-extreme)".
    // For example: "regions/us-west3/diskTypes/pd-ssd"
    String diskType = String.format("regions/%s/diskTypes/%s", region, "pd-standard");

    createReplicatedDisk(projectId, region, replicaZones, diskName, diskSizeGb, diskType);
  }

  // Create a disk for synchronous data replication between two zones in the same region
  public static Disk createReplicatedDisk(String projectId, String region,
        List<String> replicaZones, String diskName, int diskSizeGb, String diskType)
        throws IOException, InterruptedException, ExecutionException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (RegionDisksClient regionDisksClient = RegionDisksClient.create()) {
      Disk disk = Disk.newBuilder()
            .setSizeGb(diskSizeGb)
            .setName(diskName)
             .setType(diskType)
             .addAllReplicaZones(replicaZones)
               .build();

      InsertRegionDiskRequest insertRegionDiskRequest = InsertRegionDiskRequest.newBuilder()
               .setProject(projectId)
               .setRegion(region)
               .setDiskResource(disk)
              .build();

      Operation response = regionDisksClient.insertAsync(insertRegionDiskRequest)
               .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Failed to create regional replicated disk.");
        return null;
      }
      return regionDisksClient.get(projectId, region, diskName);
    }
  }
}
// [END compute_disk_regional_replicated]
