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

// [START parametermanager_get_regional_param]

import com.google.cloud.parametermanager.v1.Parameter;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerSettings;
import com.google.cloud.parametermanager.v1.ParameterName;
import java.io.IOException;

/**
 * This class demonstrates how to get a regional parameter using the Parameter Manager SDK for GCP.
 */
public class GetRegionalParam {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String parameterId = "your-parameter-id";

    // Call the method to get a regional parameter.
    getRegionalParam(projectId, locationId, parameterId);
  }

  // This is an example snippet that gets a regional parameter.
  public static Parameter getRegionalParam(String projectId, String locationId, String parameterId)
      throws IOException {
    // Endpoint to call the regional parameter manager server
    String apiEndpoint = String.format("parametermanager.%s.rep.googleapis.com:443", locationId);
    ParameterManagerSettings parameterManagerSettings =
        ParameterManagerSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Initialize the client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create(parameterManagerSettings)) {
      // Build the parameter name.
      ParameterName parameterName = ParameterName.of(projectId, locationId, parameterId);

      // Get the parameter.
      Parameter parameter = client.getParameter(parameterName.toString());
      // Find more details for the Parameter object here:
      // https://cloud.google.com/secret-manager/parameter-manager/docs/reference/rest/v1/projects.locations.parameters#Parameter
      System.out.printf(
          "Found the regional parameter %s with format %s\n",
          parameter.getName(), parameter.getFormat());

      return parameter;
    }
  }
}
// [END parametermanager_get_regional_param]
