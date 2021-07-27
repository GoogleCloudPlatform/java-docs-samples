/*
 * Copyright 2021 Google LLC
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

import com.google.cloud.bigtable.admin.v2.BigtableInstanceAdminClient;
import com.google.cloud.bigtable.admin.v2.models.CreateInstanceRequest;
import com.google.cloud.bigtable.admin.v2.models.Instance;
import com.google.cloud.bigtable.admin.v2.models.StorageType;
import com.google.common.truth.Truth;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BulkWriteTest {

  private static final String PROJECT_ENV = "GOOGLE_CLOUD_PROJECT";
  private static final String INSTANCE_ID = "ins-" + UUID.randomUUID().toString().substring(0, 10);
  private static final String CLUSTER_ID = "cl-" + UUID.randomUUID().toString().substring(0, 10);
  private static final String REGION_ID = "us-central1";
  private static final String ZONE_ID = "us-central1-b";
  private static final int NUM_TABLES_TO_CREATE = 3;
  private static final double TABLE_SIZE = .5;
  private static final double BIGTABLE_SIZE = TABLE_SIZE * NUM_TABLES_TO_CREATE;

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
      CreateInstanceRequest request = CreateInstanceRequest.of(INSTANCE_ID)
          .addCluster(CLUSTER_ID, ZONE_ID, 1, StorageType.SSD);
      Instance instance = instanceAdmin.createInstance(request);
    } catch (IOException e) {
      System.out.println("Error during BeforeClass while creating instance: \n" + e.toString());
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
      System.out.println("Error during AfterClass while deleting instance: \n" + e.toString());
    }
  }

  @Test
  public void testBulkWrite() {
    BulkWrite.main(
        new String[]{
            "--bigtableInstanceId=" + INSTANCE_ID,
            "--bigtableSize=" + BIGTABLE_SIZE,
            "--runner=dataflow",
            "--region=" + REGION_ID
        });

    String output = bout.toString();

    Truth.assertThat(output).contains("Cluster size 1");
    Truth.assertThat(output).contains("Creating 3 tables");
    Truth.assertThat(output).contains("Generate 500000 rows at 40MB per second for 3 tables");
    Truth.assertThat(output).contains("Create mutations that write 1 MB to each row");

    BulkWrite.main(
        new String[]{
            "--bigtableInstanceId=" + INSTANCE_ID,
            "--bigtableSize=0"
        });

    output = bout.toString();
    Truth.assertThat(output).contains("Deleted 3 tables");
  }
}
