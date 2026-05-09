/*
 * Copyright 2026 Google LLC
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

package com.example.firestore;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.example.firestore.DeleteCollection;

public class DeleteCollectionIT {

  private static ByteArrayOutputStream bout;
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String COLLECTION_NAME = "usa-cities";
  private static final String DOCUMENT_NAME = "LA";
  private static final String NAME = "Los Angeles";

  private static String getEnvVar(String varName) {
    String value = System.getenv(varName);
    assertNotNull(
        String.format("Environment variable '%s' must be set to perform these tests.", varName),
        value);
    return value;
  }

  @BeforeAll
  public static void setUp() throws Exception {
    getEnvVar("GOOGLE_CLOUD_PROJECT");

    // Create collection
    Utils.createCollection(PROJECT_ID, COLLECTION_NAME, DOCUMENT_NAME, NAME);

    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @AfterAll
  public static void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testDeleteCollection() throws ExecutionException, InterruptedException {
    DeleteCollection.deleteCollection(PROJECT_ID, COLLECTION_NAME);

    String output = bout.toString();
    assertTrue(output.contains("Collection and all its subcollections deleted successfully."));
  }
}
