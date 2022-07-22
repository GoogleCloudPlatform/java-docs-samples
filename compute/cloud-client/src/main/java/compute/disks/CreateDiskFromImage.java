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
// limitations under the License

package compute.disks;

// [START compute_disk_create_from_image]

import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateDiskFromImage {

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
    String diskType = "zones/us-west3-b/diskTypes/pd-ssd";

    // Size of the new disk in gigabytes.
    long diskSizeGb = 10;

    // Source image to use when creating this disk. You must have read access to this disk. This
    // can be one of the publicly available images or an image from one of your projects.
    // This value uses the following format: "projects/{project_name}/global/images/{image_name}"
    String sourceImage = String.format("projects/%s/global/images/%s", projectId, "IMAGE_NAME");

    createDiskFromImage(projectId, zone, diskName, diskType, diskSizeGb, sourceImage);
  }

  // Creates a new disk in a project in given zone using an image as base.
  public static void createDiskFromImage(String projectId, String zone, String diskName,
      String diskType, long diskSizeGb, String sourceImage)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    try (DisksClient disksClient = DisksClient.create()) {

      Disk disk = Disk.newBuilder()
          .setSizeGb(diskSizeGb)
          .setName(diskName)
          .setZone(zone)
          .setType(diskType)
          .setSourceImage(sourceImage)
          .build();

      Operation response = disksClient.insertAsync(projectId, zone, disk)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Disk creation failed ! ! " + response);
        return;
      }
      System.out.println("Disk created from image. Operation Status: " + response.getStatus());
    }
  }
}
// [END compute_disk_create_from_image]