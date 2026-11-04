/*
 * Copyright 2026 Google LLC
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

// [START parametermanager_get_regional_param_version_verify_checksum]

import com.google.cloud.parametermanager.v1.GetParameterVersionRequest;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerSettings;
import com.google.cloud.parametermanager.v1.ParameterVersion;
import com.google.cloud.parametermanager.v1.ParameterVersionName;
import com.google.cloud.parametermanager.v1.View;
import java.io.IOException;
import java.util.zip.CRC32C;

/**
 * This class demonstrates how to get a regional parameter version and verify its checksum using the
 * Parameter Manager SDK for GCP.
 */
public class GetRegionalParamVersionVerifyChecksum {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String parameterId = "your-parameter-id";
    String versionId = "your-version-id";

    // Call the method to get a parameter version and verify its checksum.
    getRegionalParamVersionVerifyChecksum(projectId, locationId, parameterId, versionId);
  }

  // This is an example snippet for getting a regional parameter version and verifying its payload
  // checksum.
  public static ParameterVersion getRegionalParamVersionVerifyChecksum(
      String projectId, String locationId, String parameterId, String versionId)
      throws IOException {
    // Endpoint to call the regional parameter manager server.
    String apiEndpoint = String.format("parametermanager.%s.rep.googleapis.com:443", locationId);
    ParameterManagerSettings parameterManagerSettings =
        ParameterManagerSettings.newBuilder().setEndpoint(apiEndpoint).build();
    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create(parameterManagerSettings)) {
      // Build the parameter version name.
      ParameterVersionName parameterVersionName =
          ParameterVersionName.of(projectId, locationId, parameterId, versionId);

      // Get the parameter version with the payload and checksum.
      GetParameterVersionRequest request =
          GetParameterVersionRequest.newBuilder()
              .setName(parameterVersionName.toString())
              .setView(View.FULL)
              .build();
      ParameterVersion parameterVersion = client.getParameterVersion(request);

      // Verify the payload checksum.
      CRC32C crc32c = new CRC32C();
      crc32c.update(parameterVersion.getPayload().getData().toByteArray());
      if (crc32c.getValue() != parameterVersion.getPayload().getDataCrc32C()) {
        throw new IllegalStateException(
            String.format("Checksum mismatch for %s: data corrupted", parameterVersion.getName()));
      }
      System.out.printf(
          "Verified checksum for regional parameter version: %s\n", parameterVersion.getName());
      System.out.printf("Checksum source: %s\n", parameterVersion.getChecksumSource());

      return parameterVersion;
    }
  }
}
// [END parametermanager_get_regional_param_version_verify_checksum]
