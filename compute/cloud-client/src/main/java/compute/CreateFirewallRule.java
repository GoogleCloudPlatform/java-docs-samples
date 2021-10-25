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

// [START compute_firewall_create]

import com.google.cloud.compute.v1.Allowed;
import com.google.cloud.compute.v1.Firewall;
import com.google.cloud.compute.v1.Firewall.Direction;
import com.google.cloud.compute.v1.FirewallsClient;
import com.google.cloud.compute.v1.GlobalOperationsClient;
import com.google.cloud.compute.v1.InsertFirewallRequest;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.UUID;

public class CreateFirewallRule {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample
    /* project: project ID or project number of the Cloud project you want to use.
       firewallRuleName: name of the rule that is created.
       network: name of the network the rule will be applied to. Available name formats:
        * https://www.googleapis.com/compute/v1/projects/{project_id}/global/networks/{network}
        * projects/{project_id}/global/networks/{network}
        * global/networks/{network} */
    String project = "your-project-id";
    String firewallRuleName = "firewall-rule-name-" + UUID.randomUUID();
    String network = "global/networks/default";

    // The rule will be created with default priority of 1000.
    createFirewall(project, firewallRuleName, network);
  }

  // Creates a simple firewall rule allowing for incoming HTTP and 
  // HTTPS access from the entire Internet.
  public static void createFirewall(String project, String firewallRuleName, String network)
      throws IOException {
    /* Initialize client that will be used to send requests. This client only needs to be created
       once, and can be reused for multiple requests. After completing all of your requests, call
       the `firewallsClient.close()` method on the client to safely
       clean up any remaining background resources. */
    try (FirewallsClient firewallsClient = FirewallsClient.create();
        GlobalOperationsClient operationsClient = GlobalOperationsClient.create()) {

      // The below firewall rule is created in the default network.
      Firewall firewallRule = Firewall.newBuilder()
          .setName(firewallRuleName)
          .setDirection(Direction.INGRESS)
          .addAllowed(
              Allowed.newBuilder().addPorts("80").addPorts("443").setIPProtocol("tcp").build())
          .addSourceRanges("0.0.0.0/0")
          .setNetwork(network)
          .setDescription("Allowing TCP traffic on port 80 and 443 from Internet.")
          .build();

      /* Note that the default value of priority for the firewall API is 1000.
         If you check the value of `firewallRule.getPriority()` at this point it
         will be equal to 0, however it is not treated as "set" by the library and thus
         the default will be applied to the new rule. If you want to create a rule that
         has priority == 0, you'll need to explicitly set it so: setPriority(0) */

      InsertFirewallRequest insertFirewallRequest = InsertFirewallRequest.newBuilder()
          .setFirewallResource(firewallRule)
          .setProject(project).build();

      Operation operation = firewallsClient.insert(insertFirewallRequest);
      operationsClient.wait(project, operation.getName());

      System.out.println("Firewall rule created successfully -> " + firewallRuleName);
    }
  }
}
// [END compute_firewall_create]
