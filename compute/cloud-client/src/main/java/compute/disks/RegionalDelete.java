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

// [START compute_regional_disk_delete]

import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RegionalDelete {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String project = "YOUR_PROJECT_ID";

    // Name of the region where the disk is located.
    String region = "europe-central2";

    // Name of the disk you want to delete.
    String diskName = "YOUR_DISK_NAME";

    deleteRegionalDisk(project, region, diskName);
  }

  // Deletes a disk from a project.
  public static void deleteRegionalDisk(String project, String region, String diskName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `regionDisksClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (RegionDisksClient regionDisksClient = RegionDisksClient.create()) {

      Operation operation = regionDisksClient.deleteAsync(project, region, diskName)
          .get(3, TimeUnit.MINUTES);

      if (operation.hasError()) {
        System.out.println("Disk deletion failed!");
        throw new Error(operation.getError().toString());
      }
      System.out.println("Operation Status: " + operation.getStatus());
    }
  }
}
// [END compute_regional_disk_delete]
