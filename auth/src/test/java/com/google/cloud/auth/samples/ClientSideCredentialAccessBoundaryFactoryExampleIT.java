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

package com.google.cloud.auth.samples;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.cloud

@RunWith(JUnit4.class)
// CHECKSTYLE OFF: AbbreviationAsWordInName
public class ClientSideCredentialAccessBoundaryFactoryExampleIT {
  // CHECKSTYLE ON: AbbreviationAsWordInName
  private static final String CONTENT = "CONTENT";
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private String credentials;
  private Bucket bucket;
  private Blob blob;
  private String[] args;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    credentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
    assertNotNull(credentials);

    // Create a bucket and object that are deleted once the test completes.
    Storage storage = StorageOptions.newBuilder().build().getService();

    String bucketName = String.format("bucket-client-side-cab-test-%s", UUID.randomUUID());
    Bucket bucket = storage.create(BucketInfo.newBuilder(bucketName).build());

    String objectName = String.format("blob-client-side-cab-test-%s", UUID.randomUUID());
    BlobId blobId = BlobId.of(bucketName, objectName);
    BlobInfo blobInfo = Blob.newBuilder(blobId).build();
    Blob blob = storage.create(blobInfo, CONTENT.getBytes(StandardCharsets.UTF_8));

    this.bucket = bucket;
    this.blob = blob;
    this.args = new String[] {bucketName, objectName};
  }

  @After
  public void cleanup() {
    blob.delete();
    bucket.delete();
  }

  @Test
  public void testClientSideCredentialAccessBoundaryFactory() throws IOException {
    ClientSideCredentialAccessBoundaryFactoryExample.tokenConsumer(bucket.getName(), blob.getName());
    String expectedOutput =
        "Retrieved object, "
            + blob.getName()
            + ", from bucket,"
            + bucket.getName()
            + ", with content: "
            + CONTENT;
    String output = bout.toString();
    assertTrue(output.contains(expectedOutput));
  }
}
