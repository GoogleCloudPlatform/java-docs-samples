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

package genai.videogeneration;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class VideoGenerationIT {

  private static final String VIDEO_GEN_MODEL = "veo-3.0-generate-001";
  private static final String VIDEO_GEN_PREVIEW_MODEL = "veo-3.0-generate-preview";
  private static final String BUCKET_NAME = "java-docs-samples-testing";
  private static final String PREFIX = "genai-video-generation-" + UUID.randomUUID();
  private static final String OUTPUT_GCS_URI = String.format("gs://%s/%s", BUCKET_NAME, PREFIX);

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

  @Test
  public void testVideoGenWithImg() throws InterruptedException {
    String response = VideoGenWithImg.generateContent(VIDEO_GEN_PREVIEW_MODEL, OUTPUT_GCS_URI);
    assertThat(response).isNotEmpty();
  }

  @Test
  public void testVideoGenWithTxt() throws InterruptedException {
    String response = VideoGenWithTxt.generateContent(VIDEO_GEN_MODEL, OUTPUT_GCS_URI);
    assertThat(response).isNotEmpty();
  }
}
