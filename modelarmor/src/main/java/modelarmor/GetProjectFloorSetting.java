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

// [START modelarmor_get_project_floor_settings]

import com.google.cloud.modelarmor.v1.FloorSetting;
import com.google.cloud.modelarmor.v1.FloorSettingName;
import com.google.cloud.modelarmor.v1.GetFloorSettingRequest;
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import java.io.IOException;

public class GetProjectFloorSetting {

  public static void main(String[] args) throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "your-project-id";

    getProjectFloorSetting(projectId);
  }

  public static FloorSetting getProjectFloorSetting(String projectId) throws IOException {

    // Initialize client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests.
    try (ModelArmorClient client = ModelArmorClient.create()) {
      String name = FloorSettingName.of(projectId, "global").toString();

      GetFloorSettingRequest request = GetFloorSettingRequest.newBuilder().setName(name).build();

      FloorSetting floorSetting = client.getFloorSetting(request);
      System.out.println("Fetched floor setting for project: " + projectId);

      return floorSetting;
    }
  }
}
// [END modelarmor_get_project_floor_settings]
