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

// [START parametermanager_create_param_with_kms_key]

import com.google.cloud.parametermanager.v1.LocationName;
import com.google.cloud.parametermanager.v1.Parameter;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import java.io.IOException;

/**
 * Example class to create a new parameter with provided KMS key
 * using the Parameter Manager SDK for GCP.
 */
public class CreateParamWithKmsKey {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String parameterId = "your-parameter-id";
    String kmsKeyName = "your-kms-key";

    // Call the method to create a parameter with the specified kms key.
    createParameterWithKmsKey(projectId, parameterId, kmsKeyName);
  }

  // This is an example snippet for creating a new parameter with a specific format.
  public static Parameter createParameterWithKmsKey(
      String projectId, String parameterId, String kmsKeyName) throws IOException {
    // Initialize the client that will be used to send requests.
    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      String locationId = "global";

      // Build the parent name from the project.
      LocationName location = LocationName.of(projectId, locationId);

      // Build the parameter to create with the provided format.
      Parameter parameter = Parameter.newBuilder().setKmsKey(kmsKeyName).build();

      // Create the parameter.
      Parameter createdParameter =
          client.createParameter(location.toString(), parameter, parameterId);
      System.out.printf(
          "Created parameter %s with kms key %s\n",
          createdParameter.getName(), createdParameter.getKmsKey());

      return createdParameter;
    }
  }
}
// [END parametermanager_create_param_with_kms_key]
