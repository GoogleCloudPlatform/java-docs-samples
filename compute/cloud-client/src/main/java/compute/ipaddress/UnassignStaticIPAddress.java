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

// [START compute_ip_address_unassign_static_address]

import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UnassignStaticIPAddress {

  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-project-id";
    // Instance ID of the Cloud project you want to use.
    String instanceId = "your-instance-id";
    // name of the zone to create the instance in. For example: "us-west3-b"
    String zone = "your-zone";
    // Name of the network interface to assign.
    String netInterfaceName = "your-netInterfaceName";

    unassignStaticIPAddress(projectId, instanceId, zone, netInterfaceName);
  }

  public static Instance unassignStaticIPAddress(String projectId, String instanceId,
                                                 String zone, String netInterfaceName)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (InstancesClient client = InstancesClient.create()) {
      Instance instance = client.get(projectId, zone, instanceId);
      NetworkInterface networkInterface = null;
      for (NetworkInterface netIterface : instance.getNetworkInterfacesList()) {
        if (netIterface.getName().equals(netInterfaceName)) {
          networkInterface = netIterface;
        }
      }

      if (networkInterface == null) {
        String errMsg = String
                .format("No network interface named '%s' found on instance %s.",
                        netInterfaceName, instanceId);
        System.out.println(errMsg);
        return null;
      }

      AccessConfig accessConfig = null;
      for (AccessConfig config : networkInterface.getAccessConfigsList()) {
        if (config.getType().equals(AccessConfig.Type.ONE_TO_ONE_NAT.name())) {
          accessConfig = config;
        }
      }

      if (accessConfig != null) {
        // Delete the existing access configuration first
        client.deleteAccessConfigAsync(projectId, zone, instanceId,
                        accessConfig.getName(), netInterfaceName).get(30, TimeUnit.SECONDS);
      }

      // return updated instance
      return client.get(projectId, zone, instanceId);
    }
  }
}
// [END compute_ip_address_unassign_static_address]