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

// [START compute_regional_disk_stop_replication]
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.StopAsyncReplicationRegionDiskRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StopRegionalDiskReplication {

  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // The project that contains the primary disk.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the region or zone in which your secondary disk is located.
    String secondaryDiskLocation = "us-east1-b";
    // Name of the secondary disk.
    String secondaryDiskName = "SECONDARY_DISK_NAME";

    stopRegionalDiskAsyncReplication(projectId, secondaryDiskLocation, secondaryDiskName);
  }

  // Stops asynchronous replication for the specified disk.
  public static Status stopRegionalDiskAsyncReplication(
          String project, String secondaryDiskLocation, String secondaryDiskName)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (RegionDisksClient disksClient = RegionDisksClient.create()) {
      StopAsyncReplicationRegionDiskRequest stopReplicationDiskRequest =
              StopAsyncReplicationRegionDiskRequest.newBuilder()
                      .setDisk(secondaryDiskName)
                      .setProject(project)
                      .setRegion(secondaryDiskLocation)
                      .build();
      Operation response = disksClient.stopAsyncReplicationAsync(stopReplicationDiskRequest)
              .get(1, TimeUnit.MINUTES);

      if (response.hasError()) {
        throw new Error("Error stopping replication! " + response.getError());
      }
      return response.getStatus();
    }
  }
}
// [END compute_regional_disk_stop_replication]