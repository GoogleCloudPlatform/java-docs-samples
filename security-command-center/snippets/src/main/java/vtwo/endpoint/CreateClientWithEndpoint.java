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

package vtwo.endpoint;

// [START securitycenter_set_client_endpoint_v2]

import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import com.google.cloud.securitycenter.v2.SecurityCenterSettings;
import java.io.IOException;

public class CreateClientWithEndpoint {

  public static void main(String[] args) throws IOException {
    // TODO: Replace the value with your regional endpoint.
    String regionalEndpoint = "securitycenter.REGION.rep.googleapis.com:443";

    createClientWithEndpoint(regionalEndpoint);
  }

  // Creates Security Command Center clients for the default endpoint and for a
  // regional endpoint.
  public static void createClientWithEndpoint(String regionalEndpoint) throws java.io.IOException {
    SecurityCenterSettings regionalSettings =
        SecurityCenterSettings.newBuilder().setEndpoint(regionalEndpoint).build();

    try (SecurityCenterClient client = SecurityCenterClient.create();
        SecurityCenterClient regionalClient = SecurityCenterClient.create(regionalSettings)) {
      System.out.println("Client initiated with endpoint: " + client.getSettings().getEndpoint());
      System.out.println(
          "Regional client initiated with endpoint: " + regionalClient.getSettings().getEndpoint());
    }
  }
}

// [END securitycenter_set_client_endpoint_v2]
