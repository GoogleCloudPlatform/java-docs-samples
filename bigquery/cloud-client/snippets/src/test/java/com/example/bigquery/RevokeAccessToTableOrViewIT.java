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

import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.google.cloud.bigquery.testing.RemoteBigQueryHelper;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RevokeAccessToTableOrViewIT {

  private final Logger log = Logger.getLogger(this.getClass().getName());
  private String datasetName;
  private String tableName;
  private String viewName;
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;

  private static final String GOOGLE_CLOUD_PROJECT = System.getenv("GOOGLE_CLOUD_PROJECT");

  private static String requireEnvVar(String varName) {
    String value = System.getenv(varName);
    assertNotNull(
        "Environment variable " + varName + " is required to perform these tests.",
        System.getenv(varName));
    return value;
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);

    // Create temporary dataset
    datasetName = RemoteBigQueryHelper.generateDatasetName();
    CreateDataset.createDataset(GOOGLE_CLOUD_PROJECT, datasetName);

    // Create temporary table and grant access to it
    tableName = "CREATE_POLICY_TABLE_TEST_" + UUID.randomUUID().toString().substring(0, 8);
    Schema schema =
        Schema.of(
            Field.of("stringField", StandardSQLTypeName.STRING),
            Field.of("isBooleanField", StandardSQLTypeName.BOOL));
    CreateTable.createTable(GOOGLE_CLOUD_PROJECT, datasetName, tableName, schema);
    GrantAccessToTableOrView.grantAccessToTableOrView(GOOGLE_CLOUD_PROJECT, datasetName, tableName);

    // Create a temporary view and grant access to it
    viewName = "CREATE_POLICY_VIEW_TEST_" + UUID.randomUUID().toString().substring(0, 8);
    String query =
        String.format("SELECT stringField, isBooleanField FROM %s.%s", datasetName, tableName);
    CreateView.createView(GOOGLE_CLOUD_PROJECT, datasetName, viewName, query);
    GrantAccessToTableOrView.grantAccessToTableOrView(GOOGLE_CLOUD_PROJECT, datasetName, viewName);
  }

  @After
  public void tearDown() {
    // Clean up
    DeleteTable.deleteTable(GOOGLE_CLOUD_PROJECT, datasetName, tableName);
    DeleteDataset.deleteDataset(GOOGLE_CLOUD_PROJECT, datasetName);

    // Restores print statements to the original output stream
    System.out.flush();
    System.setOut(originalPrintStream);
    log.log(Level.INFO, bout.toString());
  }

  @Test
  public void testRevokeAccessToTableOrView_revokeAccessToTable() {
    RevokeAccessToTableOrView.revokeAccessToTableOrView(
        GOOGLE_CLOUD_PROJECT, datasetName, tableName);
    assertThat(bout.toString())
        .contains("IAM policy of resource " + tableName + " updated successfully");
  }

  @Test
  public void testRevokeAccessToTableOrView_revokeAccessToView() {
    RevokeAccessToTableOrView.revokeAccessToTableOrView(
        GOOGLE_CLOUD_PROJECT, datasetName, viewName);
    assertThat(bout.toString())
        .contains("IAM policy of resource " + viewName + " updated successfully");
  }
}
