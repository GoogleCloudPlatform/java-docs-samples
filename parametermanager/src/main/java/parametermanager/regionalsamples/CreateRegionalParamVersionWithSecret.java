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

// [START parametermanager_create_regional_param_version_with_secret]

import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerSettings;
import com.google.cloud.parametermanager.v1.ParameterName;
import com.google.cloud.parametermanager.v1.ParameterVersion;
import com.google.cloud.parametermanager.v1.ParameterVersionPayload;
import com.google.protobuf.ByteString;
import java.io.IOException;

/**
 * This class demonstrates how to create a regional parameter version with a JSON payload that
 * includes a secret reference using the Parameter Manager SDK for GCP.
 */
public class CreateRegionalParamVersionWithSecret {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String parameterId = "your-parameter-id";
    String versionId = "your-version-id";
    String secretId =
        "projects/your-project-id/locations/your-location-id"
            + "/secrets/your-secret-id/versions/latest";

    // Call the method to create a regional parameter version with JSON payload that includes a
    // secret reference.
    createRegionalParamVersionWithSecret(projectId, locationId, parameterId, versionId, secretId);
  }

  // This is an example snippet that creates a regional parameter version with a JSON payload that
  // includes a secret reference.
  public static ParameterVersion createRegionalParamVersionWithSecret(
      String projectId, String locationId, String parameterId, String versionId, String secretId)
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

      // Convert the JSON payload string to ByteString.
      String payload =
          String.format(
              "{\"username\": \"test-user\","
                  + "\"password\": \"__REF__(//secretmanager.googleapis.com/%s)\"}",
              secretId);
      ByteString byteStringPayload = ByteString.copyFromUtf8(payload);

      // Create the parameter version payload with the secret reference.
      ParameterVersionPayload parameterVersionPayload =
          ParameterVersionPayload.newBuilder().setData(byteStringPayload).build();

      // Create the parameter version with the JSON payload.
      ParameterVersion parameterVersion =
          ParameterVersion.newBuilder().setPayload(parameterVersionPayload).build();

      // Create the parameter version in the Parameter Manager.
      ParameterVersion createdParameterVersion =
          client.createParameterVersion(parameterName.toString(), parameterVersion, versionId);
      System.out.printf(
          "Created regional parameter version: %s\n", createdParameterVersion.getName());

      return createdParameterVersion;
    }
  }
}
// [END parametermanager_create_regional_param_version_with_secret]
