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

// [START parametermanager_get_param_template]

import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.Template;
import com.google.cloud.parametermanager.v1.TemplateName;
import java.io.IOException;

/** This class demonstrates how to get a template using the Parameter Manager SDK for GCP. */
public class GetParamTemplate {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String templateId = "your-template-id";

    // Call the method to get a template.
    getParamTemplate(projectId, templateId);
  }

  // This is an example snippet for getting a template.
  public static Template getParamTemplate(String projectId, String templateId) throws IOException {

    // Initialize the client that will be used to send requests. This client only needs to be
    // created once, and can be reused for multiple requests.
    try (ParameterManagerClient client = ParameterManagerClient.create()) {
      String locationId = "global";

      // Build the template name.
      TemplateName templateName = TemplateName.of(projectId, locationId, templateId);

      // Get the template.
      Template template = client.getTemplate(templateName.toString());
      System.out.printf(
          "Got template %s with format %s\n", template.getName(), template.getFormat());

      return template;
    }
  }
}
// [END parametermanager_get_param_template]
