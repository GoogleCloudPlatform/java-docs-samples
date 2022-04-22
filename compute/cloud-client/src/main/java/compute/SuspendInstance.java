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

package compute;

// [START compute_suspend_instance]

import com.google.cloud.compute.v1.Instance.Status;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SuspendInstance {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // project: project ID or project number of the Cloud project your instance belongs to.
    // zone: name of the zone your instance belongs to.
    // instanceName: name of the instance your want to suspend.

    String project = "your-project-id";
    String zone = "zone-name";
    String instanceName = "instance-name";

    suspendInstance(project, zone, instanceName);
  }

  // Suspend a running Google Compute Engine instance.
  // For limitations and compatibility on which instances can be suspended,
  // see: https://cloud.google.com/compute/docs/instances/suspend-resume-instance#limitations
  public static void suspendInstance(String project, String zone, String instanceName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Instantiates a client.
    try (InstancesClient instancesClient = InstancesClient.create()) {

      Operation operation = instancesClient.suspendAsync(project, zone, instanceName)
          .get(300, TimeUnit.SECONDS);

      if (operation.hasError() || !instancesClient.get(project, zone, instanceName).getStatus()
          .equalsIgnoreCase(Status.SUSPENDED.toString())) {
        System.out.println("Cannot suspend instance. Try again!");
        return;
      }

      System.out.printf("Instance suspended successfully ! %s", instanceName);
    }
  }
}
// [END compute_suspend_instance]