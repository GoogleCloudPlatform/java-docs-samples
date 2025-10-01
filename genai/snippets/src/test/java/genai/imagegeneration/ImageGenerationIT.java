/*
 * Copyright 2025 Google LLC
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

package genai.imagegeneration;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.genai.types.Image;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ImageGenerationIT {

  private static final String IMAGEN_3_MODEL = "imagen-3.0-capability-001";
  private static final String BUCKET_NAME = "java-docs-samples-testing";
  private static final String PREFIX = "genai-img-generation-" + UUID.randomUUID();
  private static final String OUTPUT_GCS_URI = String.format("gs://%s/%s", BUCKET_NAME, PREFIX);
  private static final String IMAGEN_4_MODEL = "imagen-4.0-generate-001";
  private static final String VIRTUAL_TRY_ON_MODEL = "virtual-try-on-preview-08-04";

  private ByteArrayOutputStream bout;
  private PrintStream out;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @AfterClass
  public static void cleanup() {
    Storage storage = StorageOptions.getDefaultInstance().getService();
    Page<Blob> blobs = storage.list(BUCKET_NAME, Storage.BlobListOption.prefix(PREFIX));

    for (Blob blob : blobs.iterateAll()) {
      storage.delete(blob.getBlobId());
    }
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testImageGenCannyCtrlTypeWithTextAndImage() {
    Optional<String> response =
            ImageGenCannyCtrlTypeWithTextAndImage.cannyEdgeCustomization(
                    IMAGEN_3_MODEL, OUTPUT_GCS_URI);
    assertThat(response).isPresent();
    assertThat(response.get()).isNotEmpty();
  }

  @Test
  public void testImageGenRawReferenceWithTextAndImage() {
    Optional<String> response =
            ImageGenRawReferenceWithTextAndImage.styleTransferCustomization(
                    IMAGEN_3_MODEL, OUTPUT_GCS_URI);
    assertThat(response).isPresent();
    assertThat(response.get()).isNotEmpty();
  }

  @Test
  public void testImageGenScribbleCtrlTypeWithTextAndImage() {
    Optional<String> response =
            ImageGenScribbleCtrlTypeWithTextAndImage.scribbleCustomization(
                    IMAGEN_3_MODEL, OUTPUT_GCS_URI);
    assertThat(response).isPresent();
    assertThat(response.get()).isNotEmpty();
  }

  @Test
  public void testImageGenStyleReferenceWithTextAndImage() {
    Optional<String> response =
            ImageGenStyleReferenceWithTextAndImage.styleCustomization(
                    IMAGEN_3_MODEL, OUTPUT_GCS_URI);
    assertThat(response).isPresent();
    assertThat(response.get()).isNotEmpty();
  }

  @Test
  public void testImageGenSubjectReferenceWithTextAndImage() {
    Optional<String> response =
            ImageGenSubjectReferenceWithTextAndImage.subjectCustomization(
                    IMAGEN_3_MODEL, OUTPUT_GCS_URI);
    assertThat(response).isPresent();
    assertThat(response.get()).isNotEmpty();
  }

  @Test
  public void testImageGenVirtualTryOnWithTextAndImage() throws IOException {
    Image image =
            ImageGenVirtualTryOnWithTextAndImage.generateContent(
                    VIRTUAL_TRY_ON_MODEL, "resources/output/man_in_sweater.png");

    assertThat(image).isNotNull();
    assertThat(image.imageBytes()).isPresent();
    assertThat(image.imageBytes().get().length).isGreaterThan(0);
  }

  @Test
  public void testImageGenWithText() throws IOException {
    Image image =
            ImageGenWithText.generateImage(IMAGEN_4_MODEL, "resources/output/dog_newspaper.png");

    assertThat(image).isNotNull();
    assertThat(image.imageBytes()).isPresent();
    assertThat(image.imageBytes().get().length).isGreaterThan(0);
  }

}