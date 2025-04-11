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

import com.google.cloud.modelarmor.v1.ListTemplatesRequest;
import com.google.cloud.modelarmor.v1.LocationName;
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.ModelArmorSettings;
import com.google.cloud.modelarmor.v1.Template;
import com.google.protobuf.util.JsonFormat;

public class ListTemplatesWithFilter {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";
    String locationId = "your-location-id";
    String templateId = "your-template-id";

    listTemplatesWithFilter(projectId, locationId, templateId);
  }

  public static void listTemplatesWithFilter(String projectId, String locationId, String templateId)
      throws Exception {
    String apiEndpoint = String.format("modelarmor.%s.rep.googleapis.com:443", locationId);
    ModelArmorSettings modelArmorSettings =
        ModelArmorSettings.newBuilder().setEndpoint(apiEndpoint).build();

    try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {
      String parent = LocationName.of(projectId, locationId).toString();
      String filter = String.format("name=\"%s/templates/%s\"", parent, templateId);
      ListTemplatesRequest request =
          ListTemplatesRequest.newBuilder().setParent(parent).setFilter(filter).build();
      for (Template template : client.listTemplates(request).iterateAll()) {
        System.out.println("Template with filter: " + JsonFormat.printer().print(template));
      }
    }
  }
}
