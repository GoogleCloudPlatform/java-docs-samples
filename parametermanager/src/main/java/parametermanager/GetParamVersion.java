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

// [START parametermanager_get_param_version]

import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterVersion;
import com.google.cloud.parametermanager.v1.ParameterVersionName;
import java.io.IOException;

/**
 * This class demonstrates how to get a parameter version using the Parameter Manager SDK for GCP.
 */
public class GetParamVersion {
  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String parameterId = "your-parameter-id";
    String versionId = "your-version-id";

    // Call the method to get a parameter version.
    getParamVersion(projectId, parameterId, versionId);
  }

  // This is an example snippet for getting a parameter version.
  public static ParameterVersion getParamVersion(
      String projectId, String parameterId, String versionId) throws IOException {
    // Initialize the client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      String locationId = "global";

      // Build the parameter version name.
      ParameterVersionName parameterVersionName =
          ParameterVersionName.of(projectId, locationId, parameterId, versionId);

      // Get the parameter version.
      ParameterVersion parameterVersion =
          client.getParameterVersion(parameterVersionName.toString());
      // Find more details for the Parameter Version object here:
      // https://cloud.google.com/secret-manager/parameter-manager/docs/reference/rest/v1/projects.locations.parameters.versions#ParameterVersion
      System.out.printf(
          "Found parameter version %s with state %s\n",
          parameterVersion.getName(), (parameterVersion.getDisabled() ? "disabled" : "enabled"));
      if (!parameterVersion.getDisabled()) {
        System.out.printf("Payload: %s\n", parameterVersion.getPayload().getData().toStringUtf8());
      }
      return parameterVersion;
    }
  }
}
// [END parametermanager_get_param_version]
