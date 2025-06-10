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

// [START modelarmor_update_organization_floor_settings]

import com.google.cloud.modelarmor.v1.DetectionConfidenceLevel;
import com.google.cloud.modelarmor.v1.FilterConfig;
import com.google.cloud.modelarmor.v1.FloorSetting;
import com.google.cloud.modelarmor.v1.FloorSettingName;
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.RaiFilterSettings;
import com.google.cloud.modelarmor.v1.RaiFilterSettings.RaiFilter;
import com.google.cloud.modelarmor.v1.RaiFilterType;
import com.google.cloud.modelarmor.v1.UpdateFloorSettingRequest;
import com.google.protobuf.FieldMask;
import java.io.IOException;
import java.util.List;

public class UpdateOrganizationsFloorSetting {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String organizationId = "your-organization-id";

    updateOrganizationFloorSetting(organizationId);
  }

  public static FloorSetting updateOrganizationFloorSetting(String organizationId)
      throws IOException {

    // Initialize client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests.
    try (ModelArmorClient client = ModelArmorClient.create()) {
      String name = FloorSettingName.ofOrganizationLocationName(organizationId, "global")
          .toString();

      // For more details on filters, please refer to the following doc:
      // https://cloud.google.com/security-command-center/docs/key-concepts-model-armor#ma-filters
      RaiFilterSettings raiFilterSettings =
          RaiFilterSettings.newBuilder()
              .addAllRaiFilters(List.of())
              .build();

      FilterConfig modelArmorFilter = FilterConfig.newBuilder()
          .setRaiSettings(raiFilterSettings)
          .build();

      FloorSetting floorSetting = FloorSetting.newBuilder()
          .setName(name)
          .setFilterConfig(modelArmorFilter)
          .setEnableFloorSettingEnforcement(true)
          .build();

      UpdateFloorSettingRequest request = UpdateFloorSettingRequest.newBuilder()
          .setFloorSetting(floorSetting)
          .build();

      FloorSetting updatedFloorSetting = client.updateFloorSetting(request);
      System.out.println("Updated floor setting for organization: " + organizationId);

      return updatedFloorSetting;
    }
  }
}
// [END modelarmor_update_organization_floor_settings]
