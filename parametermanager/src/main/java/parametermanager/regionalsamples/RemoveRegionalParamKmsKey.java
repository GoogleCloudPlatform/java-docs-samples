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

// [START parametermanager_remove_regional_param_kms_key]

import com.google.cloud.parametermanager.v1.Parameter;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerSettings;
import com.google.cloud.parametermanager.v1.ParameterName;
import com.google.protobuf.FieldMask;
import com.google.protobuf.util.FieldMaskUtil;
import java.io.IOException;

/**
 * This class demonstrates how to change the kms key of a parameter 
 * using the Parameter Manager SDK for GCP.
 */
public class RemoveRegionalParamKmsKey {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String parameterId = "your-parameter-id";

    // Call the method to remove kms key of a parameter.
    removeRegionalParamKmsKey(projectId, locationId, parameterId);
  }

  // This is an example snippet for updating the kms key of a parameter.
  public static Parameter removeRegionalParamKmsKey(
      String projectId, String locationId, String parameterId) throws IOException {

    // Endpoint to call the regional parameter manager server
    String apiEndpoint = String.format("parametermanager.%s.rep.googleapis.com:443", locationId);
    ParameterManagerSettings parameterManagerSettings =
        ParameterManagerSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create(parameterManagerSettings)) {

      // Build the parameter name.
      ParameterName name = ParameterName.of(projectId, locationId, parameterId);

      // Remove kms key of a parameter  .
      Parameter parameter = Parameter.newBuilder()
          .setName(name.toString())
          .clearKmsKey()
          .build();

      // Build the field mask for the kms_key field.
      FieldMask fieldMask = FieldMaskUtil.fromString("kms_key");

      // Update the parameter kms key.
      Parameter updatedParameter = client.updateParameter(parameter, fieldMask);
      System.out.printf(
          "Removed kms key for regional parameter %s\n",
          updatedParameter.getName());

      return updatedParameter;
    }
  }
}
// [END parametermanager_remove_regional_param_kms_key]
