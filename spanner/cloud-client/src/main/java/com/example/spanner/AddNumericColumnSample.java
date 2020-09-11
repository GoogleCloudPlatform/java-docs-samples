/*
 * Copyright 2020 Google Inc.
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

package com.example.spanner;

//[START spanner_add_numeric_column]
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.common.collect.ImmutableList;
import com.google.spanner.admin.database.v1.UpdateDatabaseDdlMetadata;
import java.util.concurrent.ExecutionException;

class AddNumericColumnSample {

  static void addNumericColumn() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";

    try (Spanner spanner =
        SpannerOptions.newBuilder().setProjectId(projectId).build().getService()) {
      DatabaseAdminClient adminClient = spanner.getDatabaseAdminClient();
      addNumericColumn(adminClient, instanceId, databaseId);
    }
  }

  static void addNumericColumn(
      DatabaseAdminClient adminClient, String instanceId, String databaseId) {
    OperationFuture<Void, UpdateDatabaseDdlMetadata> operation =
        adminClient.updateDatabaseDdl(
            instanceId,
            databaseId,
            ImmutableList.of("ALTER TABLE Venues ADD COLUMN Revenue NUMERIC"),
            null);
    try {
      operation.get();
      System.out.printf("Successfully added column `Revenue`%n");
    } catch (ExecutionException e) {
      System.out.printf("Adding column `Revenue` failed: %s%n", e.getCause().getMessage());
    } catch (InterruptedException e) {
      System.out.printf("Adding column `Revenue` was interrupted%n");
    }
  }
}
// [END spanner_add_numeric_column]
