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

package compute.customhostname;

// [START compute_instances_get_hostname]

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import java.io.IOException;

public class GetInstanceHostname {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String project = "your-project-id";
    String zone = "zone-name"; // eg: "us-central1-a"
    String instanceName = "instance-name"; // Name of the VM instance to retrieve.

    getInstanceHostname(project, zone, instanceName);
  }

  // Retrieves the hostname of the Google Cloud VM instance.
  public static void getInstanceHostname(String projectId, String zone, String instanceName)
      throws IOException {
    try (InstancesClient instancesClient = InstancesClient.create()) {

      Instance instance = instancesClient.get(projectId, zone, instanceName);

      if (instance.hasHostname()) {
        // If a custom hostname is not set, the output for instance.getHostname() will be undefined.
        System.out.printf("Custom Hostname for the instance %s is: %s", instanceName,
            instance.getHostname());
      }
    }
  }

}
// [END compute_instances_get_hostname]