/*
 * Copyright 2024 Google LLC
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

package compute.spots;

// [START compute_spot_check]

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Scheduling;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class CheckSpot {
  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-project-id";
    // Name of the virtual machine to check.
    String instanceName = "your-route-name";
    // Name of the zone you want to use. For example: "us-west3-b"
    String zone = "your-zone";

    isSpotVm(projectId, instanceName, zone);
  }

  // Check if a given instance is Spot VM or not.
  public static boolean isSpotVm(String projectId, String instanceName, String zone)
          throws IOException {
    try (InstancesClient client = InstancesClient.create()) {
      Instance instance = client.get(projectId, zone, instanceName);

      return instance.getScheduling().getProvisioningModel()
              .equals(Scheduling.ProvisioningModel.SPOT.name());
    }
  }
}
// [END compute_spot_check]