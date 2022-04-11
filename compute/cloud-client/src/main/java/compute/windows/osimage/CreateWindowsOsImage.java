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

package compute.windows.osimage;

// [START compute_windows_image_create]

import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.Image;
import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.InsertImageRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class CreateWindowsOsImage {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.

    // Project ID or project number of the Cloud project you use.
    String project = "your-project-id";
    // Zone of the disk you copy from.
    String zone = "europe-central2-b";
    // Name of the source disk you copy from.
    String sourceDiskName = "source-disk-name";
    // Name of the image you want to create.
    String imageName = "your-image-name";
    // Storage location for the image. If the value is undefined,
    // function will store the image in the multi-region closest to your image's source location.
    String storageLocation = "eu";
    // Create the image even if the source disk is attached to a running instance.
    boolean forceCreate = false;

    createWindowsOsImage(project, zone, sourceDiskName, imageName, storageLocation, forceCreate);
  }

  // Creates a new Windows image from the specified source disk.
  public static void createWindowsOsImage(String project, String zone, String sourceDiskName,
      String imageName, String storageLocation, boolean forceCreate)
      throws IOException, ExecutionException, InterruptedException {

    try (ImagesClient imagesClient = ImagesClient.create();
        InstancesClient instancesClient = InstancesClient.create();
        DisksClient disksClient = DisksClient.create()) {

      Disk disk = disksClient.get(project, zone, sourceDiskName);

      // Getting instances where source disk is attached.
      for (String fullInstanceName : disk.getUsersList()) {
        Map<String, String> instanceInfo = parseInstanceName(fullInstanceName);
        Instance instance = instancesClient.get(instanceInfo.get("instanceProjectId"),
            instanceInfo.get("instanceZone"), instanceInfo.get("instanceName"));

        // Сhecking whether the instances is stopped.
        if (!Arrays.asList("TERMINATED", "STOPPED").contains(instance.getStatus())
            && !forceCreate) {
          throw new IllegalStateException(
              String.format(
                  "Instance %s should be stopped. Please stop the instance using GCESysprep command or set forceCreate parameter to true (not recommended). More information here: https://cloud.google.com/compute/docs/instances/windows/creating-windows-os-image#api.",
                  instanceInfo.get("instanceName")));
        }
      }

      if (forceCreate) {
        System.out.println(
            "Warning: forceCreate option compromise the integrity of your image. "
                + "Stop the instance before you create the image if possible.");
      }

      // Create Image.
      Image image = Image.newBuilder()
          .setName(imageName)
          .setSourceDisk(String.format("/zones/%s/disks/%s", zone, sourceDiskName))
          .addStorageLocations(storageLocation.isEmpty() ? "" : storageLocation)
          .build();

      InsertImageRequest insertImageRequest = InsertImageRequest.newBuilder()
          .setProject(project)
          .setForceCreate(forceCreate)
          .setImageResource(image)
          .build();

      Operation response = imagesClient.insertAsync(insertImageRequest).get();

      if (response.hasError()) {
        System.out.println("Windows OS Image creation failed ! ! " + response);
        return;
      }

      System.out.println("Image created.");
    }
  }


  public static Map<String, String> parseInstanceName(String name) {
    String[] parsedName = name.split("/");
    int splitLength = parsedName.length;

    if (splitLength < 5) {
      throw new IllegalArgumentException(
          "Provide correct instance name in the following format: "
              + "https://www.googleapis.com/compute/v1/projects/PROJECT/zones/ZONE/instances/INSTANCE_NAME");
    }

    return new HashMap<>() {
      {
        put("instanceName", parsedName[splitLength - 1]);
        put("instanceZone", parsedName[splitLength - 3]);
        put("instanceProjectId", parsedName[splitLength - 5]);
      }
    };
  }

}
// [END compute_windows_image_create]