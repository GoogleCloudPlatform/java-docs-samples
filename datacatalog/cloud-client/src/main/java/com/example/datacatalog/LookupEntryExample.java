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

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.datacatalog.Entry;
import com.google.cloud.datacatalog.LookupEntryRequest;
import com.google.cloud.datacatalog.v1beta1.DataCatalogClient;

public class LookupEntryExample {

  private static Entry lookupBigQueryDataset(String projectId,
      String datasetId) throws Exception {

    String linkedResource = String.format(
        "//bigquery.googleapis.com/projects/%s/datasets/%s",
        projectId, datasetId);

    LookupEntryRequest request = LookupEntryRequest.newBuilder()
        .setLinkedResource(linkedResource).build();

    try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {
      return dataCatalogClient.lookupEntry(request);
    } catch (ApiException e) {
      System.out.print(e.getStatusCode().getCode());
    }

    return null;
  }

  private static Entry lookupBigQueryDatasetSqlResource(String projectId,
      String datasetId) throws Exception {

    String sqlResource = String.format(
        "bigquery.dataset.`%s`.`%s`", projectId, datasetId);

    LookupEntryRequest request = LookupEntryRequest.newBuilder()
        .setSqlResource(sqlResource).build();

    try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {
      return dataCatalogClient.lookupEntry(request);
    } catch (ApiException e) {
      System.out.print(e.getStatusCode().getCode());
    }

    return null;
  }

  private static Entry lookupBigQueryTable(String projectId, String datasetId,
      String tableId) throws Exception {

    String linkedResource = String.format(
        "//bigquery.googleapis.com/projects/%s/datasets/%s/tables/%s",
        projectId, datasetId, tableId);

    LookupEntryRequest request = LookupEntryRequest.newBuilder()
        .setLinkedResource(linkedResource).build();

    try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {
      return dataCatalogClient.lookupEntry(request);
    } catch (ApiException e) {
      System.out.print(e.getStatusCode().getCode());
    }

    return null;
  }

  private static Entry lookupBigQueryTableSqlResource(String projectId,
      String datasetId, String tableId) throws Exception {

    String sqlResource = String.format(
        "bigquery.table.`%s`.`%s`.`%s`", projectId, datasetId, tableId);

    LookupEntryRequest request = LookupEntryRequest.newBuilder()
        .setSqlResource(sqlResource).build();

    try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {
      return dataCatalogClient.lookupEntry(request);
    } catch (ApiException e) {
      System.out.print(e.getStatusCode().getCode());
    }

    return null;
  }

  private static Entry lookupPubSubTopic(String projectId, String topicId)
      throws Exception {

    String linkedResource = String.format(
        "//pubsub.googleapis.com/projects/%s/topics/%s", projectId, topicId);

    LookupEntryRequest request = LookupEntryRequest.newBuilder()
        .setLinkedResource(linkedResource).build();

    try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {
      return dataCatalogClient.lookupEntry(request);
    } catch (ApiException e) {
      System.out.print(e.getStatusCode().getCode());
    }

    return null;
  }

  private static Entry lookupPubSubTopicSqlResource(String projectId,
      String topicId) throws Exception {

    String sqlResource = String.format(
        "pubsub.topic.`%s`.`%s`", projectId, topicId);

    LookupEntryRequest request = LookupEntryRequest.newBuilder()
        .setSqlResource(sqlResource).build();

    try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {
      return dataCatalogClient.lookupEntry(request);
    } catch (ApiException e) {
      System.out.print(e.getStatusCode().getCode());
    }

    return null;
  }

  /**
   * Lookup a catalog entry.
   *
   * @param args projectId
   *             { bigquery-dataset | bigquery-table | pubsub-topic },
   *             datasetId | topicId,
   *             tableId (required when looking for tables),
   *             --sql-resource (optional)
   */
  public static void main(String... args) {

    try {
      Entry entry = lookupEntry(args);
      if (entry != null) {
        System.out.printf("Entry name: %s\n", entry.getName());
      }
    } catch (Exception e) {
      System.out.print("Error during lookupEntry:\n" + e.toString());
    }
  }

  private static Entry lookupEntry(String... args) throws Exception {

    String projectId = args[0];
    String resourceType = args[1];

    boolean useSqlResource = "--sql-resource".equals(args[args.length - 1]);

    switch (resourceType) {
      case "bigquery-dataset":
        return useSqlResource ?
            lookupBigQueryDatasetSqlResource(projectId, args[2]) :
            lookupBigQueryDataset(projectId, args[2]);
      case "bigquery-table":
        return useSqlResource ?
            lookupBigQueryTableSqlResource(projectId, args[2], args[3]) :
            lookupBigQueryTable(projectId, args[2], args[3]);
      case "pubsub-topic":
        return useSqlResource ?
            lookupPubSubTopicSqlResource(projectId, args[2]) :
            lookupPubSubTopic(projectId, args[2]);
      default:
        System.out.printf("Invalid resource type: %s\n", resourceType);
        return null;
    }
  }
}
