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

// [START compute_firewall_list]

import com.google.cloud.compute.v1.Firewall;
import com.google.cloud.compute.v1.FirewallsClient;
import com.google.cloud.compute.v1.FirewallsClient.ListPagedResponse;
import java.io.IOException;

public class ListFirewallRules {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample
    // project: project ID or project number of the Cloud project you want to use.
    String project = "your-project-id";
    listFirewallRules(project);
  }

  // Return a list of all the firewall rules in specified project.
  public static ListPagedResponse listFirewallRules(String project)
      throws IOException {
    /* Initialize client that will be used to send requests. This client only needs to be created
       once, and can be reused for multiple requests. After completing all of your requests, call
       the `instancesClient.close()` method on the client to safely
       clean up any remaining background resources. */
    try (FirewallsClient firewallsClient = FirewallsClient.create()) {
      ListPagedResponse firewallResponse = firewallsClient.list(project);
      for (Firewall firewall : firewallResponse.iterateAll()) {
        System.out.println(firewall.getName());
      }
      return firewallResponse;
    }
  }
}
// [END compute_firewall_list]
