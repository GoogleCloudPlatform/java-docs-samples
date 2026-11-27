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

// [START parametermanager_list_param_templates]

import com.google.cloud.parametermanager.v1.LocationName;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerClient.ListTemplatesPagedResponse;
import java.io.IOException;

/** This class demonstrates how to list templates using the Parameter Manager SDK for GCP. */
public class ListParamTemplates {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";

    // Call the method to list templates.
    listParamTemplates(projectId);
  }

  // This is an example snippet for listing all templates in the given project.
  public static ListTemplatesPagedResponse listParamTemplates(String projectId) throws IOException {

    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      String locationId = "global";

      // Build the parent name from the project.
      LocationName location = LocationName.of(projectId, locationId);

      // List all templates.
      ListTemplatesPagedResponse response = client.listTemplates(location.toString());
      response
          .iterateAll()
          .forEach(
              template ->
                  System.out.printf(
                      "Found template %s with format %s\n",
                      template.getName(), template.getFormat()));

      return response;
    }
  }
}
// [END parametermanager_list_param_templates]
