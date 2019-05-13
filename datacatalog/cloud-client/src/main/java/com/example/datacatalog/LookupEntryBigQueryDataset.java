/*
 * Copyright 2019 Google Inc.
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

package com.example.datacatalog;

import com.google.cloud.datacatalog.Entry;
import com.google.cloud.datacatalog.LookupEntryRequest;
import com.google.cloud.datacatalog.v1beta1.DataCatalogClient;

public class LookupEntryBigQueryDataset {

  /**
   * Lookup the Data Catalog entry referring to a BigQuery Dataset
   *
   * @param projectId The project ID to which the Dataset belongs, e.g. 'my-project'
   * @param datasetId The dataset ID to which the Catalog Entry refers, e.g. 'my_dataset'
   * @param lookupBySqlResource Indicates whether the lookup should be performed by Sql Resource
   *     instead of Linked Resource, e.g. 'false'
   */
  private static void lookupEntryBigQueryDataset(
      String projectId, String datasetId, boolean lookupBySqlResource) {

    LookupEntryRequest request;

    // Construct the Lookup request to be sent by the client.
    if (lookupBySqlResource) {
      String sqlResource = String.format("bigquery.dataset.`%s`.`%s`", projectId, datasetId);
      request = LookupEntryRequest.newBuilder().setSqlResource(sqlResource).build();
    } else {
      String linkedResource =
          String.format("//bigquery.googleapis.com/projects/%s/datasets/%s", projectId, datasetId);
      request = LookupEntryRequest.newBuilder().setLinkedResource(linkedResource).build();
    }

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {
      Entry entry = dataCatalogClient.lookupEntry(request);
      System.out.printf("Entry name: %s\n", entry.getName());
    } catch (Exception e) {
      System.out.print("Error during lookupEntryBigQueryDataset:\n" + e.toString());
    }
  }

  /**
   * Command line application to lookup the Data Catalog entry referring to a BigQuery Dataset.
   * Requires 2 positional args: projectId and datasetId. A third arg is optional:
   * lookupBySqlResource (when set, the lookup is done by Sql Resource instead of Linked Resource).
   */
  public static void main(String... args) {

    String projectId = args[0];
    String datasetId = args[1];
    boolean lookupBySqlResource = "-lookupBySqlResource".equals(args[args.length - 1]);

    lookupEntryBigQueryDataset(projectId, datasetId, lookupBySqlResource);
  }
}
