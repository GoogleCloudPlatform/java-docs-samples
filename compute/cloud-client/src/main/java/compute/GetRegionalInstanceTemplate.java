/*
 * Copyright 2024 Google LLC
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

package compute;

// [START compute_regional_template_get]

import com.google.cloud.compute.v1.InstanceTemplate;
import com.google.cloud.compute.v1.RegionInstanceTemplatesClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class GetRegionalInstanceTemplate {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the instance you want to get.
    String instanceName = "YOUR_INSTANCE_NAME";
    // Name of the region.
    String region = "us-central1";

    getRegionalInstanceTemplate(projectId, region, instanceName);
  }

  // Get a regional instance template.
  public static InstanceTemplate getRegionalInstanceTemplate(
      String project, String region, String instanceName) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (RegionInstanceTemplatesClient instancesClient = RegionInstanceTemplatesClient.create()) {
      return instancesClient.get(project, region, instanceName);
    }
  }
}
// [END compute_regional_template_get]
