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

package compute.disks;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 3, unit = TimeUnit.MINUTES)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DiskReplicationIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String PRIMARY_REGION = "us-central1";
  private static final String SECONDARY_REGION = "us-east1";
  private static final String DISK_TYPE = String.format(
      "projects/%s/regions/%s/diskTypes/pd-balanced", PROJECT_ID, SECONDARY_REGION);
  private static final List<String> replicaZones = Arrays.asList(
      String.format("projects/%s/zones/%s-c", PROJECT_ID, PRIMARY_REGION),
      String.format("projects/%s/zones/%s-b", PROJECT_ID, PRIMARY_REGION));
  static String templateUUID = UUID.randomUUID().toString().substring(0, 8);
  private static final String PRIMARY_DISK_NAME = "test-disk-primary-" + templateUUID;
  private static final String SECONDARY_DISK_NAME = "test-disk-secondary-" + templateUUID;
  private static ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    RegionalCreateFromSource.createRegionalDisk(PROJECT_ID, PRIMARY_REGION, replicaZones,
        PRIMARY_DISK_NAME, DISK_TYPE, 10,
        Optional.empty(), Optional.empty());
    CreateDiskSecondaryRegional.createDiskSecondaryRegional(
        PROJECT_ID, PROJECT_ID, PRIMARY_DISK_NAME, SECONDARY_DISK_NAME,
        PRIMARY_REGION, SECONDARY_REGION, 10L, DISK_TYPE);
  }

  @BeforeEach
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @AfterEach
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }

  @Test
  @Order(1)
  public void testStartDiskAsyncReplication()
      throws IOException, ExecutionException, InterruptedException {
    String secondaryDiskPath = String.format("projects/%s/regions/%s/disks/%s",
        PROJECT_ID, SECONDARY_REGION, SECONDARY_DISK_NAME);
    StartDiskReplication.startDiskAsyncReplication(PROJECT_ID,  PRIMARY_DISK_NAME,
        PRIMARY_REGION, secondaryDiskPath);

    assertThat(stdOut.toString().contains("Async replication started successfully."));
  }

  @Test
  @Order(2)
  public void testStopPrimaryDiskAsyncReplication()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    StopDiskReplication.stopDiskAsyncReplication(PROJECT_ID, PRIMARY_REGION, PRIMARY_DISK_NAME);

    assertThat(stdOut.toString().contains("Async replication stopped successfully."));

    RegionalDelete.deleteRegionalDisk(PROJECT_ID, PRIMARY_REGION, PRIMARY_DISK_NAME);
  }

  @Test
  @Order(2)
  public void testStopSecondaryDiskAsyncReplication()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    StopDiskReplication.stopDiskAsyncReplication(PROJECT_ID, SECONDARY_REGION, SECONDARY_DISK_NAME);

    assertThat(stdOut.toString().contains("Async replication stopped successfully."));

    RegionalDelete.deleteRegionalDisk(PROJECT_ID, SECONDARY_REGION, SECONDARY_DISK_NAME);
  }
}