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

// [START storageinsights_edit_inventory_report_config]

import com.google.cloud.storageinsights.v1.ReportConfig;
import com.google.cloud.storageinsights.v1.ReportConfigName;
import com.google.cloud.storageinsights.v1.StorageInsightsClient;
import com.google.cloud.storageinsights.v1.UpdateReportConfigRequest;
import java.io.IOException;

public class EditInventoryReportConfig {

  // [END storageinsights_edit_inventory_report_config]
  public static void main(String[] args) throws IOException {
    // The ID of your Google Cloud Project
    String projectId = "your-project-id";

    // The location your bucket is in
    String bucketLocation = "us-west-1";

    // The UUID of the inventory report you want to edit
    String inventoryReportConfigUuid = "2b90d21c-f2f4-40b5-9519-e29a78f2b09f";

    editInventoryReportConfig(projectId, bucketLocation, inventoryReportConfigUuid);
  }
  // [START storageinsights_edit_inventory_report_config]

  public static void editInventoryReportConfig(
      String projectId, String location, String inventoryReportConfigUuid) throws IOException {
    try (StorageInsightsClient storageInsightsClient = StorageInsightsClient.create()) {
      ReportConfigName name = ReportConfigName.of(projectId, location, inventoryReportConfigUuid);
      ReportConfig reportConfig = storageInsightsClient.getReportConfig(name);

      // Set any other fields you want to update here
      ReportConfig updatedReportConfig =
          reportConfig.toBuilder().setDisplayName("Updated Display Name").build();

      storageInsightsClient.updateReportConfig(
          UpdateReportConfigRequest.newBuilder().setReportConfig(updatedReportConfig).build());

      System.out.println("Edited inventory report config with name " + name);
    }
  }
}

// [END storageinsights_edit_inventory_report_config]
