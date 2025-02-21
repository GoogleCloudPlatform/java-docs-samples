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

// [START parametermanager_get_param]

import com.google.cloud.parametermanager.v1.Parameter;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterName;
import java.io.IOException;

/** This class demonstrates how to get a parameter using the Parameter Manager SDK for GCP. */
public class GetParam {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String parameterId = "your-parameter-id";

    // Call the method to get a parameter.
    getParam(projectId, parameterId);
  }

  // This is an example snippet for getting a parameter.
  public static Parameter getParam(String projectId, String parameterId) throws IOException {
    // Initialize the client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      String locationId = "global";

      // Build the parameter name.
      ParameterName parameterName = ParameterName.of(projectId, locationId, parameterId);

      // Get the parameter.
      Parameter parameter = client.getParameter(parameterName.toString());
      System.out.printf(
          "Retrieved parameter %s with labels: %s\n",
          parameter.getName(), parameter.getLabelsMap());

      return parameter;
    }
  }
}
// [END parametermanager_get_param]
