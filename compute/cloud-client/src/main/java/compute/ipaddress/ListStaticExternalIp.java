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

// [START compute_ip_address_list_static_external]

import com.google.cloud.compute.v1.Address;
import com.google.cloud.compute.v1.AddressesClient;
import com.google.cloud.compute.v1.GlobalAddressesClient;
import com.google.cloud.compute.v1.ListAddressesRequest;
import com.google.cloud.compute.v1.ListGlobalAddressesRequest;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ListStaticExternalIp {

  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "your-project-id";
    // Region where the VM and IP is located.
    String region = "your-region-id";

    listStaticExternalIp(projectId, region);
  }

  // Lists all static external IP addresses, either regional or global.
  public static List<Address> listStaticExternalIp(String projectId, String region)
          throws IOException {
    // Use regional client if a region is specified
    if (region != null) {
      // Initialize client that will be used to send requests. This client only needs to be created
      // once, and can be reused for multiple requests.
      try (AddressesClient client = AddressesClient.create()) {
        ListAddressesRequest request = ListAddressesRequest.newBuilder()
                .setProject(projectId)
                .setRegion(region)
                .build();

        return Lists.newArrayList(client.list(request).iterateAll());
      }
    } else {
      // Initialize client that will be used to send requests. This client only needs to be created
      // once, and can be reused for multiple requests.
      try (GlobalAddressesClient client = GlobalAddressesClient.create()) {
        ListGlobalAddressesRequest request = ListGlobalAddressesRequest.newBuilder()
                .setProject(projectId)
                .build();

        return Lists.newArrayList(client.list(request).iterateAll());
      }
    }
  }
}
// [END compute_ip_address_list_static_external]