/*
 * Copyright 2021 Google LLC
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

// [START compute_stop_instance]
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.StopInstanceRequest;
import com.google.cloud.compute.v1.ZoneOperationsClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class StopInstance {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    /* project: project ID or project number of the Cloud project your instance belongs to.
       zone: name of the zone your instance belongs to.
       instanceName: name of the instance your want to stop.
     */
    String project = "your-project-id";
    String zone = "zone-name";
    String instanceName = "instance-name";

    stopInstance(project, zone, instanceName);
  }

  // Stops a stopped Google Compute Engine instance.
  public static void stopInstance(String project, String zone, String instanceName)
      throws IOException, ExecutionException, InterruptedException {
    /* Initialize client that will be used to send requests. This client only needs to be created
       once, and can be reused for multiple requests. After completing all of your requests, call
       the `instancesClient.close()` method on the client to safely
       clean up any remaining background resources. */
    try (InstancesClient instancesClient = InstancesClient.create();
        ZoneOperationsClient zoneOperationsClient = ZoneOperationsClient.create()) {

      StopInstanceRequest stopInstanceRequest = StopInstanceRequest.newBuilder()
          .setProject(project)
          .setZone(zone)
          .setInstance(instanceName)
          .build();

      Operation operation = instancesClient.stopCallable()
          .futureCall(stopInstanceRequest)
          .get();

      Operation response = zoneOperationsClient.wait(project, zone, operation.getName());

      if(response.getStatus() == Status.DONE) {
        System.out.println("Instance stopped successfully ! ");
      }
    }
  }
}
// [END compute_stop_instance]