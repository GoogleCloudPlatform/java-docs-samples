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

// [START compute_delete_protection_get]

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import java.io.IOException;

public class GetDeleteProtection {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // project: project ID or project number of the Cloud project you want to use.
    // zone: name of the zone you want to use. For example: “us-west3-b”
    // instanceName: name of the new virtual machine.
    String projectId = "your-project-id-or-number";
    String zone = "zone-name";
    String instanceName = "instance-name";
    getDeleteProtection(projectId, zone, instanceName);
  }

  // Returns the state of delete protection flag of given instance.
  public static boolean getDeleteProtection(String projectId, String zone,
      String instanceName) throws IOException {

    try (InstancesClient instancesClient = InstancesClient.create()) {
      Instance instance = instancesClient.get(projectId, zone, instanceName);
      boolean deleteProtection = instance.getDeletionProtection();
      System.out.printf("Retrieved Delete Protection setting for instance: %s : %s", instanceName,
          deleteProtection);

      return deleteProtection;
    }
  }
}

// [END compute_delete_protection_get]
