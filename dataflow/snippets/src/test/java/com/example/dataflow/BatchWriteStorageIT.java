/*
 * Copyright 2023 Google LLC
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

import static org.junit.Assert.assertNotNull;

import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.testing.RemoteStorageHelper;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BatchWriteStorageIT {

  private static final String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
  String bucketName;
  Storage storage;

  @Before
  public void setUp() {
    RemoteStorageHelper helper = RemoteStorageHelper.create();
    storage = helper.getOptions().getService();
    bucketName = RemoteStorageHelper.generateBucketName();
    storage.create(BucketInfo.of(bucketName));
  }

  @After
  public void tearDown() throws ExecutionException, InterruptedException {
    RemoteStorageHelper.forceDelete(storage, bucketName, 5, TimeUnit.SECONDS);
  }

  @Test
  public void batchWriteToStorage_shouldWriteObject() throws Exception {
    BatchWriteStorage.main(
        new String[] {
            "--runner=DirectRunner",
            "--bucketName=gs://" + bucketName
        });

    // Verify the pipeline wrote an object to the storage bucket.
    var blobs = storage.get(bucketName).list();
    var object = blobs.iterateAll().iterator().next();
    assertNotNull(object);
  }
}
