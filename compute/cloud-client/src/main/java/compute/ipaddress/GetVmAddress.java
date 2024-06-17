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

// [START compute_ip_address_get_vm_address]

import com.google.cloud.compute.v1.AccessConfig;
import com.google.cloud.compute.v1.AccessConfig.Type;
import com.google.cloud.compute.v1.GetInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetVmAddress {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "your-project-id";
    // Instance ID of the Google Cloud project you want to use.
    String instanceId = "your-instance-id";
    // IPType you want to search.
    IpType ipType = IpType.INTERNAL;

    getVmAddress(projectId, instanceId, ipType);
  }

  // Retrieves the specified type of IP address
  // (ipv6, internal or external) of a specified Compute Engine instance.
  public static List<String> getVmAddress(String projectId, String instanceId, IpType ipType)
          throws IOException {
    List<String> result = new ArrayList<>();
    Instance instance = getInstance(projectId, instanceId);

    for (NetworkInterface networkInterface : instance.getNetworkInterfacesList()) {
      if (ipType == IpType.EXTERNAL) {
        for (AccessConfig accessConfig : networkInterface.getAccessConfigsList()) {
          if (accessConfig.getType().equals(Type.ONE_TO_ONE_NAT.name())) {
            result.add(accessConfig.getNatIP());
          }
        }
      } else if (ipType == IpType.IP_V6) {
        for (AccessConfig accessConfig : networkInterface.getAccessConfigsList()) {
          if (accessConfig.hasExternalIpv6()
                  && accessConfig.getType().equals(Type.DIRECT_IPV6.name())) {
            result.add(accessConfig.getExternalIpv6());
          }
        }
      } else if (ipType == IpType.INTERNAL) {
        result.add(networkInterface.getNetworkIP());
      }
    }

    return result;
  }

  private static Instance getInstance(String projectId, String instanceId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (InstancesClient instancesClient = InstancesClient.create()) {
      GetInstanceRequest request = GetInstanceRequest.newBuilder()
              .setInstance(instanceId)
              .setProject(projectId)
              .setZone("us-central1-b")
              .build();
      return instancesClient.get(request);
    }
  }

  public enum IpType {
    INTERNAL("internal"),
    EXTERNAL("external"),
    IP_V6("ipv6");

    private final String type;

    IpType(String type) {
      this.type = type;
    }

    public String getType() {
      return type;
    }
  }
}
// [END compute_ip_address_get_vm_address]