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

public class LookupEntryBigQueryTable {

  /**
   * Lookup the Data Catalog entry referring to a BigQuery Table
   *
   * @param projectId The project ID to which the Dataset belongs, e.g. 'my-project'
   * @param datasetId The dataset ID to which the Table belongs, e.g. 'my_dataset'
   * @param tableId The table ID to which the Catalog Entry refers, e.g. 'my_table'
   * @param lookupBySqlResource Indicates whether the lookup should be performed by Sql Resource
   *     instead of Linked Resource, e.g. 'false'
   */
  private static void lookupEntryBigQueryTable(
      String projectId, String datasetId, String tableId, boolean lookupBySqlResource) {

    LookupEntryRequest request;

    // Construct the Lookup request to be sent by the client.
    if (lookupBySqlResource) {
      String sqlResource =
          String.format("bigquery.table.`%s`.`%s`.`%s`", projectId, datasetId, tableId);
      request = LookupEntryRequest.newBuilder().setSqlResource(sqlResource).build();
    } else {
      String linkedResource =
          String.format(
              "//bigquery.googleapis.com/projects/%s/datasets/%s/tables/%s",
              projectId, datasetId, tableId);
      request = LookupEntryRequest.newBuilder().setLinkedResource(linkedResource).build();
    }

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {
      Entry entry = dataCatalogClient.lookupEntry(request);
      System.out.printf("Entry name: %s\n", entry.getName());
    } catch (Exception e) {
      System.out.print("Error during lookupEntryBigQueryTable:\n" + e.toString());
    }
  }

  /**
   * Command line application to lookup the Data Catalog entry referring to a BigQuery Table.
   * Requires 3 positional args: projectId, datasetId, and tableId. A fourth arg is optional:
   * lookupBySqlResource (when set, the lookup is done by Sql Resource instead of Linked Resource).
   */
  public static void main(String... args) {

    String projectId = args[0];
    String datasetId = args[1];
    String tableId = args[2];
    boolean lookupBySqlResource = "-lookupBySqlResource".equals(args[args.length - 1]);

    lookupEntryBigQueryTable(projectId, datasetId, tableId, lookupBySqlResource);
  }
}
