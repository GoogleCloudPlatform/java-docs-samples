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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RevokeAccessToTableOrView {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    // Project, dataset and resource (table or view) from which to get the access policy
    String projectId = "MY_PROJECT_ID";
    String datasetName = "MY_DATASET_NAME";
    String resourceName = "MY_RESOURCE_NAME";
    // Role to remove from the access policy
    Role role = Role.of("roles/bigquery.dataViewer");
    // Identity to remove from the access policy
    Identity user = Identity.user("user-add@example.com");
    revokeAccessToTableOrView(projectId, datasetName, resourceName, role, user);
  }

  public static void revokeAccessToTableOrView(
      String projectId, String datasetName, String resourceName, Role role, Identity identity) {
    try {
      // Initialize client that will be used to send requests. This client only needs
      // to be created once, and can be reused for multiple requests.
      BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

      // Create table identity given the projectId, the datasetName and the resourceName.
      TableId tableId = TableId.of(projectId, datasetName, resourceName);

      // Remove either identities or roles, or both from bindings and replace it in
      // the current IAM policy.
      Policy policy = bigquery.getIamPolicy(tableId);
      // Create a copy of a inmutable map.
      Map<Role, Set<Identity>> bindings = new HashMap<>(policy.getBindings());

      // Remove all identities with a specific role.
      bindings.remove(role);
      // Update bindings.
      policy = policy.toBuilder().setBindings(bindings).build();

      // Remove one identity in all the existing roles.
      for (Role roleKey : bindings.keySet()) {
        if (bindings.get(roleKey).contains(identity)) {
          // Create a copy of a inmutable set if the identity is present in the role.
          Set<Identity> identities = new HashSet<>(bindings.get(roleKey));
          // Remove identity.
          identities.remove(identity);
          bindings.put(roleKey, identities);
          if (bindings.get(roleKey).isEmpty()) {
            // Remove the role if it has no identities.
            bindings.remove(roleKey);
          }
        }
      }
      // Update bindings.
      policy = policy.toBuilder().setBindings(bindings).build();

      // Update the IAM policy by setting the new one.
      bigquery.setIamPolicy(tableId, policy);

      System.out.println("IAM policy of resource \"" + resourceName + "\" updated successfully");
    } catch (BigQueryException e) {
      System.out.println("IAM policy was not updated. \n" + e.toString());
    }
  }
}
// [END bigquery_revoke_access_to_table_or_view]
