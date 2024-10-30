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

package com.example.dataflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.testing.RemoteStorageHelper;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.beam.sdk.PipelineResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ReadFromStorageIT {

  private static final String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");

  private ByteArrayOutputStream bout;
  private final PrintStream originalout = System.out;

  String bucketName;
  Storage storage;

  private static final String[] lines = {"line 1", "line 2"};

  @Before
  public void setUp() {
    // Redirect System.err to capture logs.
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    // Create a Cloud Storage bucket with a text file.
    RemoteStorageHelper helper = RemoteStorageHelper.create();
    storage = helper.getOptions().getService();
    bucketName = RemoteStorageHelper.generateBucketName();
    storage.create(BucketInfo.of(bucketName));

    String objectName = "file1.txt";
    String contents = String.format("%s\n%s\n", lines[0], lines[1]);

    BlobId blobId = BlobId.of(bucketName, objectName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    byte[] content = contents.getBytes(StandardCharsets.UTF_8);

    storage.create(blobInfo, content);
  }

  @After
  public void tearDown() throws ExecutionException, InterruptedException {
    RemoteStorageHelper.forceDelete(storage, bucketName, 5, TimeUnit.SECONDS);

    System.setOut(originalout);
    bout.reset();
  }

  @Test
  public void readFromStorage_shouldReadFile() throws Exception {

    PipelineResult.State state = ReadFromStorage.main(
        new String[] {"--runner=DirectRunner", "--bucket=" + bucketName});
    assertEquals(PipelineResult.State.DONE, state);

    String got = bout.toString();
    assertTrue(got.contains(lines[0]));
    assertTrue(got.contains(lines[1]));
  }
}
