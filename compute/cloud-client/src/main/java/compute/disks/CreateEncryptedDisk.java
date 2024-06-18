// Copyright 2024 Google LLC
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

// [START compute_create_encrypted_disk]

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

public class CreateEncryptedDisk {
  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the zone in which you want to create the disk.
    String zone = "europe-central2-b";
    // Name of the disk you want to create.
    String diskName = "YOUR_DISK_NAME";
    // The type of disk you want to create. This value uses the following format:
    // "zones/{zone}/diskTypes/(pd-standard|pd-ssd|pd-balanced|pd-extreme)".
    // For example: "zones/us-west3-b/diskTypes/pd-ssd"
    String diskType = String.format("zones/%s/diskTypes/pd-ssd", zone);
    // Size of the new disk in gigabytes.
    long diskSizeGb = 10;
    // Customer-supplied encryption key used for encrypting data in the source disk.
    // The data will be encrypted with the same key in the new disk.
    byte[] encryptionKey = null;

    createEncryptedDisk(projectId, zone, diskName, diskType, diskSizeGb, encryptionKey);
  }

  // Creates a zonal non-boot persistent disk in a project
  public static Disk createEncryptedDisk(String projectId, String zone, String diskName,
                                         String diskType, long diskSizeGb, byte[] encryptionKey)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (DisksClient client = DisksClient.create()) {
      // Create a disk and set the encryption key.
      Disk disk = Disk.newBuilder()
              .setZone(zone)
              .setName(diskName)
              .setType(diskType)
              .setSizeGb(diskSizeGb)
              .setDiskEncryptionKey(CustomerEncryptionKey
                      .newBuilder()
                      .setRawKeyBytes(ByteString.copyFrom(encryptionKey))
                      .build())
              .build();

      InsertDiskRequest request = InsertDiskRequest.newBuilder()
              .setProject(projectId)
              .setZone(zone)
              .setDiskResource(disk)
              .build();

      // Wait for the insert disk operation to complete.
      Operation operation = client.insertAsync(request).get(1, TimeUnit.MINUTES);

      if (operation.hasError()) {
        System.out.println("Disk creation failed!");
        throw new Error(operation.getError().toString());
      }

      // Wait for server update
      TimeUnit.SECONDS.sleep(10);

      Disk encrypted = client.get(projectId, zone, diskName);

      System.out.printf("Encrypted disk '%s' has been created successfully", encrypted.getName());

      return encrypted;
    }
  }
}
// [END compute_create_encrypted_disk]