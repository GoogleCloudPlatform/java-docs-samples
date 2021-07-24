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

// [START compute_instances_list_all]

import com.google.cloud.compute.v1.AggregatedListInstancesRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.InstancesScopedList;
import java.io.IOException;
import java.util.Map;

public class ListAllInstances {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample
    String project = "your-project-id";
    listAllInstances(project);
  }

  // List all instances in the specified project ID.
  public static void listAllInstances(String project) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `instancesClient.close()` method on the client to 
    // safely clean up any remaining background resources.
    try (InstancesClient instancesClient = InstancesClient.create()) {

      // Use the `setMaxResults` parameter to limit the number of results that the API returns per response page.
      AggregatedListInstancesRequest aggregatedListInstancesRequest = AggregatedListInstancesRequest
          .newBuilder()
          .setProject(project)
          .setMaxResults(5)
          .build();

      InstancesClient.AggregatedListPagedResponse response = instancesClient
          .aggregatedList(aggregatedListInstancesRequest);

      // Despite using the `setMaxResults` parameter, you don't need to handle the pagination
      // yourself. The returned `AggregatedListPager` object handles pagination
      // automatically, requesting next pages as you iterate over the results.
      for (Map.Entry<String, InstancesScopedList> zoneInstances : response.iterateAll()) {
        // Instances scoped by each zone
        String zone = zoneInstances.getKey();
        if (!zoneInstances.getValue().getInstancesList().isEmpty()) {
          // zoneInstances.getKey() returns the fully qualified address.
          // Hence, strip it to get the zone name only
          System.out.println(
              String.format("Instances at %s: ", zone.substring(zone.lastIndexOf('/') + 1)));
          for (Instance instance : zoneInstances.getValue().getInstancesList()) {
            System.out.println(instance.getName());
          }
        }
      }
      System.out.println("####### Listing all instances complete #######");
    }
  }

}
// [END compute_instances_list_all]
