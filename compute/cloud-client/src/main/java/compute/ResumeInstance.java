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

// [START compute_resume_instance]

import com.google.cloud.compute.v1.Instance.Status;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ResumeInstance {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // project: project ID or project number of the Cloud project your instance belongs to.
    // zone: name of the zone your instance belongs to.
    // instanceName: name of the instance your want to resume.

    String project = "your-project-id";
    String zone = "zone-name";
    String instanceName = "instance-name";

    resumeInstance(project, zone, instanceName);
  }

  // Resume a suspended Google Compute Engine instance (with unencrypted disks).
  // Instance state changes to RUNNING, if successfully resumed.
  public static void resumeInstance(String project, String zone, String instanceName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Instantiates a client.
    try (InstancesClient instancesClient = InstancesClient.create()) {

      String currentInstanceState = instancesClient.get(project, zone, instanceName).getStatus();

      // Check if the instance is currently suspended.
      if (currentInstanceState.equalsIgnoreCase(Status.SUSPENDED.toString())) {
        throw new RuntimeException(
            String.format("Only suspended instances can be resumed. Instance %s is in %s state.",
                instanceName, currentInstanceState));
      }

      Operation operation = instancesClient.resumeAsync(project, zone, instanceName)
          .get(300, TimeUnit.SECONDS);

      if (operation.hasError() || !instancesClient.get(project, zone, instanceName).getStatus()
          .equalsIgnoreCase(
              Status.RUNNING.toString())) {
        System.out.println("Cannot resume instance. Try again!");
        return;
      }

      System.out.printf("Instance resumed successfully ! %s", instanceName);
    }
  }
}
// [END compute_resume_instance]