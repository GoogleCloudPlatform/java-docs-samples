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

package com.example.bigquery;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.cloud.bigquery.testing.RemoteBigQueryHelper;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RevokeDatasetAccessIT {

  private final Logger log = Logger.getLogger(this.getClass().getName());
  private String datasetName;
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;

  private static final String GOOGLE_CLOUD_PROJECT = System.getenv("GOOGLE_CLOUD_PROJECT");

  private static void requireEnvVar(String varName) {
    assertNotNull(
        "Environment variable " + varName + " is required to perform these tests.",
        System.getenv(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void setUp() throws Exception {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
    datasetName = RemoteBigQueryHelper.generateDatasetName();

    // Create a dataset.
    Util.setUpTest_createDataset(GOOGLE_CLOUD_PROJECT, datasetName);
    String groupEmail = "cloud-developer-relations@google.com";

    // Add new ACL entry in order to remove it.
    Util.setUpTest_grantAccessToDataset(GOOGLE_CLOUD_PROJECT, datasetName, groupEmail);
  }

  @After
  public void tearDown() {
    // Clean up.
    Util.tearDownTest_deleteDataset(GOOGLE_CLOUD_PROJECT, datasetName);

    // Restores print statements to the original output stream.
    System.out.flush();
    System.setOut(originalPrintStream);
    log.log(Level.INFO, "\n" + bout.toString());
  }

  @Test
  public void revokeDatasetAccess() {
    String groupEmail = "cloud-developer-relations@google.com";
    RevokeDatasetAccess.revokeDatasetAccess(GOOGLE_CLOUD_PROJECT, datasetName, groupEmail);
    assertThat(bout.toString()).contains("ACLs of \"" + datasetName + "\" updated successfully");
  }
}
