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

// [START compute_route_list]

import com.google.cloud.compute.v1.ListRoutesRequest;
import com.google.cloud.compute.v1.Route;
import com.google.cloud.compute.v1.RoutesClient;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;

public class ListRoute {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "your-project-id";

    listRoutes(projectId);
  }

  // Lists routes from a project.
  public static List<Route> listRoutes(String projectId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (RoutesClient routesClient = RoutesClient.create()) {
      ListRoutesRequest request = ListRoutesRequest.newBuilder()
              .setProject(projectId)
              .build();

      return Lists.newArrayList(routesClient.list(request).iterateAll());
    }
  }
}
// [END compute_route_list]