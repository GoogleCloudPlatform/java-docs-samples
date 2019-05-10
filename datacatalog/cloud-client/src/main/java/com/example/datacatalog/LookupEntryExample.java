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

  /**
   * Lookup a catalog entry.
   *
   * @param args projectId
   *             resourceType
   *                { bigquery-dataset | bigquery-table | pubsub-topic },
   *             datasetId,
   *             tableId,
   *             topicId,
   *             useSqlResource (optional)
   * @throws Exception exception thrown if operation is unsuccessful
   */
  public static void main(String... args) throws Exception {

    LookupEntryRequest request = buildRequest(args);
    try (DataCatalogClient dataCatalogClient = DataCatalogClient.create()) {
      Entry entry = dataCatalogClient.lookupEntry(request);
      System.out.printf("Entry name: %s:\n", entry.getName());
    } catch (ApiException e) {
      System.out.print(e.getStatusCode().getCode());
      System.out.print(e.isRetryable());
    }
  }

  private static LookupEntryRequest buildRequest(String... args) {

    String projectId = args[0];
    String resourceType = args[1];

    boolean useSqlResource = "--sql-resource".equals(args[args.length - 1]);

    return useSqlResource
        ? buildSqlResourceRequest(projectId, resourceType, args)
        : buildLinkedResourceRequest(projectId, resourceType, args);
  }

  private static LookupEntryRequest buildLinkedResourceRequest(
      String projectId, String resourceType, String... args) {

    String linkedResource = null;

    switch (resourceType) {
      case "bigquery-dataset":
        linkedResource = String.format(
            "//bigquery.googleapis.com/projects/%s/datasets/%s",
            projectId, args[2]);
        break;
      case "bigquery-table":
        linkedResource = String.format(
            "//bigquery.googleapis.com/projects/%s/datasets/%s/tables/%s",
            projectId, args[2], args[3]);
        break;
      case "pubsub-topic":
        linkedResource = String.format(
            "//pubsub.googleapis.com/projects/%s/topics/%s",
            projectId, args[2]);
        break;
      default:
        break;
    }

    return LookupEntryRequest.newBuilder()
        .setLinkedResource(linkedResource).build();
  }

  private static LookupEntryRequest buildSqlResourceRequest(
      String projectId, String resourceType, String... args) {

    String sqlResource = null;

    switch (resourceType) {
      case "bigquery-dataset":
        sqlResource = String.format("bigquery.dataset.`%s`.`%s`",
            projectId, args[2]);
        break;
      case "bigquery-table":
        sqlResource = String.format("bigquery.table.`%s`.`%s`.`%s`",
            projectId, args[2], args[3]);
        break;
      case "pubsub-topic":
        sqlResource = String.format("pubsub.topic.`%s`.`%s`",
            projectId, args[2]);
        break;
      default:
        break;
    }

    return LookupEntryRequest.newBuilder()
        .setSqlResource(sqlResource).build();
  }
}
