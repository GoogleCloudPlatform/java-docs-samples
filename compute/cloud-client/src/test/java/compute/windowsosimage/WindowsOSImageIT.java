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

package compute.windowsosimage;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.compute.v1.AttachedDisk;
import com.google.cloud.compute.v1.AttachedDiskInitializeParams;
import com.google.cloud.compute.v1.ImagesClient;
import com.google.cloud.compute.v1.InsertInstanceRequest;
import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.NetworkInterface;
import com.google.cloud.compute.v1.Operation;
import compute.DeleteInstance;
import compute.Util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class WindowsOSImageIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-a";
  private static String INSTANCE_NAME;
  private static String DISK_NAME;
  private static String IMAGE_NAME;
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeClass
  public static void setup() throws IOException, ExecutionException, InterruptedException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    // Cleanup existing test instances.
    Util.cleanUpExistingInstances("windowsimage-test-instance-", PROJECT_ID, ZONE);

    String randomUUID = UUID.randomUUID().toString().split("-")[0];
    INSTANCE_NAME = "windowsimage-test-instance-" + randomUUID;
    DISK_NAME = "windowsimage-test-disk-" + randomUUID;
    IMAGE_NAME = "windowsimage-test-image-" + randomUUID;

    // Create Instance with Windows source image.
    try (InstancesClient instancesClient = InstancesClient.create()) {
      AttachedDisk attachedDisk = AttachedDisk.newBuilder()
          .setDeviceName(DISK_NAME)
          .setAutoDelete(true)
          .setBoot(true)
          .setType(AttachedDisk.Type.PERSISTENT.name())
          .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
              .setDiskName(DISK_NAME)
              .setDiskSizeGb(64)
              .setSourceImage(
                  "projects/windows-cloud/global/images/windows-server-2012-r2-dc-core-v20220314")
              .build())
          .build();

      Instance instance = Instance.newBuilder()
          .setName(INSTANCE_NAME)
          .setMachineType(String.format("zones/%s/machineTypes/n1-standard-1", ZONE))
          .addNetworkInterfaces(NetworkInterface.newBuilder()
              .setName("global/networks/default")
              .build())
          .addDisks(attachedDisk)
          .build();

      InsertInstanceRequest request = InsertInstanceRequest.newBuilder()
          .setProject(PROJECT_ID)
          .setZone(ZONE)
          .setInstanceResource(instance)
          .build();

      Operation response = instancesClient.insertAsync(request).get();
      Assert.assertFalse(response.hasError());
    }

    stdOut.close();
    System.setOut(out);
  }

  @AfterClass
  public static void cleanUp() throws IOException, ExecutionException, InterruptedException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // Delete image.
    ImagesClient imagesClient = ImagesClient.create();
    Operation operation = imagesClient.deleteAsync(PROJECT_ID, IMAGE_NAME).get();
    if (operation.hasError()) {
      System.out.println("Image not deleted.");
    }

    // Delete instance.
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_NAME);

    stdOut.close();
    System.setOut(out);
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @After
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }


  @Test
  public void testCreateWindowsImage_failDueToRunningInstance()
      throws IOException, ExecutionException, InterruptedException {
    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage(String.format("Instance %s should be stopped.", INSTANCE_NAME));
    CreateWindowsOSImage.createWindowsOSImage(
        PROJECT_ID, ZONE, DISK_NAME, IMAGE_NAME, "eu", false);
  }


  @Test
  public void testCreateWindowsImage_pass()
      throws IOException, ExecutionException, InterruptedException {
    CreateWindowsOSImage.createWindowsOSImage(
        PROJECT_ID, ZONE, DISK_NAME, IMAGE_NAME, "eu", true);
    assertThat(stdOut.toString()).contains("Image created.");
  }

}
