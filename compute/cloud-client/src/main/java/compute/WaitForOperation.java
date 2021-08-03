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

// [START compute_instances_operation_check]

import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.WaitZoneOperationRequest;
import com.google.cloud.compute.v1.ZoneOperationsClient;
import java.io.IOException;
import java.net.SocketTimeoutException;

public class WaitForOperation {

  public static void main(String[] args) throws IOException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // client-operation-id: Specify the operation id corresponding to a particular operation.
    String project = "your-project-id";
    String zone = "zone-name";
    String operationId = "client-operation-id";

    waitForOperation(project, zone, operationId);
  }

  // Waits for the specified operation to complete and returns the operation result in boolean.
  public static void waitForOperation(String project, String zone, String operationId)
      throws IOException, InterruptedException {
    try {
      // Construct the Operation instance.
      Operation operation = Operation.newBuilder().setClientOperationId(operationId).build();

      // Check if the operation hasn't been completed already.
      if (operation.getStatus() == Status.RUNNING || operation.getStatus() == Status.PENDING) {
        // Create a zone operations client.
        ZoneOperationsClient zoneOperationsClient = ZoneOperationsClient.create();

        // Create the wait request.
        WaitZoneOperationRequest waitZoneOperationRequest = WaitZoneOperationRequest.newBuilder()
            .setProject(project)
            .setZone(zone)
            .setOperation(String.valueOf(operation.getId())).build();

        // Wait for the operation to complete; default timeout is 2 mins.
        operation = zoneOperationsClient.wait(waitZoneOperationRequest);
        zoneOperationsClient.close();

        // Check if the operation has errors.
        if (operation.hasError()) {
          System.out.println("Error in executing the operation ! ! " + operation.getError());
          return;
        }
      }
      System.out.println("Operation executed successfully !");
    } catch (SocketTimeoutException e) {
      // Handle SocketTimeoutException which is being thrown as UnknownException.
      // (Operation will run to completion in the background)
      System.out.println("Operation executed successfully !");
    }
  }
}
// [END compute_instances_operation_check]
