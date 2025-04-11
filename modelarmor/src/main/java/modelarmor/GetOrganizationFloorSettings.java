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

import com.google.cloud.modelarmor.v1.FloorSetting;
import com.google.cloud.modelarmor.v1.FloorSettingName;
import com.google.cloud.modelarmor.v1.GetFloorSettingRequest;
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.protobuf.util.JsonFormat;

public class GetOrganizationFloorSettings {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String organizationId = "your-organization-id";

    getOrganizationFloorSettings(organizationId);
  }

  public static void getOrganizationFloorSettings(String organizationId) throws Exception {

    try (ModelArmorClient client = ModelArmorClient.create()) {
      String name =
          FloorSettingName.ofOrganizationLocationName(organizationId, "global").toString();
      GetFloorSettingRequest request = GetFloorSettingRequest.newBuilder().setName(name).build();
      FloorSetting response = client.getFloorSetting(request);
      System.out.println("Organization Floor Settings: " + JsonFormat.printer().print(response));
    }
  }
}
