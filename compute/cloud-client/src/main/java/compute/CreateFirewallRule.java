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
    String project = "your-project-id";
    String firewallRuleName = "firewall-rule-name-" + UUID.randomUUID();
    createFirewall(project, firewallRuleName);
  }

  /* Creates a simple firewall rule allowing for incoming HTTP and HTTPS access from the entire Internet.
     Args:
     project: project Id or project number of the Cloud project you want to use.
     firewallRuleName: name of the rule that is created. */
  public static void createFirewall(String project, String firewallRuleName)
      throws IOException {
    try (FirewallsClient firewallsClient = FirewallsClient.create();
        GlobalOperationsClient operationsClient = GlobalOperationsClient.create()) {

      Firewall firewallRule = Firewall.newBuilder()
          .setName(firewallRuleName)
          .setDirection(Direction.INGRESS)
          .addAllowed(
              Allowed.newBuilder().addPorts("80").addPorts("443").setIPProtocol("tcp").build())
          .addSourceRanges("0.0.0.0/0")
          .setNetwork("global/networks/default")
          .setDescription("Allowing TCP traffic on port 80 and 443 from Internet.")
          .build();

    /* Note that the default value of priority for the firewall API is 1000.
       If you check the value of `firewallRule.getPriority()` at this point it
       will be equal to 0. If you want to create a rule that has priority == 0,
       you'll need to explicitly set it so: setPriority(0) */

      InsertFirewallRequest insertFirewallRequest = InsertFirewallRequest.newBuilder()
          .setFirewallResource(firewallRule)
          .setProject(project).build();

      Operation operation = firewallsClient.insert(insertFirewallRequest);
      operationsClient.wait(project, operation.getName());

      System.out.println("Firewall rule created successfully ! ");
    }
  }
}
// [END compute_firewall_create]
