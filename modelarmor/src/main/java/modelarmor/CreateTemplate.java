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

import com.google.cloud.modelarmor.v1.*;
import com.google.cloud.modelarmor.v1.RaiFilterSettings.RaiFilter;
import com.google.protobuf.util.JsonFormat;

import java.util.List;

public class CreateTemplate {

    public static void main(String[] args) throws Exception {
        // TODO(developer): Replace these variables before running the sample.
        String projectId = "your-project-id";
        String locationId = "your-location-id";
        String templateId = "your-template-id";

        createTemplate(projectId, locationId, templateId);
    }

    public static Template createTemplate(String projectId, String locationId, String templateId)
        throws Exception {
        String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);
        ModelArmorSettings modelArmorSettings =
            ModelArmorSettings.newBuilder().setEndpoint(apiEndpoint).build();

        try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {
            String parent = LocationName.of(projectId, locationId).toString();

            Template template =
                Template.newBuilder()
                    .setFilterConfig(
                        FilterConfig.newBuilder()
                            .setRaiSettings(
                                RaiFilterSettings.newBuilder()
                                    .addAllRaiFilters(
                                        List.of(
                                            RaiFilter.newBuilder()
                                                .setFilterType(RaiFilterType.DANGEROUS)
                                                .setConfidenceLevel(DetectionConfidenceLevel.HIGH)
                                                .build()))
                                    .build())
                            .build())
                    .build();

            CreateTemplateRequest request =
                CreateTemplateRequest.newBuilder()
                    .setParent(parent)
                    .setTemplateId(templateId)
                    .setTemplate(template)
                    .build();

            Template createdTemplate = client.createTemplate(request);
            System.out.println("Created template: " + JsonFormat.printer().print(createdTemplate));
            return createdTemplate;
        }
    }
}
