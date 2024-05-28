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

package compute.routes;

// [START compute_route_create]

import com.google.cloud.compute.v1.InsertRouteRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Route;
import com.google.cloud.compute.v1.RoutesClient;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CreateRoute {

  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-project-id";
    // Route name you want to use.
    String routeName = "your-route-name";
    createRoute(projectId, routeName);
  }

  // Create route for a project.
  public static Operation.Status createRoute(String projectId, String routeName)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (RoutesClient routesClient = RoutesClient.create()) {
      String nextHopGateway =
              String.format("projects/%s/global/gateways/default-internet-gateway", projectId);

      Route route = Route.newBuilder()
              .setName(routeName)
              .setDestRange("10.0.0.0/16")
              .setNetwork("global/networks/default")
              .setNextHopGateway(nextHopGateway)
              .build();

      InsertRouteRequest request = InsertRouteRequest.newBuilder()
              .setProject(projectId)
              .setRequestId(UUID.randomUUID().toString())
              .setRouteResource(route)
              .build();

      return routesClient.insertCallable().futureCall(request)
              .get(30, TimeUnit.SECONDS).getStatus();
    }
  }
}
// [END compute_route_create]
