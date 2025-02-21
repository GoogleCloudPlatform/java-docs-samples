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

// [START parametermanager_create_structured_regional_param]

import com.google.cloud.parametermanager.v1.LocationName;
import com.google.cloud.parametermanager.v1.Parameter;
import com.google.cloud.parametermanager.v1.ParameterFormat;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerSettings;
import java.io.IOException;

/**
 * Example class to create a new regional parameter with a specific format using the Parameter
 * Manager SDK for GCP.
 */
public class CreateStructuredRegionalParam {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String parameterId = "your-parameter-id";
    ParameterFormat format = ParameterFormat.JSON;

    // Call the method to create a regional parameter with the specified format.
    createStructuredRegionalParam(projectId, locationId, parameterId, format);
  }

  // This is an example snippet that creates a regional parameter with a specific format.
  public static Parameter createStructuredRegionalParam(
      String projectId, String locationId, String parameterId, ParameterFormat format)
      throws IOException {

    // Endpoint to call the regional parameter manager server
    String apiEndpoint = String.format("parametermanager.%s.rep.googleapis.com:443", locationId);
    ParameterManagerSettings parameterManagerSettings =
        ParameterManagerSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create(parameterManagerSettings)) {
      // Build the parent name from the project.
      LocationName location = LocationName.of(projectId, locationId);

      // Build the regional parameter to create with the provided format.
      Parameter parameter = Parameter.newBuilder().setFormat(format).build();

      // Create the regional parameter.
      Parameter createdParameter =
          client.createParameter(location.toString(), parameter, parameterId);
      System.out.printf("Created regional parameter %s\n", createdParameter.getName());

      return createdParameter;
    }
  }
}
// [END parametermanager_create_structured_regional_param]
