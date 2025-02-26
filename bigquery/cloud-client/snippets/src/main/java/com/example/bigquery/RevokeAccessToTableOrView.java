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

// [START bigquery_revoke_access_to_table_or_view]
import com.google.cloud.Identity;
import com.google.cloud.Policy;
import com.google.cloud.Role;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.TableId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RevokeAccessToTableOrView {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    // Project, dataset and resource (table or view) from which to get the access policy
    String projectId = "MY_PROJECT_ID";
    String datasetName = "MY_DATASET_NAME";
    String resourceName = "MY_RESOURCE_NAME";
    revokeAccessToTableOrView(projectId, datasetName, resourceName);
  }

  public static void revokeAccessToTableOrView(
      String projectId, String datasetName, String resourceName) {
    try {
      // Initialize client that will be used to send requests. This client only needs to be created
      // once, and can be reused for multiple requests.
      BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

      // Create table identity given the projectId, the datasetName and the resourceName.
      TableId tableId = TableId.of(projectId, datasetName, resourceName);

      // Remove identity from bindings and replace it in the current IAM policy
      Policy policy = bigquery.getIamPolicy(tableId);
      Map<Role, Set<Identity>> binding = new HashMap<>(policy.getBindings());
      binding.remove(Role.of("roles/bigquery.dataViewer"));
      policy.toBuilder().setBindings(binding).build();

      // Update the IAM policy by setting the new one
      bigquery.setIamPolicy(tableId, policy);

      System.out.println("IAM policy of resource \"" + resourceName + "\" updated successfully");
    } catch (BigQueryException e) {
      System.out.println("IAM policy was not updated. \n" + e.toString());
    }
  }
}
// [END bigquery_revoke_access_to_table_or_view]
