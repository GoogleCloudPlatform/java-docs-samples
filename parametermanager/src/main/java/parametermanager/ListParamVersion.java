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

// [START parametermanager_list_param_version]

import com.google.cloud.parametermanager.v1.ListParameterVersionsRequest;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerClient.ListParameterVersionsPagedResponse;
import com.google.cloud.parametermanager.v1.ParameterName;
import java.io.IOException;

/** Class to list parameter versions using the Parameter Manager SDK for GCP. */
public class ListParamVersion {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String parameterId = "your-parameter-id";

    // Call the method to list parameter versions.
    listParamVersion(projectId, parameterId);
  }

  // This is an example snippet that list all parameter versions
  public static ListParameterVersionsPagedResponse listParamVersion(
      String projectId, String parameterId) throws IOException {
    // Initialize the client that will be used to send requests. This client only needs to be
    // created once,
    // and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      String locationId = "global";

      // Build the parameter name from the project and parameter ID.
      ParameterName parameterName = ParameterName.of(projectId, locationId, parameterId);

      // Build the request to list parameter versions.
      ListParameterVersionsRequest request =
          ListParameterVersionsRequest.newBuilder().setParent(parameterName.toString()).build();

      // Send the request and get the response.
      ListParameterVersionsPagedResponse response = client.listParameterVersions(request);

      // Iterate through all versions and print their details.
      response
          .iterateAll()
          .forEach(
              version ->
                  System.out.printf(
                      "Parameter version: %s, Disabled: %s\n",
                      version.getName(), version.getDisabled()));

      return response;
    }
  }
}
// [END parametermanager_list_param_version]
