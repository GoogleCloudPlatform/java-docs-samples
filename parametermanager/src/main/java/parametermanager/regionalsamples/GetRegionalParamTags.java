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

// [START parametermanager_get_regional_param_tags]

import com.google.cloud.parametermanager.v1.Parameter;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerSettings;
import com.google.cloud.parametermanager.v1.ParameterName;
import com.google.cloud.resourcemanager.v3.TagBindingsClient;
import com.google.cloud.resourcemanager.v3.TagBindingsClient.ListTagBindingsPagedResponse;
import com.google.cloud.resourcemanager.v3.TagBindingsSettings;
import java.io.IOException;

/**
 * This class demonstrates how to get the tags of a regional parameter using the Parameter Manager
 * SDK for GCP.
 */
public class GetRegionalParamTags {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String parameterId = "your-parameter-id";

    // Call the method to get the tags of a parameter.
    getRegionalParamTags(projectId, locationId, parameterId);
  }

  // This is an example snippet for getting the tag bindings of a regional parameter.
  public static ListTagBindingsPagedResponse getRegionalParamTags(
      String projectId, String locationId, String parameterId) throws IOException {
    // Endpoint to call the regional parameter manager server.
    String apiEndpoint = String.format("parametermanager.%s.rep.googleapis.com:443", locationId);
    ParameterManagerSettings parameterManagerSettings =
        ParameterManagerSettings.newBuilder().setEndpoint(apiEndpoint).build();

    // Endpoint to call the regional resource manager server.
    String tagEndpoint = String.format("%s-cloudresourcemanager.googleapis.com:443", locationId);
    TagBindingsSettings tagBindingsSettings =
        TagBindingsSettings.newBuilder().setEndpoint(tagEndpoint).build();
    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create(parameterManagerSettings);
        TagBindingsClient tagBindingsClient = TagBindingsClient.create(tagBindingsSettings)) {
      // Build the parameter name.
      ParameterName parameterName = ParameterName.of(projectId, locationId, parameterId);

      // Get the parameter.
      Parameter parameter = client.getParameter(parameterName.toString());

      // List the tag bindings of the parameter.
      ListTagBindingsPagedResponse response =
          tagBindingsClient.listTagBindings(
              "//parametermanager.googleapis.com/" + parameter.getName());
      response
          .iterateAll()
          .forEach(
              binding ->
                  System.out.printf("Found regional parameter tag: %s\n", binding.getTagValue()));

      return response;
    }
  }
}
// [END parametermanager_get_regional_param_tags]
