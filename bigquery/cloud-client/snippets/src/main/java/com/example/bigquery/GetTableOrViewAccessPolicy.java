/*
 * Copyright 2025 Google LLC
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

package com.example.bigquery;

// [START bigquery_view_table_or_view_access_policy]

import com.google.cloud.Policy;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.TableId;

public class GetTableOrViewAccessPolicy {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    // Project, dataset and resource (table or view) from which to get the access policy.
    String projectId = "MY_PROJECT_ID";
    String datasetName = "MY_DATASET_NAME";
    String resourceName = "MY_RESOURCE_NAME";
    getTableOrViewAccessPolicy(projectId, datasetName, resourceName);
  }

  public static void getTableOrViewAccessPolicy(
      String projectId, String datasetName, String resourceName) {
    try {
      // Initialize client that will be used to send requests. This client only needs
      // to be created once, and can be reused for multiple requests.
      BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

      // Create table identity given the projectId, the datasetName and the resourceName.
      TableId tableId = TableId.of(projectId, datasetName, resourceName);

      // Get the table IAM policy.
      Policy policy = bigquery.getIamPolicy(tableId);

      // Show policy details.
      // Find more information about the Policy Class here:
      // https://cloud.google.com/java/docs/reference/google-cloud-core/latest/com.google.cloud.Policy
      System.out.println(
          "IAM policy info of resource \"" + resourceName + "\" retrieved succesfully");
      System.out.println();
      System.out.println("IAM policy info: " + policy.toString());
    } catch (BigQueryException e) {
      System.out.println("IAM policy info not retrieved. \n" + e.toString());
    }
  }
}
 // [END bigquery_view_table_or_view_access_policy]
