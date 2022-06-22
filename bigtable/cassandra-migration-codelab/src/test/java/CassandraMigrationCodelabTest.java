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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminSettings;
import com.google.cloud.bigtable.admin.v2.models.CreateTableRequest;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.Test;

public class CassandraMigrationCodelabTest {

  private static final String INSTANCE_ENV = "BIGTABLE_TESTING_INSTANCE";
  private static final String COLUMN_FAMILY_NAME = "stats_summary";
  private static final String TABLE_PREFIX = "cass-";

  private static final String TABLE_ID =
      TABLE_PREFIX + UUID.randomUUID().toString().substring(0, 20);
  private static BigtableTableAdminClient adminClient;
  private static String projectId;
  private static String instanceId;
  private CassandraMigrationCodelab cassandraMigrationCodelab;

  private static String requireEnv(String varName) {
    assertNotNull(
        System.getenv(varName),
        "Environment variable '%s' is required to perform these tests.".format(varName));
    return System.getenv(varName);
  }

  @Test
  public void testRunDoesNotFail() throws Exception {
    projectId = requireEnv("GOOGLE_CLOUD_PROJECT");
    instanceId = requireEnv(INSTANCE_ENV);
    BigtableTableAdminSettings adminSettings =
        BigtableTableAdminSettings.newBuilder()
            .setProjectId(projectId)
            .setInstanceId(instanceId)
            .build();
    adminClient = BigtableTableAdminClient.create(adminSettings);

    cassandraMigrationCodelab = new CassandraMigrationCodelab(projectId, instanceId, TABLE_ID);
    adminClient.createTable(CreateTableRequest.of(TABLE_ID).addFamily(COLUMN_FAMILY_NAME));

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    cassandraMigrationCodelab.run();

    String output = bout.toString();
    assertThat(output).doesNotContainMatch("Error during");

    adminClient.deleteTable(TABLE_ID);
  }
}
