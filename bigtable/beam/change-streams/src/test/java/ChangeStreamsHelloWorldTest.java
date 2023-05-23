/*
 * Copyright 2023 Google LLC
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

import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.models.RowMutation;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ChangeStreamsHelloWorldTest {

  // This table needs to be created manually before running the test since there is no API to create
  // change-stream enabled tables yet. For java-docs-samples, the table should already be created,
  // but if deleted, run the create table command in the README.
  private static final String TABLE_ID = "change-stream-hello-world-test";
  private static final String COLUMN_FAMILY_NAME_1 = "cf1";
  private static final String COLUMN_FAMILY_NAME_2 = "cf2";
  private static final String REGION = "us-central1";

  private static String projectId;
  private static String instanceId;
  private ByteArrayOutputStream bout;

  private static String requireEnv(String varName) {
    String value = System.getenv(varName);
    assertNotNull(
        String.format("Environment variable '%s' is required to perform these tests.", varName),
        value);
    return value;
  }

  @BeforeClass
  public static void beforeClass() {
    projectId = requireEnv("GOOGLE_CLOUD_PROJECT");
    instanceId = requireEnv("BIGTABLE_TESTING_INSTANCE");
  }

  @Before
  public void setupStream() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @Test
  public void testChangeStreamsHelloWorld() throws IOException, InterruptedException {
    String[] args = {
        "--bigtableProjectId=" + projectId,
        "--bigtableInstanceId=" + instanceId,
        "--bigtableTableId=" + TABLE_ID
    };

    new Thread(() -> ChangeStreamsHelloWorld.main(args)).start();

    // Pause for job to start.
    Thread.sleep(10 * 1000);

    BigtableDataClient dataClient = BigtableDataClient.create(projectId, instanceId);
    String rowKey = UUID.randomUUID().toString().substring(0, 20);
    dataClient.mutateRow(RowMutation.create(TABLE_ID, rowKey)
        .setCell(COLUMN_FAMILY_NAME_1, "col a", "a"));

    dataClient.mutateRow(RowMutation.create(TABLE_ID, rowKey)
        .deleteCells(COLUMN_FAMILY_NAME_1, "col a"));

    dataClient.mutateRow(RowMutation.create(TABLE_ID, rowKey).deleteRow());

    // Wait for change to be captured.
    Thread.sleep(15 * 1000);

    String output = bout.toString();
    assertThat(output).contains("USER,SetCell,cf1,col a,a");
    assertThat(output).contains("USER,DeleteCells,cf1,col a,0-0");
    assertThat(output).contains("USER,DeleteFamily,cf1");
    assertThat(output).contains("USER,DeleteFamily,cf2");
  }
}
