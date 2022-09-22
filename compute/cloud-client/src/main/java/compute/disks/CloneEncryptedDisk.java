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

// [START compute_disk_clone_encrypted_disk]

import com.google.cloud.compute.v1.CustomerEncryptionKey;
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.InsertDiskRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CloneEncryptedDisk {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String project = "YOUR_PROJECT_ID";

    // Name of the zone in which you want to create the disk.
    String zone = "europe-central2-b";

    // Name of the disk you want to create.
    String diskName = "YOUR_DISK_NAME";

    // The type of disk you want to create. This value uses the following format:
    // "zones/{zone}/diskTypes/(pd-standard|pd-ssd|pd-balanced|pd-extreme)".
    // For example: "zones/us-west3-b/diskTypes/pd-ssd"
    String diskType = String.format("zones/%s/diskTypes/pd-ssd", zone);

    // Size of the new disk in gigabytes.
    int diskSizeGb = 10;

    // A link to the disk you want to use as a source for the new disk.
    // This value uses the following format:
    // "projects/{project_name}/zones/{zone}/disks/{disk_name}"
    String diskLink = String.format("projects/%s/zones/%s/disks/%s", "PROJECT_NAME", "ZONE",
        "DISK_NAME");

    // Customer-supplied encryption key used for encrypting data in the source disk.
    // The data will be encrypted with the same key in the new disk.
    byte[] encryptionKey = null;

    createDiskFromCustomerEncryptedKey(project, zone, diskName, diskType, diskSizeGb, diskLink,
        encryptionKey);
  }

  // Creates a zonal non-boot persistent disk in a project with the copy of data
  // from an existing disk.
  // The encryption key must be the same for the source disk and the new disk.
  public static void createDiskFromCustomerEncryptedKey(String project, String zone,
      String diskName, String diskType, int diskSizeGb, String diskLink, byte[] encryptionKey)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `disksClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (DisksClient disksClient = DisksClient.create()) {

      // Create a disk and set the encryption key.
      Disk disk = Disk.newBuilder()
          .setZone(zone)
          .setName(diskName)
          .setType(diskType)
          .setSizeGb(diskSizeGb)
          .setSourceDisk(diskLink)
          .setDiskEncryptionKey(CustomerEncryptionKey
              .newBuilder()
              .setRawKeyBytes(ByteString.copyFrom(encryptionKey))
              .build())
          .build();

      // Wait for the insert disk operation to complete.
      Operation operation = disksClient.insertAsync(
          InsertDiskRequest.newBuilder()
              .setProject(project)
              .setZone(zone)
              .setDiskResource(disk)
              .build()).get(3, TimeUnit.MINUTES);

      if (operation.hasError()) {
        System.out.println("Disk creation failed!");
        throw new Error(operation.getError().toString());
      }
      System.out.println(
          "Disk cloned with customer encryption key. Operation Status: " + operation.getStatus());
    }
  }
}
// [END compute_disk_clone_encrypted_disk]
