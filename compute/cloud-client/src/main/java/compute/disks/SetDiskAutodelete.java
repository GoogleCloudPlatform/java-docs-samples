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

// [START compute_disk_autodelete_change]

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.SetDiskAutoDeleteInstanceRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SetDiskAutodelete {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.

    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";

    // Name of the zone in which is the disk you want to modify.
    String zone = "europe-central2-b";

    // Name of the instance the disk is attached to.
    String instanceName = "YOUR_INSTANCE_NAME";

    // The name of the disk which flag you want to modify.
    String diskName = "YOUR_DISK_NAME";

    // The new value of the autodelete flag.
    boolean autoDelete = true;

    setDiskAutodelete(projectId, zone, instanceName, diskName, autoDelete);
  }

  // Sets the autodelete flag of a disk to given value.
  public static void setDiskAutodelete(String projectId, String zone, String instanceName,
      String diskName, boolean autoDelete)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `instancesClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (InstancesClient instancesClient = InstancesClient.create()) {

      // Retrieve the instance given by the instanceName.
      Instance instance = instancesClient.get(projectId, zone, instanceName);

      // Check if the instance contains a disk that matches the given diskName.
      boolean diskNameMatch = instance.getDisksList()
          .stream()
          .anyMatch(disk -> disk.getDeviceName().equals(diskName));

      if (!diskNameMatch) {
        throw new Error(
            String.format("Instance %s doesn't have a disk named %s attached", instanceName,
                diskName));
      }

      // Create the request object.
      SetDiskAutoDeleteInstanceRequest request = SetDiskAutoDeleteInstanceRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setInstance(instanceName)
          .setDeviceName(diskName)
          // Update the autodelete property.
          .setAutoDelete(autoDelete)
          .build();

      // Wait for the update instance operation to complete.
      Operation response = instancesClient.setDiskAutoDeleteAsync(request)
          .get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Failed to update Disk autodelete field!" + response);
        return;
      }
      System.out.println(
          "Disk autodelete field updated. Operation Status: " + response.getStatus());
    }
  }
}
// [END compute_disk_autodelete_change]