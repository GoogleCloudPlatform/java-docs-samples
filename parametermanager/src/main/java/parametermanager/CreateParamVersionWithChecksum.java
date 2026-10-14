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

package parametermanager;

// [START parametermanager_create_param_version_with_checksum]

import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterName;
import com.google.cloud.parametermanager.v1.ParameterVersion;
import com.google.cloud.parametermanager.v1.ParameterVersionPayload;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32C;

/**
 * This class demonstrates how to create a parameter version with a checksum using the Parameter
 * Manager SDK for GCP.
 */
public class CreateParamVersionWithChecksum {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String parameterId = "your-parameter-id";
    String versionId = "your-version-id";

    // Call the method to create a parameter version with a checksum.
    createParamVersionWithChecksum(projectId, parameterId, versionId);
  }

  // This is an example snippet for creating a parameter version with a CRC32C checksum of the
  // payload.
  public static ParameterVersion createParamVersionWithChecksum(
      String projectId, String parameterId, String versionId) throws IOException {

    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      String locationId = "global";

      // Build the parameter name.
      ParameterName parameterName = ParameterName.of(projectId, locationId, parameterId);

      // Define the payload and compute its CRC32C checksum.
      byte[] data = "hello world!".getBytes(StandardCharsets.UTF_8);
      CRC32C crc32c = new CRC32C();
      crc32c.update(data);

      // Build the parameter version with the payload and checksum.
      ParameterVersionPayload payload =
          ParameterVersionPayload.newBuilder()
              .setData(ByteString.copyFrom(data))
              .setDataCrc32C(crc32c.getValue())
              .build();
      ParameterVersion parameterVersion = ParameterVersion.newBuilder().setPayload(payload).build();

      // Create the parameter version.
      ParameterVersion createdParameterVersion =
          client.createParameterVersion(parameterName.toString(), parameterVersion, versionId);
      System.out.printf("Created parameter version: %s\n", createdParameterVersion.getName());
      System.out.printf("Checksum source: %s\n", createdParameterVersion.getChecksumSource());

      return createdParameterVersion;
    }
  }
}
// [END parametermanager_create_param_version_with_checksum]
