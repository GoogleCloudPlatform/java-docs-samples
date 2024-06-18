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

package compute.images;

import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.compute.v1.DeprecationStatus;
import com.google.cloud.compute.v1.GuestOsFeature;
import com.google.cloud.compute.v1.Image;
import com.google.cloud.compute.v1.ImagesClient;
import compute.disks.CreateDiskFromImage;
import compute.disks.CreateSnapshot;
import compute.disks.DeleteDisk;
import compute.disks.DeleteSnapshot;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ImagesIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String IMAGE_FROM_IMAGE_NAME;
  private static String IMAGE_FROM_SNAPSHOT_NAME;
  private static String DISK_NAME;
  private static String SNAPSHOT_NAME;
  private static final String ZONE = "europe-west2-c";

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
            .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeClass
  public static void setUp()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    IMAGE_FROM_IMAGE_NAME = "image-name-" + UUID.randomUUID().toString().substring(0, 8);
    IMAGE_FROM_SNAPSHOT_NAME = "image-name-" + UUID.randomUUID().toString().substring(0, 8);
    DISK_NAME = "test-disk-" + UUID.randomUUID().toString().substring(0, 8);
    SNAPSHOT_NAME = "test-snapshot-" + UUID.randomUUID().toString().substring(0, 8);

    Image imageFromFamily = GetImageFromFamily.getImageFromFamily("debian-cloud", "debian-11");
    CreateDiskFromImage.createDiskFromImage(PROJECT_ID, ZONE, DISK_NAME,
            String.format("zones/%s/diskTypes/pd-standard", ZONE), 20,
            imageFromFamily.getSelfLink());
    CreateSnapshot.createSnapshot(PROJECT_ID, DISK_NAME, SNAPSHOT_NAME, ZONE, "", "", "");
  }

  @AfterClass
  public static void cleanUp()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (ImagesClient client = ImagesClient.create()) {
      client.deleteAsync(PROJECT_ID, IMAGE_FROM_IMAGE_NAME).get(60, TimeUnit.SECONDS);
      client.deleteAsync(PROJECT_ID, IMAGE_FROM_SNAPSHOT_NAME).get(60, TimeUnit.SECONDS);
    }
    DeleteDisk.deleteDisk(PROJECT_ID, ZONE, DISK_NAME);
    DeleteSnapshot.deleteSnapshot(PROJECT_ID, SNAPSHOT_NAME);
  }

  @Test
  public void stage1_createImageFromImageTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Image sourceImage = GetImageFromFamily.getImageFromFamily("ubuntu-os-cloud", "ubuntu-2204-lts");
    Image image = CreateImageFromImage.createImageFromImage(PROJECT_ID, sourceImage.getName(),
            IMAGE_FROM_IMAGE_NAME, "ubuntu-os-cloud",
            Collections.singletonList(GuestOsFeature.Type.MULTI_IP_SUBNET.name()), "eu");

    Assert.assertNotNull(image);
    Assert.assertEquals(sourceImage.getDiskSizeGb(), image.getDiskSizeGb());
    Assert.assertEquals(image.getName(), IMAGE_FROM_IMAGE_NAME);
    Assert.assertTrue(image.getGuestOsFeaturesList().stream()
            .anyMatch(guestOsFeature
                    -> guestOsFeature.getType().equals(GuestOsFeature.Type.MULTI_IP_SUBNET.name())
            )
    );
  }

  @Test
  public void stage2_createImageFromSnapshotTest()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Image image = CreateImageFromSnapshot.createImageFromSnapshot(PROJECT_ID, SNAPSHOT_NAME,
            IMAGE_FROM_SNAPSHOT_NAME, PROJECT_ID,
            Collections.singletonList(GuestOsFeature.Type.MULTI_IP_SUBNET.name()), "us-central1");

    Assert.assertNotNull(image);
    Assert.assertEquals(20, image.getDiskSizeGb());
    Assert.assertEquals(image.getName(), IMAGE_FROM_SNAPSHOT_NAME);
  }

  @Test
  public void stage3_getImageTest() throws IOException {
    Image image = GetImage.getImage(PROJECT_ID, IMAGE_FROM_IMAGE_NAME);
    Assert.assertNotNull(image);
    Assert.assertEquals(image.getName(), IMAGE_FROM_IMAGE_NAME);
    Assert.assertTrue(image.getGuestOsFeaturesList().stream()
            .anyMatch(guestOsFeature
                    -> guestOsFeature.getType().equals(GuestOsFeature.Type.MULTI_IP_SUBNET.name())
            )
    );
  }

  @Test
  public void stage3_getImageFromFamilyTest() throws IOException {
    Image image = GetImageFromFamily.getImageFromFamily("ubuntu-os-cloud", "ubuntu-2204-lts");
    Assert.assertNotNull(image);
    Assert.assertEquals(image.getFamily(), "ubuntu-2204-lts");
  }

  @Test
  public void stage3_listImagesTest() throws IOException {
    List<Image> images = ListImages.listImages(PROJECT_ID);
    Assert.assertNotNull(images);
    Assert.assertTrue(images.size() > 1);
    Assert.assertTrue(images.stream().anyMatch(image
            -> image.getName().equals(IMAGE_FROM_IMAGE_NAME)));
  }

  @Test
  public void stage4_setImageDeprecationStatus()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    TimeUnit.SECONDS.sleep(100);
    Image image = SetImageDeprecationStatus.setDeprecationStatus(PROJECT_ID,
            IMAGE_FROM_IMAGE_NAME, DeprecationStatus.State.DEPRECATED);
    Assert.assertNotNull(image);
    String name = DeprecationStatus.State.DEPRECATED.name();
    Assert.assertEquals(name, image.getDeprecated().getState());
  }
}
