/*
 * Copyright 2024 Google LLC
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

package compute.ipaddress;

// [START compute_ip_address_assign_static_existing_vm]

import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.AccessConfig.Type;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AssignStaticExistingVm {

  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "your-project-id";
    // Instance ID of the Google Cloud project you want to use.
    String instanceId = "your-instance-id";
    // Name of the zone to create the instance in. For example: "us-west3-b"
    String zone = "your-zone-id";
    // Name of the network interface to assign.
    String netInterfaceName = "your-netInterfaceName-id";

    assignStaticExistingVmAddress(projectId, instanceId, zone, netInterfaceName);
  }

  // Updates or creates an access configuration for a VM instance to assign a static external IP.
  // As network interface is immutable - deletion stage is required
  // in case of any assigned ip (static or ephemeral).
  // VM and ip address must be created before calling this function.
  // IMPORTANT: VM and assigned IP must be in the same region.
  public static Instance assignStaticExistingVmAddress(String projectId, String instanceId,
                                                       String zone, String netInterfaceName)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (InstancesClient client = InstancesClient.create()) {
      Instance instance = client.get(projectId, zone, instanceId);

      NetworkInterface networkInterface = null;
      for (NetworkInterface netInterface : instance.getNetworkInterfacesList()) {
        if (netInterface.getName().equals(netInterfaceName)) {
          networkInterface = netInterface;
          break;
        }
      }

      if (networkInterface == null) {
        throw new IllegalArgumentException(
                String.format(
                        "No '{network_interface_name}' variable found on instance %s.",
                        instanceId)
        );
      }
      AccessConfig accessConfig = null;
      for (AccessConfig config : networkInterface.getAccessConfigsList()) {
        if (config.getType().equals(Type.ONE_TO_ONE_NAT.name())) {
          accessConfig = config;
          break;
        }
      }

      if (accessConfig != null) {
        // Delete the existing access configuration first
        client.deleteAccessConfigAsync(projectId, zone, instanceId,
                        accessConfig.getName(), netInterfaceName)
                .get(30, TimeUnit.SECONDS);
      }

      // Add a new access configuration with the new IP
      AccessConfig newAccessConfig = AccessConfig.newBuilder()
              // Leave this field undefined to use an IP from a shared ephemeral IP address pool
              // .setNatIP(ipAddress)
              .setType(Type.ONE_TO_ONE_NAT.name())
              .setName("external-nat")
              .build();

      client.addAccessConfigAsync(projectId, zone, instanceId, netInterfaceName, newAccessConfig)
              .get(30, TimeUnit.SECONDS);

      // return updated instance
      return client.get(projectId, zone, instanceId);
    }
  }
}
// [END compute_ip_address_assign_static_existing_vm]