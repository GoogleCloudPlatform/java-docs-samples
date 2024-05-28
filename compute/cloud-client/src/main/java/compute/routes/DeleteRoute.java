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

// [START compute_route_delete]

import com.google.cloud.compute.v1.DeleteRouteRequest;
import com.google.cloud.compute.v1.RoutesClient;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteRoute {

  public static void main(String[] args)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-project-id";
    // Route name you want to delete.
    String routeName = "your-route-name";

    deleteRoute(projectId, routeName);
  }

  // Deletes a route from a project.
  public static void deleteRoute(String projectId, String routeName)
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (RoutesClient routesClient = RoutesClient.create()) {
      DeleteRouteRequest request = DeleteRouteRequest.newBuilder()
              .setProject(projectId)
              .setRoute(routeName)
              .setRequestId(UUID.randomUUID().toString())
              .build();
      routesClient.deleteCallable().futureCall(request).get(30, TimeUnit.SECONDS);
    }
  }
}
// [END compute_route_delete]