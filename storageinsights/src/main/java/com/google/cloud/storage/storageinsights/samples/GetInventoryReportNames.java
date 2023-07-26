/*
 * Copyright 2023 Google Inc.
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

// [START storageinsights_get_inventory_report_names]

package com.google.cloud.storage.storageinsights.samples;

import com.google.cloud.storageinsights.v1.ReportConfig;
import com.google.cloud.storageinsights.v1.ReportConfigName;
import com.google.cloud.storageinsights.v1.ReportDetail;
import com.google.cloud.storageinsights.v1.StorageInsightsClient;
import java.io.IOException;

public class GetInventoryReportNames {

  public static void main(String[] args) throws IOException {
    // The ID of your Google Cloud Project
    String projectId = "your-project-id";

    // The location your bucket is in
    String bucketLocation = "us-west-1";

    // The UUID of the inventory report you want to get file names for
    String inventoryReportConfigUUID = "2b90d21c-f2f4-40b5-9519-e29a78f2b09f";

    getInventoryReportNames(projectId, bucketLocation, inventoryReportConfigUUID);
  }

  public static void getInventoryReportNames(
      String projectID, String location, String reportConfigUUID) throws IOException {
    try (StorageInsightsClient storageInsightsClient = StorageInsightsClient.create()) {
      ReportConfig config =
          storageInsightsClient.getReportConfig(
              ReportConfigName.of(projectID, location, reportConfigUUID));
      String extension = config.hasCsvOptions() ? "csv" : "parquet";
      System.out.println(
          "You can use the Google Cloud Storage Client to download the following objects from Google Cloud Storage:");
      for (ReportDetail reportDetail :
          storageInsightsClient.listReportDetails(config.getName()).iterateAll()) {
        for (long index = reportDetail.getShardsCount() - 1; index >= 0; index--) {
          System.out.println(reportDetail.getReportPathPrefix() + index + "." + extension);
        }
      }
    }
  }
}

// [END storageinsights_get_inventory_report_names]
