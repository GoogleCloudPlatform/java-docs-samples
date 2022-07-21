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

package compute;

// [START compute_instances_get]

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import java.io.IOException;

public class GetInstance {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";

    // Name of the zone you want to use. For example: 'us-west3-b'.
    String zone = "europe-central2-b";

    // Name of the VM instance you want to query.
    String instanceName = "YOUR_INSTANCE_NAME";

    getInstance(projectId, zone, instanceName);
  }

  // Prints information about a VM instance in the given zone in the specified project.
  public static void getInstance(String projectId, String zone, String instanceName)
      throws IOException {

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `instancesClient.close()` method on the client to safely
    // clean up any remaining background resources.
    try (InstancesClient instancesClient = InstancesClient.create()) {

      Instance instance = instancesClient.get(projectId, zone, instanceName);

      System.out.printf("Retrieved the instance %s", instance.toString());
    }
  }
}
// [END compute_instances_get]