/*
 * Copyright 2015 Google Inc.
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

package com.example.storage;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for quickstart sample.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class QuickstartSampleIT {
  private String bucketName;
  private ByteArrayOutputStream bout;
  private PrintStream out;

  private static final void deleteBucket(String bucketName) {
    Storage storage = StorageOptions.getDefaultInstance().getService();
    storage.delete(bucketName);
  }

  @Before
  public void setUp() {
    bucketName = "my-new-bucket-" + UUID.randomUUID().toString();

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
    deleteBucket(bucketName);
  }

  @Test
  public void testQuickstart() throws Exception {
    QuickstartSample.main(bucketName);
    String got = bout.toString();
    assertThat(got).contains(String.format("Bucket %s created.", bucketName));
  }
}
