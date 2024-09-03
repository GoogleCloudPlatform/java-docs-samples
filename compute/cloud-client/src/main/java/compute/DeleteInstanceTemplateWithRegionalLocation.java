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

import com.google.cloud.compute.v1.DeleteRegionInstanceTemplateRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.RegionInstanceTemplatesClient;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteInstanceTemplateWithRegionalLocation {
  public static void main(String[] args)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // TODO(developer): Replace these variables before running the sample.
    // Project ID or project number of the Cloud project you want to use.
    String projectId = "YOUR_PROJECT_ID";
    // Name of the instance you want to delete.
    String instanceName = "YOUR_INSTANCE_NAME";
    // Name of the zone.
    String zone = "us-central1-a";

    deleteInstanceTemplate(projectId, zone, instanceName);
  }

  // Delete an instance template with the REGIONAL location.
  public static void deleteInstanceTemplate(
      String projectId, String zone, String templateName)
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (RegionInstanceTemplatesClient regionInstanceTemplatesClient =
             RegionInstanceTemplatesClient.create()) {
      String region = zone.substring(0, zone.lastIndexOf('-')); // Extract the region from the zone

      DeleteRegionInstanceTemplateRequest deleteInstanceTemplateRequest =
          DeleteRegionInstanceTemplateRequest
              .newBuilder()
              .setProject(projectId)
              .setRegion(region)
              .setInstanceTemplate(templateName)
              .build();

      Operation response = regionInstanceTemplatesClient.deleteAsync(
          deleteInstanceTemplateRequest).get(3, TimeUnit.MINUTES);

      if (response.hasError()) {
        System.out.println("Instance template deletion failed ! ! " + response);
        return;
      }
      System.out.printf("Instance template deletion operation status for %s: %s ", templateName,
          response.getStatus());
    }
  }
}
