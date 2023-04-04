/*
 * Copyright 2018 Google LLC
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

package com.example.asset;

// [START asset_quickstart_export_assets]
// Imports the Google Cloud client library

import com.google.cloud.ServiceOptions;
import com.google.cloud.asset.v1.AssetServiceClient;
import com.google.cloud.asset.v1.ContentType;
import com.google.cloud.asset.v1.ExportAssetsRequest;
import com.google.cloud.asset.v1.ExportAssetsRequest.Builder;
import com.google.cloud.asset.v1.ExportAssetsResponse;
import com.google.cloud.asset.v1.GcsDestination;
import com.google.cloud.asset.v1.OutputConfig;
import com.google.cloud.asset.v1.ProjectName;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExportAssetsExample {

  // Use the default project Id.
  private static final String projectId = ServiceOptions.getDefaultProjectId();

  /**
   * Export assets for a project.
   *
   * @param exportPath where the results will be exported to
   * @param contentType determines the schema for the table
   * @param assetTypes a list of asset types to export. if empty, export all.
   */
  public static void exportAssets(String exportPath, ContentType contentType, String[] assetTypes)
      throws IOException,
          IllegalArgumentException,
          InterruptedException,
          ExecutionException,
          TimeoutException {
    try (AssetServiceClient client = AssetServiceClient.create()) {
      ProjectName parent = ProjectName.of(projectId);
      OutputConfig outputConfig =
          OutputConfig.newBuilder()
              .setGcsDestination(GcsDestination.newBuilder().setUri(exportPath).build())
              .build();
      Builder exportAssetsRequestBuilder =
          ExportAssetsRequest.newBuilder()
              .setParent(parent.toString())
              .setContentType(contentType)
              .setOutputConfig(outputConfig);
      if (assetTypes.length > 0) {
        exportAssetsRequestBuilder.addAllAssetTypes(Arrays.asList(assetTypes));
      }
      ExportAssetsRequest request = exportAssetsRequestBuilder.build();
      ExportAssetsResponse response = client.exportAssetsAsync(request).get(5, TimeUnit.MINUTES);
      System.out.println(response);
    }
  }
}
// [END asset_quickstart_export_assets]
