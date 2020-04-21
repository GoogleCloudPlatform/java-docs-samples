/*
 * Copyright 2018 Google LLC
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

package com.example.asset;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageOptions;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for quickstart sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class QuickStartIT {
  private static final String bucketName = "java-docs-samples-testing";
  private static final String path = UUID.randomUUID().toString();
  private ByteArrayOutputStream bout;
  private PrintStream out;

  private static final void deleteObjects() {
    Storage storage = StorageOptions.getDefaultInstance().getService();
    for (BlobInfo info :
        storage
            .list(
                bucketName,
                BlobListOption.versions(true),
                BlobListOption.currentDirectory(),
                BlobListOption.prefix(path + "/"))
            .getValues()) {
      storage.delete(info.getBlobId());
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
    String consoleOutput = bout.toString();
    System.setOut(null);
    deleteObjects();
  }

  @Test
  public void testExportAssetExample() throws Exception {
    String assetDumpPath = String.format("gs://%s/%s/my-assets-dump.txt", bucketName, path);
    ExportAssetsExample.main(assetDumpPath);
    String got = bout.toString();
    assertThat(got).contains(String.format("uri: \"%s\"", assetDumpPath));
  }

  @Test
  public void testBatchGetAssetsHistory() throws Exception {
    String bucketAssetName = String.format("//storage.googleapis.com/%s", bucketName);
    BatchGetAssetsHistoryExample.main(bucketAssetName);
    String got = bout.toString();
    if (!got.isEmpty()) {
      assertThat(got).contains(bucketAssetName);
    }
  }
}
