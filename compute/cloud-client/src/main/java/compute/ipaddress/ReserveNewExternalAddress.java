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

// [START compute_ip_address_reserve_new_external]

import com.google.cloud.compute.v1.Address;
import com.google.cloud.compute.v1.Address.AddressType;
import com.google.cloud.compute.v1.Address.IpVersion;
import com.google.cloud.compute.v1.Address.NetworkTier;
import com.google.cloud.compute.v1.AddressesClient;
import com.google.cloud.compute.v1.GlobalAddressesClient;
import com.google.cloud.compute.v1.InsertAddressRequest;
import com.google.cloud.compute.v1.InsertGlobalAddressRequest;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ReserveNewExternalAddress {

  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "your-project-id";
    // Address name you want to use.
    String addressName = "your-address-name";
    // 'IPV4' or 'IPV6' depending on the IP version. IPV6 if True. Option only for global regions.
    boolean ipV6 = false;
    // 'STANDARD' or 'PREMIUM' network tier. Standard option available only in regional ip.
    boolean isPremium = false;
    // region (Optional[str]): The region to reserve the IP address in, if regional.
    // Must be None if global.
    String region = null;

    reserveNewExternalIpAddress(projectId, addressName, ipV6, isPremium, region);
  }

  // Reserves a new external IP address in the specified project and region.
  public static List<Address> reserveNewExternalIpAddress(String projectId, String addressName,
                                                          boolean ipV6, boolean isPremium,
                                                          String region)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {

    String ipVersion = ipV6 ? IpVersion.IPV6.name() : IpVersion.IPV4.name();
    String networkTier = !isPremium && region != null
            ? NetworkTier.STANDARD.name() : NetworkTier.PREMIUM.name();

    Address.Builder address = Address.newBuilder()
            .setName(addressName)
            .setAddressType(AddressType.EXTERNAL.name())
            .setNetworkTier(networkTier);

    // Use global client if no region is specified
    if (region == null) {
      // Initialize client that will be used to send requests. This client only needs to be created
      // once, and can be reused for multiple requests.
      try (GlobalAddressesClient client = GlobalAddressesClient.create()) {
        address.setIpVersion(ipVersion);

        InsertGlobalAddressRequest addressRequest = InsertGlobalAddressRequest.newBuilder()
                .setProject(projectId)
                .setRequestId(UUID.randomUUID().toString())
                .setAddressResource(address.build())
                .build();

        client.insertCallable().futureCall(addressRequest).get(30, TimeUnit.SECONDS);

        return Lists.newArrayList(client.list(projectId).iterateAll());
      }
    } else {
      // Initialize client that will be used to send requests. This client only needs to be created
      // once, and can be reused for multiple requests.
      try (AddressesClient client = AddressesClient.create()) {
        address.setRegion(region);

        InsertAddressRequest addressRequest = InsertAddressRequest.newBuilder()
                .setProject(projectId)
                .setRequestId(UUID.randomUUID().toString())
                .setAddressResource(address.build())
                .setRegion(region)
                .build();

        client.insertCallable().futureCall(addressRequest).get(30, TimeUnit.SECONDS);

        return Lists.newArrayList(client.list(projectId, region).iterateAll());
      }
    }
  }
}
// [END compute_ip_address_reserve_new_external]