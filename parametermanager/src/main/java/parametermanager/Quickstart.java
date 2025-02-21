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

// [START parametermanager_quickstart]

import com.google.cloud.parametermanager.v1.LocationName;
import com.google.cloud.parametermanager.v1.Parameter;
import com.google.cloud.parametermanager.v1.ParameterFormat;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterName;
import com.google.cloud.parametermanager.v1.ParameterVersion;
import com.google.cloud.parametermanager.v1.ParameterVersionName;
import com.google.cloud.parametermanager.v1.ParameterVersionPayload;
import com.google.protobuf.ByteString;

public class Quickstart {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String parameterId = "your-parameter-id";
    String versionId = "your-version-id";
    String jsonPayload = "{\"username\": \"test-user\", \"host\": \"localhost\"}";

    // Run the quickstart method
    quickstart(projectId, parameterId, versionId, jsonPayload);
  }

  // This is an example snippet of how to use the basic capabilities in the Parameter Manager API.
  public static void quickstart(
      String projectId, String parameterId, String versionId, String jsonPayload) throws Exception {

    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      String locationId = "global";

      // Step 1: Create a parameter.
      // Build the parent name from the project.
      LocationName location = LocationName.of(projectId, locationId);

      // Specify the parameter format.
      ParameterFormat format = ParameterFormat.JSON;
      // Build the parameter to create.
      Parameter parameter = Parameter.newBuilder().setFormat(format).build();

      // Create the parameter.
      Parameter createdParameter =
          client.createParameter(location.toString(), parameter, parameterId);
      System.out.printf("Created parameter %s\n", createdParameter.getName());

      // Step 2: Create a parameter version with JSON payload containing a secret reference.
      // Build the parameter name.
      ParameterName parameterName = ParameterName.of(projectId, locationId, parameterId);

      // Convert the JSON payload string to ByteString.
      ByteString byteStringPayload = ByteString.copyFromUtf8(jsonPayload);

      // Create the parameter version payload.
      ParameterVersionPayload parameterVersionPayload =
          ParameterVersionPayload.newBuilder().setData(byteStringPayload).build();

      // Create the parameter version with the JSON payload.
      ParameterVersion parameterVersion =
          ParameterVersion.newBuilder().setPayload(parameterVersionPayload).build();

      // Create the parameter version in the Parameter Manager.
      ParameterVersion createdParameterVersion =
          client.createParameterVersion(parameterName.toString(), parameterVersion, versionId);
      System.out.printf("Created parameter version %s\n", createdParameterVersion.getName());

      // Step 3: Render the parameter version to fetch and print both simple and rendered payloads.
      // Build the parameter version name.
      ParameterVersionName parameterVersionName =
          ParameterVersionName.of(projectId, locationId, parameterId, versionId);

      // Render the parameter version.
      ParameterVersion response = client.getParameterVersion(parameterVersionName.toString());
      System.out.printf(
          "Parameter version %s with rendered data: %s\n",
          response.getName(), response.getPayload().getData().toStringUtf8());
    }
  }
}
// [END parametermanager_quickstart]
