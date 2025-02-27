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

package parametermanager.regionalsamples;

// [START parametermanager_list_regional_params]

import com.google.cloud.parametermanager.v1.LocationName;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerClient.ListParametersPagedResponse;
import com.google.cloud.parametermanager.v1.ParameterManagerSettings;
import java.io.IOException;

/** Class to demonstrate listing parameters regionally using the Parameter Manager SDK for GCP. */
public class ListRegionalParams {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";

    // Call the method to list parameters regionally.
    listRegionalParams(projectId, locationId);
  }

  // This is an example snippet that list all parameters in a given region.
  public static ListParametersPagedResponse listRegionalParams(String projectId, String locationId)
      throws IOException {
    // Endpoint to call the regional parameter manager server
    String apiEndpoint = String.format("parametermanager.%s.rep.googleapis.com:443", locationId);
    ParameterManagerSettings parameterManagerSettings =
        ParameterManagerSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Initialize the client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create(parameterManagerSettings)) {
      // Build the parent name from the project.
      LocationName location = LocationName.of(projectId, locationId);

      // Get all parameters.
      ListParametersPagedResponse response = client.listParameters(location.toString());

      // List all parameters.
      response
          .iterateAll()
          .forEach(parameter ->
                  System.out.printf("Found regional parameter %s with format %s\n",
                          parameter.getName(), parameter.getFormat()));

      return response;
    }
  }
}
// [END parametermanager_list_regional_params]
