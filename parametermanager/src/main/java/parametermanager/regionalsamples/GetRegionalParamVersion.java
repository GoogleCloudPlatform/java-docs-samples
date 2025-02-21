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

// [START parametermanager_get_regional_param_version]

import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerSettings;
import com.google.cloud.parametermanager.v1.ParameterVersion;
import com.google.cloud.parametermanager.v1.ParameterVersionName;
import java.io.IOException;

/**
 * This class demonstrates how to get a regional parameter version using the Parameter Manager SDK
 * for GCP.
 */
public class GetRegionalParamVersion {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String parameterId = "your-parameter-id";
    String versionId = "your-version-id";

    // Call the method to get a regional parameter version.
    getRegionalParamVersion(projectId, locationId, parameterId, versionId);
  }

  // This is an example snippet that gets a regional parameter version.
  public static ParameterVersion getRegionalParamVersion(
      String projectId, String locationId, String parameterId, String versionId)
      throws IOException {
    // Endpoint to call the regional parameter manager server
    String apiEndpoint = String.format("parametermanager.%s.rep.googleapis.com:443", locationId);
    ParameterManagerSettings parameterManagerSettings =
        ParameterManagerSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Initialize the client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create(parameterManagerSettings)) {
      // Build the parameter version name.
      ParameterVersionName parameterVersionName =
          ParameterVersionName.of(projectId, locationId, parameterId, versionId);

      // Get the parameter version.
      ParameterVersion parameterVersion =
          client.getParameterVersion(parameterVersionName.toString());
      System.out.printf(
          "Retrieved regional parameter version %s with data: %s\n",
          parameterVersion.getName(), parameterVersion.getPayload().getData().toStringUtf8());

      return parameterVersion;
    }
  }
}
// [END parametermanager_get_regional_param_version]
