/*
 * Copyright 2020 Google LLC
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
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertNotNull;

import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient;
import com.google.cloud.bigtable.admin.v2.models.CreateTableRequest;
import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.models.RowMutation;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MemcachedTest {

  private static final String INSTANCE_ENV = "BIGTABLE_TESTING_INSTANCE";
  private static final String TABLE_ID =
      "mobile-time-series-" + UUID.randomUUID().toString().substring(0, 20);
  private static final String COLUMN_FAMILY_NAME = "stats_summary";
  private static final String MEMCACHED_CONTAINER_NAME = "BigtableMemcachedContainerTest";

  private static String projectId;
  private static String instanceId;
  private static String discoveryEndpoint = "localhost";
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
    instanceId = requireEnv(INSTANCE_ENV);
    try (BigtableTableAdminClient adminClient =
        BigtableTableAdminClient.create(projectId, instanceId)) {
      CreateTableRequest createTableRequest =
          CreateTableRequest.of(TABLE_ID).addFamily(COLUMN_FAMILY_NAME);
      adminClient.createTable(createTableRequest);
      try (BigtableDataClient dataClient = BigtableDataClient.create(projectId, instanceId)) {
        String rowkey = "phone#4c410523#20190501";

        RowMutation rowMutation = RowMutation.create(TABLE_ID, rowkey)
            .setCell(COLUMN_FAMILY_NAME, "os_build", "PQ2A.190405.003");
        dataClient.mutateRow(rowMutation);
      }

      String[] dockerCommand = (String.format(
          "docker run --name %s -itd --rm --publish 11211:11211 sameersbn/memcached:latest",
          MEMCACHED_CONTAINER_NAME))
          .split(" ");
      Process process = new ProcessBuilder(
          dockerCommand).start();
      process.waitFor();

    } catch (Exception e) {
      System.out.println("Error during beforeClass: \n" + e.toString());
    }
  }

  @Before
  public void setupStream() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
    try {

      String[] dockerCommand = (String.format(
          "docker run --name %s -itd --rm --publish 11211:11211 sameersbn/memcached:latest",
          MEMCACHED_CONTAINER_NAME))
          .split(" ");
      Process process = new ProcessBuilder(
          dockerCommand).start();
      process.waitFor();
    } catch (Exception e) {
      e.printStackTrace(System.out);
    }
  }

  @AfterClass
  public static void afterClass() {
    try (BigtableTableAdminClient adminClient =
        BigtableTableAdminClient.create(projectId, instanceId)) {
      adminClient.deleteTable(TABLE_ID);
      String[] dockerCommand = (String.format("docker stop %s", MEMCACHED_CONTAINER_NAME))
          .split(" ");
      Process process = new ProcessBuilder(dockerCommand).start();
      process.waitFor();
    } catch (Exception e) {
      System.out.println("Error during afterClass: \n" + e.toString());
    }
  }

  @Test
  public void testMemcached() throws InterruptedException {
    // Run twice to fetch value from Bigtable and then from cache
    System.setProperty("bigtableProjectId", projectId);
    System.setProperty("bigtableInstanceId", instanceId);
    System.setProperty("bigtableTableId", TABLE_ID);
    System.setProperty("memcachedDiscoveryEndpoint", discoveryEndpoint);

    Memcached.main(null);
    sleep(1000);
    Memcached.main(null);

    String output = bout.toString();
    assertThat(output).contains("Value fetched from Bigtable: PQ2A.190405.003");

    // retry (due to occasional flakiness) if we didn't yet get the result in the cache
    int retryCount = 0;
    String foundInCache = "Value fetched from cache: PQ2A.190405.003";
    while (retryCount < 5 && !output.contains(foundInCache)) {
      Memcached.main(null);
      output = bout.toString();
      retryCount++;
    }
    assertThat(output).contains(foundInCache);
  }
}
