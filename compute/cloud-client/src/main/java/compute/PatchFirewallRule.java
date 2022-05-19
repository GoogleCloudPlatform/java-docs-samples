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

// [START compute_firewall_patch]

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.Firewall;
import com.google.cloud.compute.v1.FirewallsClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.PatchFirewallRequest;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PatchFirewallRule {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample
    // project: project ID or project number of the Cloud project you want to use.
    // firewallRuleName: name of the rule you want to modify.
    // priority: the new priority to be set for the rule.
    String project = "your-project-id";
    String firewallRuleName = "firewall-rule-name-" + UUID.randomUUID();
    int priority = 10;

    patchFirewallPriority(project, firewallRuleName, priority);
  }

  // Modifies the priority of a given firewall rule.
  public static void patchFirewallPriority(String project, String firewallRuleName, int priority)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    /* Initialize client that will be used to send requests. This client only needs to be created
       once, and can be reused for multiple requests. After completing all of your requests, call
       the `firewallsClient.close()` method on the client to safely
       clean up any remaining background resources. */
    try (FirewallsClient firewallsClient = FirewallsClient.create()) {

      /* The patch operation doesn't require the full definition of a Firewall object. It will only 
         update the values that were set in it, in this case it will only change the priority. */
      Firewall firewall = Firewall.newBuilder()
          .setPriority(priority).build();

      PatchFirewallRequest patchFirewallRequest = PatchFirewallRequest.newBuilder()
          .setProject(project)
          .setFirewall(firewallRuleName)
          .setFirewallResource(firewall).build();

      OperationFuture<Operation, Operation> operation = firewallsClient.patchAsync(
          patchFirewallRequest);
      operation.get(3, TimeUnit.MINUTES);
      System.out.println("Firewall Patch applied successfully ! ");
    }
  }
}
// [END compute_firewall_patch]
