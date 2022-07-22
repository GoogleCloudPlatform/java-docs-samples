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

// [START compute_disk_delete]

import com.google.cloud.compute.v1.DeleteDiskRequest;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteDisk {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.

    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";

    // Name of the zone in which is the disk you want to delete.
    String zone = "europe-central2-b";

    // Name of the disk you want to delete.
    String diskName = "YOUR_DISK_NAME";

    deleteDisk(projectId, zone, diskName);
  }

  // Deletes a disk from a project.
  public static void deleteDisk(String projectId, String zone, String diskName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `disksClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (DisksClient disksClient = DisksClient.create()) {

      // Create the request object.
      DeleteDiskRequest deleteDiskRequest = DeleteDiskRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setDisk(diskName)
          .build();

      // Wait for the delete disk operation to complete.
      Operation response = disksClient.deleteAsync(deleteDiskRequest)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Disk deletion failed!" + response);
        return;
      }
      System.out.println("Operation Status: " + response.getStatus());
    }
  }
}
// [END compute_disk_delete]