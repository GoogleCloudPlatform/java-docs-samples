/*
 * Copyright 2023 Google LLC
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

package com.google.cloud.storage.storageinsights.samples;

// [START storageinsights_delete_inventory_report_config]

import com.google.cloud.storageinsights.v1.ReportConfigName;
import com.google.cloud.storageinsights.v1.StorageInsightsClient;
import java.io.IOException;

public class DeleteInventoryReportConfig {

  // [END storageinsights_delete_inventory_report_config]
  public static void main(String[] args) throws IOException {
    // The ID of your Google Cloud Project
    String projectId = "your-project-id";

    // The location your bucket is in
    String bucketLocation = "us-west-1";

    // The UUID of the inventory report you want to delete
    String inventoryReportConfigUuid = "2b90d21c-f2f4-40b5-9519-e29a78f2b09f";

    deleteInventoryReportConfig(projectId, bucketLocation, inventoryReportConfigUuid);
  }
  // [START storageinsights_delete_inventory_report_config]

  public static void deleteInventoryReportConfig(
      String projectId, String location, String inventoryReportConfigUuid) throws IOException {
    try (StorageInsightsClient storageInsightsClient = StorageInsightsClient.create()) {
      ReportConfigName name = ReportConfigName.of(projectId, location, inventoryReportConfigUuid);
      storageInsightsClient.deleteReportConfig(name);

      System.out.println("Deleted inventory report config with name " + name);
    }
  }
}
// [END storageinsights_delete_inventory_report_config]
