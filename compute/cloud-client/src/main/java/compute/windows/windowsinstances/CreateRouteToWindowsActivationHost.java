// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package compute.windows.windowsinstances;

// [START compute_create_route_windows_activation]

import com.google.cloud.compute.v1.InsertRouteRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Route;
import com.google.cloud.compute.v1.RoutesClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CreateRouteToWindowsActivationHost {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    // projectId - ID or number of the project you want to use.
    String projectId = "your-google-cloud-project-id";

    // routeName - Name of the route you want to create.
    String routeName = "route-name";

    // networkName - Name of the network you want the new instance to use.
    //  *   For example: "global/networks/default" represents the network
    //  *   named "default", which is created automatically for each project.
    String networkName = "global/networks/default";

    createRouteToWindowsActivationHost(projectId, routeName, networkName);
  }

  // Creates a new route to kms.windows.googlecloud.com (35.190.247.13) for Windows activation.
  public static void createRouteToWindowsActivationHost(String projectId, String routeName,
      String networkName)
      throws IOException, ExecutionException, InterruptedException {
    // Instantiates a client.
    try (RoutesClient routesClient = RoutesClient.create()) {

      // If you have Windows instances without external IP addresses,
      // you must also enable Private Google Access so that instances
      // with only internal IP addresses can send traffic to the external
      // IP address for kms.windows.googlecloud.com.
      // More information: https://cloud.google.com/vpc/docs/configure-private-google-access#enabling
      Route route = Route.newBuilder()
          .setName(routeName)
          .setDestRange("35.190.247.13/32")
          .setNetwork(networkName)
          .setNextHopGateway(
              String.format("projects/%s/global/gateways/default-internet-gateway", projectId))
          .build();

      InsertRouteRequest request = InsertRouteRequest.newBuilder()
          .setProject(projectId)
          .setRouteResource(route)
          .build();

      // Wait for the operation to complete.
      Operation operation = routesClient.insertAsync(request).get();

      if (operation.hasError()) {
        System.out.printf("Error in creating route %s", operation.getError());
        return;
      }

      System.out.printf("Route created %s", routeName);
    }
  }
}
// [END compute_create_route_windows_activation]