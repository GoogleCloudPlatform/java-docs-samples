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

// [START compute_ip_address_release_static_address]

import com.google.cloud.compute.v1.AddressesClient;
import com.google.cloud.compute.v1.DeleteAddressRequest;
import com.google.cloud.compute.v1.DeleteGlobalAddressRequest;
import com.google.cloud.compute.v1.GlobalAddressesClient;
import com.google.cloud.compute.v1.Operation;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ReleaseStaticAddress {

  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Google Cloud project you want to use.
    String projectId = "your-project-id";
    // The region to reserve the IP address in, if regional. Must be None if global
    String region = "your-region =";
    // Name of the address to release.
    String addressName = "your-addressName";

    releaseStaticAddress(projectId, addressName, region);
  }

  // Releases a static external IP address that is currently reserved.
  // This action requires that the address is not being used by any forwarding rule.
  public static void releaseStaticAddress(String projectId, String addressName, String region)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Operation operation;
    // Use global client if no region is specified
    if (region == null) {
      // Initialize client that will be used to send requests. This client only needs to be created
      // once, and can be reused for multiple requests.
      try (GlobalAddressesClient client = GlobalAddressesClient.create()) {
        DeleteGlobalAddressRequest request = DeleteGlobalAddressRequest.newBuilder()
                .setProject(projectId)
                .setAddress(addressName)
                .build();

        operation = client.deleteCallable().futureCall(request).get(30, TimeUnit.SECONDS);
      }
    } else {
      // Initialize client that will be used to send requests. This client only needs to be created
      // once, and can be reused for multiple requests.
      try (AddressesClient client = AddressesClient.create()) {
        DeleteAddressRequest request = DeleteAddressRequest.newBuilder()
                .setProject(projectId)
                .setRegion(region)
                .setAddress(addressName)
                .build();

        operation = client.deleteCallable().futureCall(request).get(30, TimeUnit.SECONDS);
      }
    }
    if (operation.hasError()) {
      System.out.printf("Can't release external IP address '%s'. Caused by : %s",
              addressName, operation.getError());
    }
    System.out.printf("External IP address '%s' released successfully.", addressName);
  }
}
// [END compute_ip_address_release_static_address]