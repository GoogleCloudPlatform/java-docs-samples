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

//  [START compute_instances_list]

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import java.io.IOException;

public class ListInstance {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample
    String project = "your-project-id";
    String zone = "zone-name";
    listInstances(project, zone);
  }

  // List all instances in the given zone in the specified project ID.
  public static void listInstances(String project, String zone) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the `instancesClient.close()` method on the client to 
    // safely clean up any remaining background resources.
    try (InstancesClient instancesClient = InstancesClient.create()) {
      // Set the project and zone to retrieve instances present in the zone.
      System.out.println(String.format("Listing instances from %s in %s:", project, zone));
      for (Instance zoneInstance : instancesClient.list(project, zone).iterateAll()) {
        System.out.println(zoneInstance.getName());
      }
      System.out.println("####### Listing instances complete #######");
    }
  }
}
//  [END compute_instances_list]
