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

// [START bigquery_revoke_dataset_access]

import com.google.cloud.bigquery.Acl;
import com.google.cloud.bigquery.Acl.Entity;
import com.google.cloud.bigquery.Acl.Group;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import java.util.List;

public class RevokeDatasetAccess {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    // Project and dataset from which to get the access policy.
    String projectId = "MY_PROJECT_ID";
    String datasetName = "MY_DATASET_NAME";
    // Group to remove from the ACL
    String entityEmail = "group-to-remove@example.com";

    revokeDatasetAccess(projectId, datasetName, entityEmail);
  }

  public static void revokeDatasetAccess(String projectId, String datasetName, String entityEmail) {
    try {
      // Initialize client that will be used to send requests. This client only needs
      // to be created once, and can be reused for multiple requests.
      BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

      // Create datasetId with the projectId and the datasetName.
      DatasetId datasetId = DatasetId.of(projectId, datasetName);
      Dataset dataset = bigquery.getDataset(datasetId);

      // Create a new Entity with the corresponding type and email
      // "user-or-group-to-remove@example.com"
      // For more information on the types of Entities available see:
      // https://cloud.google.com/java/docs/reference/google-cloud-bigquery/latest/com.google.cloud.bigquery.Acl.Entity
      // and
      // https://cloud.google.com/java/docs/reference/google-cloud-bigquery/latest/com.google.cloud.bigquery.Acl.Entity.Type
      Entity entity = new Group(entityEmail);

      // To revoke access to a dataset, remove elements from the Acl list.
      // Find more information about ACL and the Acl Class here:
      // https://cloud.google.com/storage/docs/access-control/lists
      // https://cloud.google.com/java/docs/reference/google-cloud-bigquery/latest/com.google.cloud.bigquery.Acl
      // Remove the entity from the ACLs list.
      List<Acl> acls =
          dataset.getAcl().stream().filter(acl -> !acl.getEntity().equals(entity)).toList();

      // Update the ACLs by setting the new list.
      bigquery.update(dataset.toBuilder().setAcl(acls).build());
      System.out.println("ACLs of \"" + datasetName + "\" updated successfully");
    } catch (BigQueryException e) {
      System.out.println("ACLs were not updated \n" + e.toString());
    }
  }
}
// [END bigquery_revoke_dataset_access]
