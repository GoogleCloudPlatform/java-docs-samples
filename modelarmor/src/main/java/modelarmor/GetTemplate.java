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

package modelarmor;

/**
 * This class demonstrates how to retrieve a template using the Model Armor API.
 *
 * @author [Your Name]
 */
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.ModelArmorSettings;
import com.google.cloud.modelarmor.v1.Template;
import com.google.cloud.modelarmor.v1.TemplateName;
import com.google.protobuf.util.JsonFormat;

// [START modelarmor_get_template]

/** This class contains a main method that retrieves a template using the Model Armor API. */
public class GetTemplate {

  /**
   * Main method that calls the getTemplate method to retrieve a template.
   *
   * @param args command line arguments (not used)
   * @throws Exception if an error occurs during template retrieval
   */
  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String templateId = "your-template-id";

    getTemplate(projectId, locationId, templateId);
  }

  /**
   * Retrieves a template using the Model Armor API.
   *
   * @param projectId the ID of the project
   * @param locationId the ID of the location
   * @param templateId the ID of the template
   * @throws Exception if an error occurs during template retrieval
   */
  public static void getTemplate(String projectId, String locationId, String templateId)
      throws Exception {
    // Construct the API endpoint URL
    String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);

    // Create a Model Armor settings object with the API endpoint
    ModelArmorSettings modelArmorSettings =
        ModelArmorSettings.newBuilder().setEndpoint(apiEndpoint).build();

    try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {
      // Construct the template name
      String name = TemplateName.of(projectId, locationId, templateId).toString();

      // Retrieve the template using the Model Armor client
      Template template = client.getTemplate(name);

      // Print the retrieved template
      System.out.println("Retrieved template: " + JsonFormat.printer().print(template));
    }
  }
}

// [END modelarmor_get_template]
