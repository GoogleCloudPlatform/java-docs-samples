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

// [START spanner_update_data_with_numeric]
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;

class InsertNumericDataSample {

  static void insertNumericData() {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project";
    String instanceId = "my-instance";
    String databaseId = "my-database";

    try (Spanner spanner =
        SpannerOptions.newBuilder().setProjectId(projectId).build().getService()) {
      DatabaseClient client =
          spanner.getDatabaseClient(DatabaseId.of(projectId, instanceId, databaseId));
      insertNumericData(client);
    }
  }

  static void insertNumericData(DatabaseClient client) {
    client.write(
        ImmutableList.of(
            Mutation.newInsertOrUpdateBuilder("SingerRevenues")
                .set("SingerId")
                .to(1L)
                .set("Revenue")
                .to(new BigDecimal("35000"))
                .build(),
            Mutation.newInsertOrUpdateBuilder("SingerRevenues")
                .set("SingerId")
                .to(6L)
                .set("Revenue")
                .to(new BigDecimal("104500"))
                .build(),
            Mutation.newInsertOrUpdateBuilder("SingerRevenues")
                .set("SingerId")
                .to(14L)
                .set("Revenue")
                .to(new BigDecimal("99999999999999999999999999999.99"))
                .build()));
    System.out.println("Records successfully inserted");
  }
}
// [END spanner_update_data_with_numeric]
