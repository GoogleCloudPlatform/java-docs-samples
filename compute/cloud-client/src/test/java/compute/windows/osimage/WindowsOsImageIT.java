// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package compute.windows.osimage;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static compute.Util.getZone;

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import compute.DeleteInstance;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Rule;
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
public class WindowsOsImageIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = getZone();
  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INITIAL_BACKOFF_MILLIS = 120000; // 2 minutes
  private static String testInstanceName;
  private static String testImageName;

  private static String getBootDiskName(String instanceName) {
    return instanceName + "-boot-disk";
  }

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(
      MAX_ATTEMPT_COUNT,
      INITIAL_BACKOFF_MILLIS);

  private static boolean createInstance(String instanceName)
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    final String MACHINE_TYPE = String.format("zones/%s/machineTypes/n1-standard-1", ZONE);
    final String MACHINE_FAMILY = "projects/debian-cloud/global/images/family/debian-11";
    final long DISK_SIZE = 10L;
    try (InstancesClient instancesClient = InstancesClient.create()) {
      AttachedDisk attachedDisk = AttachedDisk.newBuilder()
          .setDeviceName(getBootDiskName(instanceName))
          .setAutoDelete(true)
          .setBoot(true)
          .setType(AttachedDisk.Type.PERSISTENT.name())
          .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
              .setDiskName(getBootDiskName(instanceName))
              .setDiskSizeGb(DISK_SIZE)
              .setSourceImage(MACHINE_FAMILY)
              .build())
          .build();
      Instance instance = Instance.newBuilder()
          .setName(instanceName)
          .setMachineType(MACHINE_TYPE)
          .addDisks(attachedDisk)
          // mind that it will not work with custom VPC
          .addNetworkInterfaces(NetworkInterface.newBuilder()
              .setName("global/networks/default")
              .build())
          .build();
      InsertInstanceRequest request = InsertInstanceRequest.newBuilder()
          .setProject(PROJECT_ID)
          .setZone(ZONE)
          .setInstanceResource(instance)
          .build();
      Operation response = instancesClient.insertAsync(request).get(5, TimeUnit.MINUTES);
      return !response.hasError();
    }
  }

  /**
   * Assert that environment has a variable set.
   *
   * @param envVarName the name of the required environment variable
   *
   */
  private static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    String randomUUID = UUID.randomUUID().toString().split("-")[0];
    testInstanceName = "images-test-help-instance-" + randomUUID;
    testImageName = "test-image-" + randomUUID;

    // Create a VM with a smallest possible disk that can be used for testing
    Assert.assertTrue("Failed to setup instance for image create/delete testing",
        createInstance(testInstanceName));
  }

  @AfterAll
  public static void cleanUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    // mind that we use *another* code sample
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, testInstanceName);
  }

  private ByteArrayOutputStream stdOut;

  @BeforeEach
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @AfterEach
  public void afterEach() {
    System.setOut(System.out);
  }

  @Test
  public void testCanCreateImage()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    CreateImage.createImage(
        PROJECT_ID, ZONE, getBootDiskName(testInstanceName), testImageName, "us", true);
    assertThat(stdOut.toString()).contains("Image created.");
    DeleteImage.deleteImage(PROJECT_ID, testImageName);
    assertThat(stdOut.toString()).contains("Operation Status for Image Name");
  }

  @Test
  public void testUnforcedCreateImage()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Assertions.assertThrows(
        IllegalStateException.class,
        () -> CreateImage.createImage(
            PROJECT_ID, ZONE, getBootDiskName(testInstanceName), testImageName, "us", false),
        String.format("Instance %s should be stopped.", testInstanceName));
  }

}