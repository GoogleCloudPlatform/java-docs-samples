/*
 * Copyright 2022 Google LLC
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

package compute.deleteprotection;

// [START compute_delete_protection_set]

import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.SetDeletionProtectionInstanceRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class SetDeleteProtection {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // project: project ID or project number of the Cloud project you want to use.
    // zone: name of the zone you want to use. For example: “us-west3-b”
    // instanceName: name of the new virtual machine.
    // deleteProtection: boolean value indicating if the new virtual machine should be
    // protected against deletion or not.
    String projectId = "your-project-id-or-number";
    String zone = "zone-name";
    String instanceName = "instance-name";
    boolean deleteProtection = true;
    setDeleteProtection(projectId, zone, instanceName, deleteProtection);
  }

  // Updates the "Delete Protection" setting of given instance.
  public static void setDeleteProtection(String projectId, String zone,
      String instanceName, boolean deleteProtection)
      throws IOException, ExecutionException, InterruptedException {

    try (InstancesClient instancesClient = InstancesClient.create()) {

      SetDeletionProtectionInstanceRequest request =
          SetDeletionProtectionInstanceRequest.newBuilder()
              .setProject(projectId)
              .setZone(zone)
              .setResource(instanceName)
              .setDeletionProtection(deleteProtection)
              .build();

      instancesClient.setDeletionProtectionAsync(request).get();
      // Retrieve the updated setting from the instance.
      System.out.printf("Updated Delete Protection setting: %s",
          instancesClient.get(projectId, zone, instanceName).getDeletionProtection());
    }
  }
}

// [END compute_delete_protection_set]