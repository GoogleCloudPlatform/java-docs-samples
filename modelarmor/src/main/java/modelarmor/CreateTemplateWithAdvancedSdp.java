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

// [START modelarmor_create_template_with_advanced_sdp]

import com.google.cloud.modelarmor.v1.CreateTemplateRequest;
import com.google.cloud.modelarmor.v1.FilterConfig;
import com.google.cloud.modelarmor.v1.LocationName;
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.ModelArmorSettings;
import com.google.cloud.modelarmor.v1.SdpAdvancedConfig;
import com.google.cloud.modelarmor.v1.SdpFilterSettings;
import com.google.cloud.modelarmor.v1.Template;
import com.google.privacy.dlp.v2.DeidentifyTemplateName;
import com.google.privacy.dlp.v2.InspectTemplateName;
import java.io.IOException;

public class CreateTemplateWithAdvancedSdp {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.

    // Specify the Google Project ID.
    String projectId = "your-project-id";
    // Specify the location ID. For example, us-central1.
    String locationId = "your-location-id";
    // Specify the template ID.
    String templateId = "your-template-id";
    // Specify the Inspect template ID.
    String inspectTemplateId = "your-inspect-template-id";
    // Specify the Deidentify template ID.
    String deidentifyTemplateId = "your-deidentify-template-id";

    createTemplateWithAdvancedSdp(projectId, locationId, templateId, inspectTemplateId,
        deidentifyTemplateId);
  }

  public static Template createTemplateWithAdvancedSdp(String projectId, String locationId,
      String templateId, String inspectTemplateId, String deidentifyTemplateId) throws IOException {

    // Construct the API endpoint URL.
    String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);
    ModelArmorSettings modelArmorSettings = ModelArmorSettings.newBuilder().setEndpoint(apiEndpoint)
        .build();

    // Initialize the client that will be used to send requests. This client
    // only needs to be created once, and can be reused for multiple requests.
    try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {
      String parent = LocationName.of(projectId, locationId).toString();

      String inspectTemplateName = InspectTemplateName
          .ofProjectLocationInspectTemplateName(projectId, locationId, inspectTemplateId)
          .toString();

      String deidentifyTemplateName = DeidentifyTemplateName
          .ofProjectLocationDeidentifyTemplateName(projectId, locationId, deidentifyTemplateId)
          .toString();

      // Build the Model Armor template with Advanced SDP Filter.

      // Note: If you specify only Inspect template, Model Armor reports the filter matches if
      // sensitive data is detected. If you specify Inspect template and De-identify template, Model
      // Armor returns the de-identified sensitive data and sanitized version of prompts or
      // responses in the deidentifyResult.data.text field of the finding.
      SdpAdvancedConfig advancedSdpConfig =
          SdpAdvancedConfig.newBuilder()
              .setInspectTemplate(inspectTemplateName)
              .setDeidentifyTemplate(deidentifyTemplateName)
              .build();

      SdpFilterSettings sdpSettings = SdpFilterSettings.newBuilder()
          .setAdvancedConfig(advancedSdpConfig).build();

      FilterConfig modelArmorFilter = FilterConfig.newBuilder().setSdpSettings(sdpSettings).build();

      Template template = Template.newBuilder().setFilterConfig(modelArmorFilter).build();

      CreateTemplateRequest request = CreateTemplateRequest.newBuilder()
          .setParent(parent)
          .setTemplateId(templateId)
          .setTemplate(template)
          .build();

      Template createdTemplate = client.createTemplate(request);
      System.out.println("Created template with Advanced SDP filter: " + createdTemplate.getName());

      return createdTemplate;
    }
  }
}
// [END modelarmor_create_template_with_advanced_sdp]
