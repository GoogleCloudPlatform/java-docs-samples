/*
 * Copyright 2025 Google LLC
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

package parametermanager;

// [START parametermanager_list_param]

import com.google.cloud.parametermanager.v1.LocationName;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerClient.ListParametersPagedResponse;
import java.io.IOException;

/** Class to demonstrate listing parameter using the parameter manager SDK for GCP. */
public class ListParam {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";

    // Call the method to list parameters.
    listParam(projectId);
  }

  // This is an example snippet for listing all parameters in given project.
  public static ListParametersPagedResponse listParam(String projectId) throws IOException {
    // Initialize the client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      String locationId = "global";

      // Build the parent name from the project.
      LocationName location = LocationName.of(projectId, locationId);

      // Get all parameters.
      ListParametersPagedResponse response = client.listParameters(location.toString());

      // List all parameters.
      response
          .iterateAll()
          .forEach(parameter -> System.out.printf("Parameter: %s\n", parameter.getName()));

      return response;
    }
  }
}
// [END parametermanager_list_param]
