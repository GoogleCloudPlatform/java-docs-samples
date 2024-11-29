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

// [START compute_disk_stop_replication]
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class StopDiskReplication {

  public static void main(String[] args)
        throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Location of your disk.
    String diskLocation = "us-central1-a";
    // Name of your disk.
    String diskName = "YOUR_DISK_NAME";

    stopDiskAsyncReplication(projectId, diskLocation, diskName);
  }

  // Stops asynchronous replication for the specified disk.
  public static Operation.Status stopDiskAsyncReplication(
        String project, String diskLocation, String diskName)
        throws IOException, ExecutionException, InterruptedException {
    Operation response;
    if (Character.isDigit(diskLocation.charAt(diskLocation.length() - 1))) {
      // Initialize client that will be used to send requests. This client only needs to be created
      // once, and can be reused for multiple requests.
      try (RegionDisksClient disksClient = RegionDisksClient.create()) {
        response = disksClient.stopAsyncReplicationAsync(
                project, diskLocation, diskName).get();
      }
    } else {
      try (DisksClient disksClient = DisksClient.create()) {
        response = disksClient.stopAsyncReplicationAsync(
                project, diskLocation, diskName).get();
      }
    }
    if (response.hasError()) {
      throw new Error("Error stopping disk replication! " + response.getError());
    }
    return response.getStatus();
  }
}
// [END compute_disk_stop_replication]