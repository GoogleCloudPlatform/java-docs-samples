/*
 * Copyright 2022 Google LLC
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

package compute.custommachinetype;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.compute.v1.Instance;
import compute.DeleteInstance;
import compute.Util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
public class CustomMachineTypeIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "europe-central2-b";
  private static final String CUSTOM_MACHINE_TYPE = String.format(
      "zones/%s/machineTypes/n2-custom-8-10240", ZONE);

  private static String CUSTOM_MACHINE_TYPE_INSTANCE;
  private static String CUSTOM_MACHINE_TYPE_INSTANCE_WITH_HELPER;
  private static String CUSTOM_MACHINE_TYPE_INSTANCE_WITH_SHARED_CORE;
  private static String CUSTOM_MACHINE_TYPE_INSTANCE_WITHOUT_HELPER;
  private static String EXTRA_MEM_INSTANCE_WITHOUT_HELPER;
  private static String CUSTOM_MACHINE_TYPE_INSTANCE_EXT_MEMORY;

  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    // Cleanup existing stale resources.
    Util.cleanUpExistingInstances("cmt-test-", PROJECT_ID, ZONE);

    String randomUUID = UUID.randomUUID().toString().split("-")[0];
    CUSTOM_MACHINE_TYPE_INSTANCE = "cmt-test" + randomUUID;
    CUSTOM_MACHINE_TYPE_INSTANCE_WITH_HELPER = "cmt-test-with-helper" + randomUUID;
    CUSTOM_MACHINE_TYPE_INSTANCE_WITH_SHARED_CORE = "cmt-test-shared-core" + randomUUID;
    CUSTOM_MACHINE_TYPE_INSTANCE_WITHOUT_HELPER = "cmt-test-without-helper" + randomUUID;
    EXTRA_MEM_INSTANCE_WITHOUT_HELPER = "cmt-test-extra-mem-without-helper" + randomUUID;
    CUSTOM_MACHINE_TYPE_INSTANCE_EXT_MEMORY = "cmt-test-ext-mem" + randomUUID;

    stdOut.close();
    System.setOut(out);
  }


  @AfterAll
  public static void cleanup()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, CUSTOM_MACHINE_TYPE_INSTANCE);
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, CUSTOM_MACHINE_TYPE_INSTANCE_WITH_HELPER);
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, CUSTOM_MACHINE_TYPE_INSTANCE_WITH_SHARED_CORE);
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, CUSTOM_MACHINE_TYPE_INSTANCE_WITHOUT_HELPER);
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, EXTRA_MEM_INSTANCE_WITHOUT_HELPER);
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, CUSTOM_MACHINE_TYPE_INSTANCE_EXT_MEMORY);

    stdOut.close();
    System.setOut(out);
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
  public void testCreateInstanceWithCustomMachineType()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Instance instance = CreateCustomMachineType.createInstanceWithCustomMachineType(PROJECT_ID,
        ZONE, CUSTOM_MACHINE_TYPE_INSTANCE, CUSTOM_MACHINE_TYPE);
    assertThat(stdOut.toString()).contains("Instance created");
    Assertions.assertTrue(instance.getMachineType().endsWith(CUSTOM_MACHINE_TYPE));
  }

  @Test
  public void testCreateInstanceWithCustomMachineTypeWithHelper()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Custom instance creation with helper.
    Instance instance = CreateWithHelper.createInstanceWithCustomMachineTypeWithHelper(PROJECT_ID,
        ZONE, CUSTOM_MACHINE_TYPE_INSTANCE_WITH_HELPER,
        CreateWithHelper.CpuSeries.E2.getCpuSeries(), 4,
        8192);
    assertThat(stdOut.toString()).contains("Instance created");
    assertThat(instance.getName()).contains(CUSTOM_MACHINE_TYPE_INSTANCE_WITH_HELPER);
    Assertions.assertTrue(instance.getMachineType()
        .endsWith(String.format("zones/%s/machineTypes/e2-custom-4-8192", ZONE)));
  }

  @Test
  public void testCreateInstanceWithCustomSharedCore()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Instance instance = CreateInstanceWithCustomSharedCore.createInstanceWithCustomSharedCore(
        PROJECT_ID, ZONE, CUSTOM_MACHINE_TYPE_INSTANCE_WITH_SHARED_CORE,
        CreateInstanceWithCustomSharedCore.CpuSeries.E2_MICRO.getCpuSeries(), 2048);
    assertThat(stdOut.toString()).contains("Instance created");
    assertThat(instance.getName()).contains(CUSTOM_MACHINE_TYPE_INSTANCE_WITH_SHARED_CORE);
    Assertions.assertTrue(instance.getMachineType()
        .endsWith(String.format("zones/%s/machineTypes/e2-custom-micro-2048", ZONE)));
  }

  @Test
  public void testAddExtendedMemoryToInstance()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateCustomMachineType.createInstanceWithCustomMachineType(PROJECT_ID, ZONE,
        CUSTOM_MACHINE_TYPE_INSTANCE_EXT_MEMORY, CUSTOM_MACHINE_TYPE);
    assertThat(stdOut.toString()).contains("Instance created");
    Instance instance = UpdateMemory.modifyInstanceWithExtendedMemory(PROJECT_ID, ZONE,
        CUSTOM_MACHINE_TYPE_INSTANCE_EXT_MEMORY, 819200);
    assertThat(stdOut.toString()).contains("Instance updated!");
    Assertions.assertTrue(instance.getMachineType().endsWith("819200-ext"));
  }

  @Test
  public void testCreateInstanceWithCustomMachineTypeWithoutHelper()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Instance instance = CreateWithoutHelper.createInstanceWithCustomMachineTypeWithoutHelper(
        PROJECT_ID, ZONE, CUSTOM_MACHINE_TYPE_INSTANCE_WITHOUT_HELPER, "e2-custom", 4, 8192);
    assertThat(stdOut.toString()).contains("Instance created");
    Assertions.assertTrue(instance.getMachineType()
        .endsWith(String.format("zones/%s/machineTypes/e2-custom-4-8192", ZONE)));
  }

  @Test
  public void testCreateInstanceWithExtraMemWithoutHelper()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Instance instance = ExtraMemoryWithoutHelper.createInstanceWithExtraMemoryWithoutHelper(
        PROJECT_ID, ZONE, EXTRA_MEM_INSTANCE_WITHOUT_HELPER, "custom", 4, 24320);
    assertThat(stdOut.toString()).contains("Instance created");
    Assertions.assertTrue(instance.getMachineType()
        .endsWith(String.format("zones/%s/machineTypes/custom-4-24320-ext", ZONE)));
  }
}
