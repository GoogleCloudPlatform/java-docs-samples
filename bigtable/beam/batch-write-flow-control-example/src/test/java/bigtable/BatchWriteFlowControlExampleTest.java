/*
 * Copyright 2024 Google LLC
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

package bigtable;

import static org.junit.Assert.assertNotNull;

import bigtable.BatchWriteFlowControlExample.BigtablePipelineOptions;
import com.google.cloud.bigtable.admin.v2.BigtableInstanceAdminClient;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient;
import com.google.cloud.bigtable.admin.v2.models.CreateInstanceRequest;
import com.google.cloud.bigtable.admin.v2.models.CreateTableRequest;
import com.google.cloud.bigtable.admin.v2.models.StorageType;
import com.google.common.truth.Truth;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import org.apache.beam.runners.dataflow.DataflowRunner;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BatchWriteFlowControlExampleTest {

  private static final String PROJECT_ENV = "GOOGLE_CLOUD_PROJECT";
  private static final String INSTANCE_ID = "i-" + UUID.randomUUID().toString().substring(0, 10);
  private static final String CLUSTER_ID = "c-" + UUID.randomUUID().toString().substring(0, 10);
  private static final String REGION_ID = "us-central1";
  private static final String ZONE_ID = "us-central1-b";
  private static final String TABLE_ID = "test-table";
  private static final String COLUMN_FAMILY = "cf";
  private static final long NUM_ROWS = 100;
  private static String projectId;
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
    projectId = requireEnv(PROJECT_ENV);
    try (BigtableInstanceAdminClient instanceAdmin =
        BigtableInstanceAdminClient.create(projectId)) {
      CreateInstanceRequest request =
          CreateInstanceRequest.of(INSTANCE_ID).addCluster(CLUSTER_ID, ZONE_ID, 1, StorageType.SSD);
      instanceAdmin.createInstance(request);
    } catch (IOException e) {
      System.out.println("Error during BeforeClass while creating instance:" + e);
      Assert.fail();
    }
    try (BigtableTableAdminClient tableAdmin =
        BigtableTableAdminClient.create(projectId, INSTANCE_ID)) {
      CreateTableRequest request = CreateTableRequest.of(TABLE_ID).addFamily(COLUMN_FAMILY);
      tableAdmin.createTable(request);
    } catch (IOException e) {
      System.out.println("Error during BeforeClass while creating table:" + e);
      Assert.fail();
    }
  }

  @Before
  public void setupStream() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @AfterClass
  public static void afterClass() {
    try (BigtableInstanceAdminClient instanceAdmin =
        BigtableInstanceAdminClient.create(projectId)) {
      instanceAdmin.deleteInstance(INSTANCE_ID);
    } catch (IOException e) {
      System.out.println("Error during AfterClass while deleting instance:" + e);
    }
  }

  @Test
  public void test() {
    BigtablePipelineOptions options =
        PipelineOptionsFactory.create().as(BigtablePipelineOptions.class);
    options.setProject(projectId);
    options.setBigtableInstanceId(INSTANCE_ID);
    options.setBigtableTableId(TABLE_ID);
    options.setBigtableRows(NUM_ROWS);
    options.setRunner(DataflowRunner.class);
    options.setRegion(REGION_ID);

    BatchWriteFlowControlExample.run(options);

    String output = bout.toString();

    Truth.assertThat(output).contains("Generating 100 rows");
  }
}
