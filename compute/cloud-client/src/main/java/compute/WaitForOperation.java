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
import com.google.cloud.compute.v1.ZoneOperationsClient;
import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class WaitForOperation {

  public static void main(String[] args) throws IOException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // operation: Specify the operation to wait.
    String project = "your-project-id";
    Operation operation = Operation.newBuilder().build();

    waitForOperation(project, operation);
  }

  // Waits for the specified operation to complete.
  public static void waitForOperation(String project, Operation operation)
      throws IOException, InterruptedException {
    try (ZoneOperationsClient zoneOperationsClient = ZoneOperationsClient.create()) {

      // Check if the operation hasn't been completed already.
      if (operation.getStatus() != Status.DONE) {
        String zone = operation.getZone();
        zone = zone.substring(zone.lastIndexOf("/") + 1);

        // Wait for the operation to complete.
        // Timeout is set at 3 minutes.
        LocalTime endTime = LocalTime.now().plusMinutes(3);
        while (operation.getStatus() != Status.DONE
            && LocalTime.now().isBefore(endTime)) {
          operation = zoneOperationsClient.get(project, zone, String.valueOf(operation.getId()));
          TimeUnit.SECONDS.sleep(3);
        }

        // Check if the operation has errors.
        if (operation.hasError()) {
          System.out.println("Error in executing the operation ! ! " + operation.getError());
          return;
        }
      }
      System.out.println("Operation Status: " + operation.getStatus());
    }
  }
}
// [END compute_instances_operation_check]