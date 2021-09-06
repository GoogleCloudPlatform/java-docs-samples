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

import com.google.cloud.compute.v1.Firewall;
import com.google.cloud.compute.v1.FirewallsClient;
import java.io.IOException;
import java.util.UUID;

public class GetFirewallRule {


  public static void main(String[] args) throws IOException {
    String project = "your-project-id";
    String firewallRuleName = "firewall-rule-name-" + UUID.randomUUID();
    getFirewallRule(project, firewallRuleName);
  }


  /* Retrieves the firewall rule given by the firewallRuleName if present.
     Args:
     project: project Id or project number of the Cloud project you want to use.
     firewallRuleName: name of the rule that is created. */
  public static void getFirewallRule(String project, String firewallRuleName)
      throws IOException {
    try (FirewallsClient firewallsClient = FirewallsClient.create()) {
      Firewall response = firewallsClient.get(project, firewallRuleName);
      System.out.println(response);
    }
  }
}
