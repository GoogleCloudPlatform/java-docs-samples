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

package com.example.bigquerydatatransfer;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.protobuf.Value;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CreateAzureBlobStorageTransferIT {

  private static final Logger LOG =
      Logger.getLogger(CreateAzureBlobStorageTransferIT.class.getName());
  private static final String ID = UUID.randomUUID().toString().substring(0, 8);
  private BigQuery bigquery;
  private String name;
  private String displayName;
  private String datasetName;
  private String tableName;
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;

  private static final String PROJECT_ID = requireEnvVar("GOOGLE_CLOUD_PROJECT");
  private static final String DTS_AZURE_STORAGE_ACCOUNT =
      requireEnvVar("DTS_AZURE_STORAGE_ACCOUNT");
  private static final String DTS_AZURE_BLOB_CONTAINER = requireEnvVar("DTS_AZURE_BLOB_CONTAINER");
  private static final String DTS_AZURE_SAS_TOKEN = requireEnvVar("DTS_AZURE_SAS_TOKEN");

  private static String requireEnvVar(String varName) {
    String value = System.getenv(varName);
    assertWithMessage("Environment variable %s is required to perform these tests.", varName)
        .that(value)
        .isNotEmpty();
    return value;
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
    requireEnvVar("DTS_AZURE_STORAGE_ACCOUNT");
    requireEnvVar("DTS_AZURE_BLOB_CONTAINER");
    requireEnvVar("DTS_AZURE_SAS_TOKEN");
  }

  @Before
  public void setUp() {
    displayName = "MY_TRANSFER_NAME_TEST_" + ID;
    datasetName = "MY_DATASET_NAME_TEST_" + ID;
    tableName = "MY_TABLE_NAME_TEST_" + ID;
    // create a temporary dataset
    bigquery = BigQueryOptions.getDefaultInstance().getService();
    bigquery.create(DatasetInfo.of(datasetName));
    // create a temporary table
    Schema schema =
        Schema.of(
            Field.of("name", StandardSQLTypeName.STRING),
            Field.of("post_abbr", StandardSQLTypeName.STRING));
    TableDefinition tableDefinition = StandardTableDefinition.of(schema);
    TableInfo tableInfo = TableInfo.of(TableId.of(datasetName, tableName), tableDefinition);
    bigquery.create(tableInfo);

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown() throws IOException {
    // Clean up
    DeleteScheduledQuery.deleteScheduledQuery(name);
    // delete a temporary table
    bigquery.delete(TableId.of(datasetName, tableName));
    // delete a temporary dataset
    bigquery.delete(datasetName, BigQuery.DatasetDeleteOption.deleteContents());
    // restores print statements in the original method
    System.out.flush();
    System.setOut(originalPrintStream);
    LOG.log(Level.INFO, bout.toString());
  }

  @Test
  public void testCreateAzureBlobStorageTransfer() throws IOException {
    Map<String, Value> params = new HashMap<>();
    params.put(
        "destination_table_name_template", Value.newBuilder().setStringValue(tableName).build());
    params.put(
        "storage_account", Value.newBuilder().setStringValue(DTS_AZURE_STORAGE_ACCOUNT).build());
    params.put("container", Value.newBuilder().setStringValue(DTS_AZURE_BLOB_CONTAINER).build());
    params.put("data_path", Value.newBuilder().setStringValue("*").build());
    params.put("sas_token", Value.newBuilder().setStringValue(DTS_AZURE_SAS_TOKEN).build());
    params.put("file_format", Value.newBuilder().setStringValue("CSV").build());
    params.put("field_delimiter", Value.newBuilder().setStringValue(",").build());
    params.put("skip_leading_rows", Value.newBuilder().setStringValue("1").build());

    CreateAzureBlobStorageTransfer.createAzureBlobStorageTransfer(
        PROJECT_ID, displayName, datasetName, params);
    String result = bout.toString();
    name = result.substring(result.indexOf(":") + 1, result.length() - 1);
    assertThat(result).contains("Azure Blob Storage transfer created successfully: ");
  }
}
