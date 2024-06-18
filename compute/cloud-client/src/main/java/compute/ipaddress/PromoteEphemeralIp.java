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

// [START compute_ip_address_promote_ephemeral]

import com.google.cloud.compute.v1.Address;
import com.google.cloud.compute.v1.Address.AddressType;
import com.google.cloud.compute.v1.AddressesClient;
import com.google.cloud.compute.v1.InsertAddressRequest;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PromoteEphemeralIp {

  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "your-project-id";
    // Region where the VM and IP is located.
    String region = "your-region-id";
    // Ephemeral IP address to promote.
    String ephemeralIp = "your-ephemeralIp";
    // Name of the address to assign.
    String addressName = "your-addressName";

    promoteEphemeralIp(projectId, region, ephemeralIp, addressName);
  }

  // Promote ephemeral IP found on the instance to a static IP.
  public static List<Address> promoteEphemeralIp(String projectId, String region,
                                                 String ephemeralIp, String addressName)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (AddressesClient client = AddressesClient.create()) {
      Address addressResource = Address.newBuilder()
              .setName(addressName)
              .setRegion(region)
              .setAddressType(AddressType.EXTERNAL.name())
              .setAddress(ephemeralIp)
              .build();

      InsertAddressRequest addressRequest = InsertAddressRequest.newBuilder()
              .setRegion(region)
              .setProject(projectId)
              .setAddressResource(addressResource)
              .setRequestId(UUID.randomUUID().toString())
              .build();

      client.insertCallable().futureCall(addressRequest).get(30, TimeUnit.SECONDS);

      return Lists.newArrayList(client.list(projectId, region).iterateAll());
    }
  }
}
// [END compute_ip_address_promote_ephemeral]