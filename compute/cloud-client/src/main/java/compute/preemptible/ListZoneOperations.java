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

package compute.preemptible;

// [START compute_preemptible_history]

import com.google.cloud.compute.v1.ListZoneOperationsRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.ZoneOperationsClient;
import com.google.cloud.compute.v1.ZoneOperationsClient.ListPagedResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListZoneOperations {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // projectId: project ID or project number of the Cloud project you want to use.
    // zone: name of the zone you want to use. For example: “us-west3-b”
    // instanceName: name of the virtual machine to look for.
    String projectId = "your-project-id-or-number";
    String zone = "zone-name";
    String instanceName = "instance-name";

    preemptionHistory(projectId, zone, instanceName);
  }


  // List all recent operations that happened in given zone in a project. Optionally filter those
  // operations by providing a filter. More about using the filter can be found here:
  // https://cloud.google.com/compute/docs/reference/rest/v1/zoneOperations/list
  public static ListPagedResponse listZoneOperations(String projectId, String zone, String filter)
      throws IOException {

    try (ZoneOperationsClient zoneOperationsClient = ZoneOperationsClient.create()) {
      ListZoneOperationsRequest request = ListZoneOperationsRequest.newBuilder()
          .setProject(projectId)
          .setZone(zone)
          .setFilter(filter)
          .build();

      return zoneOperationsClient.list(request);
    }
  }


  // Get a list of preemption operations from given zone in a project. Optionally limit
  // the results to instance name.
  private static void preemptionHistory(String projectId, String zone, String instanceName)
      throws IOException {

    String filter;
    String thisInstanceName;
    String targetLink;
    List<List<String>> history = new ArrayList<>();

    if (instanceName.length() != 0) {
      filter = String.format(
          "operationType=\"compute.instances.preempted\" AND targetLink:instances/%s",
          instanceName);
    } else {
      filter = "operationType=\"compute.instances.preempted\"";
    }

    for (Operation operation : listZoneOperations(projectId, zone, filter).iterateAll()) {
      targetLink = operation.getTargetLink();
      thisInstanceName = targetLink.substring(targetLink.lastIndexOf("/") + 1);

      // The filter used is not 100% accurate, it's `contains` not `equals`
      // So we need to check the name to make sure it's the one we want.
      if (thisInstanceName.equalsIgnoreCase(instanceName)) {
        Instant instant = Instant.from(
            DateTimeFormatter.ISO_INSTANT.parse(operation.getInsertTime()));
        history.add(new ArrayList<>(Arrays.asList(instanceName, instant.toString())));
      }
    }

    System.out.println("Retrieved preemption history: " + history);
  }
}
// [END compute_preemptible_history]