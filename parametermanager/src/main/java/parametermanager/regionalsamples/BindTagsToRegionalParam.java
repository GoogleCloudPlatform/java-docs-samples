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

// [START parametermanager_bind_tags_to_regional_param]

import com.google.cloud.parametermanager.v1.LocationName;
import com.google.cloud.parametermanager.v1.Parameter;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerSettings;
import com.google.cloud.resourcemanager.v3.TagBinding;
import com.google.cloud.resourcemanager.v3.TagBindingsClient;
import com.google.cloud.resourcemanager.v3.TagBindingsSettings;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * This class demonstrates how to create a regional parameter and bind a tag to it using the
 * Parameter Manager SDK for GCP.
 */
public class BindTagsToRegionalParam {

  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String parameterId = "your-parameter-id";
    String tagValue = "tagValues/1234567890";

    // Call the method to create a parameter and bind a tag to it.
    bindTagsToRegionalParam(projectId, locationId, parameterId, tagValue);
  }

  // This is an example snippet for creating a regional parameter and binding a tag to it.
  public static TagBinding bindTagsToRegionalParam(
      String projectId, String locationId, String parameterId, String tagValue)
      throws IOException, ExecutionException, InterruptedException {
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
      // Build the parent name from the project.
      LocationName location = LocationName.of(projectId, locationId);

      // Create the parameter.
      Parameter parameter =
          client.createParameter(location.toString(), Parameter.newBuilder().build(), parameterId);
      System.out.printf("Created regional parameter: %s\n", parameter.getName());

      // Build the tag binding for the new parameter.
      TagBinding tagBinding =
          TagBinding.newBuilder()
              .setParent("//parametermanager.googleapis.com/" + parameter.getName())
              .setTagValue(tagValue)
              .build();

      // Create the tag binding and wait for the operation to complete.
      TagBinding createdTagBinding = tagBindingsClient.createTagBindingAsync(tagBinding).get();
      System.out.printf("Created tag binding: %s\n", createdTagBinding.getName());

      return createdTagBinding;
    }
  }
}
// [END parametermanager_bind_tags_to_regional_param]
