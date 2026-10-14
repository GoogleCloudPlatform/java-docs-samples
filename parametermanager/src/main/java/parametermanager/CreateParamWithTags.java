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

// [START parametermanager_create_param_with_tags]

import com.google.cloud.parametermanager.v1.LocationName;
import com.google.cloud.parametermanager.v1.Parameter;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import java.io.IOException;

/**
 * This class demonstrates how to create a parameter with a tag using the Parameter Manager SDK for
 * GCP.
 */
public class CreateParamWithTags {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String parameterId = "your-parameter-id";
    String tagKey = "tagKeys/1234567890";
    String tagValue = "tagValues/1234567890";

    // Call the method to create a parameter with a tag.
    createParamWithTags(projectId, parameterId, tagKey, tagValue);
  }

  // This is an example snippet for creating a parameter with a tag.
  public static Parameter createParamWithTags(
      String projectId, String parameterId, String tagKey, String tagValue) throws IOException {

    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      String locationId = "global";

      // Build the parent name from the project.
      LocationName location = LocationName.of(projectId, locationId);

      // Build the parameter to create with the tag.
      Parameter parameter = Parameter.newBuilder().putTags(tagKey, tagValue).build();

      // Create the parameter.
      Parameter createdParameter =
          client.createParameter(location.toString(), parameter, parameterId);
      System.out.printf(
          "Created parameter %s with tag %s: %s\n", createdParameter.getName(), tagKey, tagValue);

      return createdParameter;
    }
  }
}
// [END parametermanager_create_param_with_tags]
