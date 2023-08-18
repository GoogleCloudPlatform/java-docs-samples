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

// [START storageinsights_create_inventory_report_config]

import com.google.cloud.storageinsights.v1.CSVOptions;
import com.google.cloud.storageinsights.v1.CloudStorageDestinationOptions;
import com.google.cloud.storageinsights.v1.CloudStorageFilters;
import com.google.cloud.storageinsights.v1.CreateReportConfigRequest;
import com.google.cloud.storageinsights.v1.FrequencyOptions;
import com.google.cloud.storageinsights.v1.LocationName;
import com.google.cloud.storageinsights.v1.ObjectMetadataReportOptions;
import com.google.cloud.storageinsights.v1.ReportConfig;
import com.google.cloud.storageinsights.v1.StorageInsightsClient;
import com.google.common.collect.ImmutableList;
import com.google.type.Date;
import java.io.IOException;

public class CreateInventoryReportConfig {
  // [END storageinsights_create_inventory_report_config]

  public static void main(String[] args) throws IOException {
    // The ID of your Google Cloud Project
    String projectId = "your-project-id";

    // The location of your source and destination buckets
    String bucketLocation = "us-west-1";

    // The name of your Google Cloud Storage source bucket
    String sourceBucket = "your-source-bucket";

    // The name of your Google Cloud Storage destination bucket
    String destinationBucket = "your-destination-bucket";

    createInventoryReportConfig(projectId, bucketLocation, sourceBucket, destinationBucket);
  }

  // [START storageinsights_create_inventory_report_config]

  public static void createInventoryReportConfig(
      String projectId, String bucketLocation, String sourceBucket, String destinationBucket)
      throws IOException {
    try (StorageInsightsClient storageInsightsClient = StorageInsightsClient.create()) {
      ReportConfig reportConfig =
          ReportConfig.newBuilder()
              .setDisplayName("Example inventory report configuration")
              .setFrequencyOptions(
                  FrequencyOptions.newBuilder()
                      .setFrequency(FrequencyOptions.Frequency.WEEKLY)
                      .setStartDate(Date.newBuilder().setDay(15).setMonth(8).setYear(3022).build())
                      .setEndDate(Date.newBuilder().setDay(15).setMonth(9).setYear(3022).build())
                      .build())
              .setCsvOptions(
                  CSVOptions.newBuilder()
                      .setDelimiter(",")
                      .setRecordSeparator("\n")
                      .setHeaderRequired(true)
                      .build())
              .setObjectMetadataReportOptions(
                  ObjectMetadataReportOptions.newBuilder()
                      .addAllMetadataFields(ImmutableList.of("project", "name", "bucket"))
                      .setStorageFilters(
                          CloudStorageFilters.newBuilder().setBucket(sourceBucket).build())
                      .setStorageDestinationOptions(
                          CloudStorageDestinationOptions.newBuilder()
                              .setBucket(destinationBucket)
                              .build())
                      .build())
              .build();
      CreateReportConfigRequest request =
          CreateReportConfigRequest.newBuilder()
              .setParent(LocationName.of(projectId, bucketLocation).toString())
              .setReportConfig(reportConfig)
              .build();
      ReportConfig response = storageInsightsClient.createReportConfig(request);
      System.out.println("Created inventory report config with name " + response.getName());
    }
  }
}
// [END storageinsights_create_inventory_report_config]
