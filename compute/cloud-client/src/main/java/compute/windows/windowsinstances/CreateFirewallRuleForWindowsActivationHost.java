// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package compute.windows.windowsinstances;

// [START compute_create_egress_rule_windows_activation]

import com.google.cloud.compute.v1.Allowed;
import com.google.cloud.compute.v1.Firewall;
import com.google.cloud.compute.v1.FirewallsClient;
import com.google.cloud.compute.v1.InsertFirewallRequest;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateFirewallRuleForWindowsActivationHost {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // projectId - ID or number of the project you want to use.
    String projectId = "your-google-cloud-project-id";

    // firewallRuleName - Name of the firewall rule you want to create.
    String firewallRuleName = "firewall-rule-name";

    // networkName - Name of the network you want the new instance to use.
    //  *   For example: "global/networks/default" represents the network
    //  *   named "default", which is created automatically for each project.
    String networkName = "global/networks/default";

    createFirewallRuleForWindowsActivationHost(projectId, firewallRuleName, networkName);
  }

  // Creates a new allow egress firewall rule with the highest priority for host
  // kms.windows.googlecloud.com (35.190.247.13) for Windows activation.
  public static void createFirewallRuleForWindowsActivationHost(String projectId,
      String firewallRuleName, String networkName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Instantiates a client.
    try (FirewallsClient firewallsClient = FirewallsClient.create()) {

      Firewall firewall = Firewall.newBuilder()
          .setName(firewallRuleName)
          // These are the default values for kms.windows.googlecloud.com
          // See, https://cloud.google.com/compute/docs/instances/windows/creating-managing-windows-instances#firewall_rule_requirements
          .addAllowed(Allowed.newBuilder()
              .setIPProtocol("tcp")
              .addPorts("1688")
              .build())
          .setDirection("EGRESS")
          .setNetwork(networkName)
          .addDestinationRanges("35.190.247.13/32")
          .setPriority(0)
          .build();

      InsertFirewallRequest request = InsertFirewallRequest.newBuilder()
          .setProject(projectId)
          .setFirewallResource(firewall)
          .build();

      // Wait for the operation to complete.
      Operation operation = firewallsClient.insertAsync(request).get(3, TimeUnit.MINUTES);
      ;

      if (operation.hasError()) {
        System.out.println("Firewall rule creation failed ! ! " + operation.getError());
        return;
      }

      System.out.printf("Firewall rule created %s", firewallRuleName);
    }
  }
}
// [END compute_create_egress_rule_windows_activation]