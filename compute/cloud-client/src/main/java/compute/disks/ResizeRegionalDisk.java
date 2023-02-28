/*
 * Copyright 2023 Google LLC
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

// [START compute_regional_disk_resize]

import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionDisksClient;
import com.google.cloud.compute.v1.RegionDisksResizeRequest;
import com.google.cloud.compute.v1.ResizeRegionDiskRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ResizeRegionalDisk {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-project-id";

    // Region of the disk to be resized.
    String diskRegion = "us-central1";

    // Name of the disk that you want to resize.
    String diskName = "DISK_NAME";

    // The new size you want to set for the disk in gigabytes.
    int newSizeGb = 23;

    resizeRegionalDisk(projectId, diskRegion, diskName, newSizeGb);
  }

  // Resizes a regional persistent disk to a specified size in GB. After you resize the disk, you
  // must also resize the file system so that the operating system can access the additional space.
  public static void resizeRegionalDisk(String projectId, String diskRegion, String diskName,
      int newSizeGb)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `regionDisksClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (RegionDisksClient regionDisksClient = RegionDisksClient.create()) {

      ResizeRegionDiskRequest resizeRegionDiskRequest = ResizeRegionDiskRequest.newBuilder()
          .setRegion(diskRegion)
          .setRegionDisksResizeRequestResource(RegionDisksResizeRequest.newBuilder()
              .setSizeGb(newSizeGb)
              .build())
          .setDisk(diskName)
          .setProject(projectId)
          .build();

      Operation response = regionDisksClient.resizeAsync(resizeRegionDiskRequest)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Resize region disk failed! " + response);
        return;
      }
      System.out.println("Resize region disk - operation status: " + response.getStatus());
    }
  }
}
// [END compute_regional_disk_resize]