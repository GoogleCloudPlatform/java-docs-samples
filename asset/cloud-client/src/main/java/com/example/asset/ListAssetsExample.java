/*
 * Copyright 2020 Google LLC
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

// [START asset_quickstart_list_assets]
// Imports the Google Cloud client library

import com.google.cloud.ServiceOptions;
import com.google.cloud.asset.v1.ProjectName;
import com.google.cloud.asset.v1p5beta1.AssetServiceClient;
import com.google.cloud.asset.v1p5beta1.AssetServiceClient.ListAssetsPagedResponse;
import com.google.cloud.asset.v1p5beta1.ContentType;
import com.google.cloud.asset.v1p5beta1.ListAssetsRequest;
import java.util.Arrays;

public class ListAssetsExample {

  // Use the default project Id (configure it by setting environment variable
  // "GOOGLE_CLOUD_PROJECT").
  private static final String projectId = ServiceOptions.getDefaultProjectId();

  // List assets of a project.
  // @param args types of the assets to list.
  public static void main(String... args) throws Exception {
    // Asset types, e.g.:
    // "storage.googleapis.com/Bucket,bigquery.googleapis.com/Table".
    // See full list of supported asset types at
    // https://cloud.google.com/asset-inventory/docs/supported-asset-types.
    String[] assetTypes = args[0].split(",");
    try (AssetServiceClient client = AssetServiceClient.create()) {
      ProjectName parent = ProjectName.of(projectId);
      ContentType contentType = ContentType.CONTENT_TYPE_UNSPECIFIED;
      ListAssetsRequest request = ListAssetsRequest.newBuilder()
          .setParent(parent.toString())
          .addAllAssetTypes(Arrays.asList(assetTypes))
          .setContentType(contentType)
          .build();
      // Repeatedly call ListAssets until page token is empty.
      ListAssetsPagedResponse response = client.listAssets(request);
      System.out.println(response);
      while (!response.getNextPageToken().isEmpty()) {
        request = request.toBuilder()
            .setPageToken(response.getNextPageToken()).build();
        response = client.listAssets(request);
        System.out.println(response);
      }
    }
  }
}
// [END asset_quickstart_list_assets]
