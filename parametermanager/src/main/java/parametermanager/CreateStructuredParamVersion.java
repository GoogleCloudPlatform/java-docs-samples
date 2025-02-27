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

// [START parametermanager_create_structured_param_version]

import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterName;
import com.google.cloud.parametermanager.v1.ParameterVersion;
import com.google.cloud.parametermanager.v1.ParameterVersionPayload;
import com.google.protobuf.ByteString;
import java.io.IOException;

/**
 * This class demonstrates how to create a parameter version with a JSON payload using the Parameter
 * Manager SDK for GCP.
 */
public class CreateStructuredParamVersion {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String parameterId = "your-parameter-id";
    String versionId = "your-version-id";
    String jsonPayload = "{\"username\": \"test-user\", \"host\": \"localhost\"}";

    // Call the method to create a parameter version with JSON payload.
    createStructuredParamVersion(projectId, parameterId, versionId, jsonPayload);
  }

  // This is an example snippet for creating a new parameter version with the given JSON payload.
  public static ParameterVersion createStructuredParamVersion(
      String projectId, String parameterId, String versionId, String jsonPayload)
      throws IOException {
    // Initialize the client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      String locationId = "global";

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
      System.out.printf("Created parameter version: %s\n", createdParameterVersion.getName());

      return createdParameterVersion;
    }
  }
}
// [END parametermanager_create_structured_param_version]
