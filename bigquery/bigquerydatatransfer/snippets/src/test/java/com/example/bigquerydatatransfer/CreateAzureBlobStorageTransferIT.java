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

package com.example.bigquerydatatransfer;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.datatransfer.v1.TransferConfig;
import com.google.protobuf.Struct;
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
import org.junit.Test;

public class CreateAzureBlobStorageTransferIT {

  private static final Logger LOG =
      Logger.getLogger(CreateAzureBlobStorageTransferIT.class.getName());
  private static final String ID = UUID.randomUUID().toString().substring(0, 8);
  private BigQuery bigquery;
  private ByteArrayOutputStream bout;
  private String name;
  private String displayName;
  private String datasetName;
  private String tableName;
  private PrintStream out;
  private PrintStream originalPrintStream;

  private static final String PROJECT_ID = requireEnvVar("GOOGLE_CLOUD_PROJECT");
  private static final String DATASET_ID = requireEnvVar("DATASET_ID");
  private static final String TABLE_NAME = requireEnvVar("TABLE_NAME");
  private static final String STORAGE_ACCOUNT = requireEnvVar("STORAGE_ACCOUNT");
  private static final String CONTAINER = requireEnvVar("CONTAINER");
  private static final String DATA_PATH = requireEnvVar("DATA_PATH");
  private static final String SAS_TOKEN = requireEnvVar("SAS_TOKEN");

  private static String requireEnvVar(String varName) {
    String value = System.getenv(varName);
    assertNotNull(
        "Environment variable " + varName + " is required to perform these tests.",
        System.getenv(varName));
    return value;
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown() throws IOException {
    System.out.flush();
    System.setOut(originalPrintStream);
    LOG.log(Level.INFO, bout.toString());
  }

  @Test
  public void testCreateAzureBlobStorageTransfer() throws IOException {
    Map<String, Value> params = new HashMap<>();
    params.put(
        "destination_table_name_template", Value.newBuilder().setStringValue(TABLE_NAME).build());
    params.put("storage_account", Value.newBuilder().setStringValue(STORAGE_ACCOUNT).build());
    params.put("container", Value.newBuilder().setStringValue(CONTAINER).build());
    params.put("data_path", Value.newBuilder().setStringValue(DATA_PATH).build());
    params.put("sas_token", Value.newBuilder().setStringValue(SAS_TOKEN).build());
    params.put("file_format", Value.newBuilder().setStringValue("CSV").build());
    params.put("field_delimiter", Value.newBuilder().setStringValue(",").build());
    params.put("skip_leading_rows", Value.newBuilder().setStringValue("1").build());

    CreateAzureBlobStorageTransfer.createAzureBlobStorageTransfer(PROJECT_ID, DATASET_ID, params);
    String result = bout.toString();
    name = result.substring(result.indexOf(":") + 1, result.length() - 1);
    assertThat(result).contains("Azure Blob Storage transfer created successfully: ");
  }
}
