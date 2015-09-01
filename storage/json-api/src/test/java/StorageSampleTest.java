/**
 * Copyright 2015 Google Inc. All Rights Reserved.
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

// [START all]

import static org.junit.Assert.*;

import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;

import org.junit.Test;

import java.util.List;
import java.util.regex.Pattern;
import java.io.ByteArrayInputStream;

public class StorageSampleTest {
  private static final String BUCKET = "cloud-samples-tests";
  private static final String TEST_OBJECT = "storage-sample-test-upload.txt";

  @Test
  public void testListBucket() throws Exception {
    List<StorageObject> listing = StorageSample.listBucket(BUCKET);
    assertTrue(listing.size() > 0);
  }

  @Test
  public void testGetBucket() throws Exception {
    Bucket bucket = StorageSample.getBucket(BUCKET);
    assertEquals(bucket.getName(), BUCKET);
    assertEquals(bucket.getLocation(), "US-CENTRAL1");
  }

  @Test
  public void testUploadDelete() throws Exception {
    StorageSample.uploadStream(
        TEST_OBJECT, "text/plain",
        new ByteArrayInputStream(("This object is uploaded and deleted as part of the "
            + "StorageSampleTest integration test.").getBytes()),
        BUCKET);

    try {
      // Verify that the object was created
      List<StorageObject> listing = StorageSample.listBucket(BUCKET);
      boolean found = false;
      for (StorageObject so : listing) {
        if (TEST_OBJECT.equals(so.getName())) {
          found = true;
          break;
        }
      }
      assertTrue("Should have uploaded successfully", found);

    } finally {
      StorageSample.deleteObject(TEST_OBJECT, BUCKET);

      // Verify that the object no longer exists
      List<StorageObject> listing = StorageSample.listBucket(BUCKET);
      boolean found = false;
      for (StorageObject so : listing) {
        if (TEST_OBJECT.equals(so.getName())) {
          found = true;
          break;
        }
      }
      assertFalse("Object (" + TEST_OBJECT + ") should have been deleted", found);
    }
  }
}
// [END all]
