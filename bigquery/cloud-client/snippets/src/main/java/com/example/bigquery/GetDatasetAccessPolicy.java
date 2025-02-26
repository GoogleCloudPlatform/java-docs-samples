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

// [START bigquery_view_dataset_access_policy]

import com.google.cloud.bigquery.Acl;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import java.util.List;

public class GetDatasetAccessPolicy {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    // Project and dataset from which to get the access policy.
    String projectId = "MY_PROJECT_ID";
    String datasetName = "MY_DATASET_NAME";
    getDatasetAccessPolicy(projectId, datasetName);
  }

  public static void getDatasetAccessPolicy(String projectId, String datasetName) {
    try {
      // Initialize client that will be used to send requests. This client only needs to be created
      // once, and can be reused for multiple requests.
      BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

      // Create datasetId with the projectId and the datasetName.
      DatasetId datasetId = DatasetId.of(projectId, datasetName);
      Dataset dataset = bigquery.getDataset(datasetId);

      // Show ACL details.
      // Find more information about ACL and the Acl Class here:
      // https://cloud.google.com/storage/docs/access-control/lists
      // https://cloud.google.com/java/docs/reference/google-cloud-bigquery/latest/com.google.cloud.bigquery.Acl
      List<Acl> acls = dataset.getAcl();
      System.out.println("ACLs in dataset \"" + dataset.getDatasetId().getDataset() + "\":");
      System.out.println(acls.toString());
      for (Acl acl : acls) {
        System.out.println();
        System.out.println("Role: " + acl.getRole());
        System.out.println("Entity: " + acl.getEntity());
      }
    } catch (BigQueryException e) {
      System.out.println("ACLs info not retrieved. \n" + e.toString());
    }
  }
}
// [END bigquery_view_dataset_access_policy]
