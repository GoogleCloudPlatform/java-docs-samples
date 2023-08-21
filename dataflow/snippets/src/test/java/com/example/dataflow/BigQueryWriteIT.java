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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.DatasetDeleteOption;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.cloud.bigquery.TableResult;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.apache.beam.sdk.PipelineResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BigQueryWriteIT {
  private static final String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");

  private ByteArrayOutputStream bout;
  private PrintStream out;
  private BigQuery bigquery;
  private String datasetName;
  private String tableName;

  private void createTable() {
    Schema schema = Schema.of(
        Field.of("user_name", StandardSQLTypeName.STRING),
        Field.of("age", StandardSQLTypeName.INT64));
    TableInfo tableInfo =
        TableInfo.newBuilder(TableId.of(datasetName, tableName), StandardTableDefinition.of(schema))
            .build();
    bigquery.create(tableInfo);
  }

  @Before
  public void setUp() throws InterruptedException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    bigquery = BigQueryOptions.getDefaultInstance().getService();

    datasetName = "test_dataset_" + UUID.randomUUID().toString().substring(0, 8);
    tableName = "test_table_" + UUID.randomUUID().toString().substring(0, 8);
    bigquery.create(DatasetInfo.newBuilder(datasetName).build());
  }

  @After
  public void tearDown() {
    bigquery.delete(
        DatasetId.of(projectId, datasetName), DatasetDeleteOption.deleteContents());
    System.setOut(null);
  }

  @Test
  public void write() throws Exception {
    createTable();
    BigQueryWrite.main(
        new String[] {
            "--runner=DirectRunner",
            "--projectId=" + projectId,
            "--datasetName=" + datasetName,
            "--tableName=" + tableName
        });
    // Verify that the records are visible in the table.
    String query = "SELECT * FROM " + tableName;
    QueryJobConfiguration queryConfig =
        QueryJobConfiguration.newBuilder(query).setDefaultDataset(datasetName).build();
    TableResult result = bigquery.query(queryConfig);
    assertEquals(result.getTotalRows(), 3);
  }

  @Test
  public void writeWithSchema() throws Exception {
    // Write to a new table.
    BigQueryWriteWithSchema.main(
        new String[] {
            "--runner=DirectRunner",
            "--projectId=" + projectId,
            "--datasetName=" + datasetName,
            "--tableName=" + tableName
        });
    // Verify that the records are visible in the new table.
    String query = "SELECT * FROM " + tableName;
    QueryJobConfiguration queryConfig =
        QueryJobConfiguration.newBuilder(query).setDefaultDataset(datasetName).build();
    TableResult result = bigquery.query(queryConfig);
    assertEquals(result.getTotalRows(), 3);
  }

  @Test
  public void streamExactlyOnce() throws Exception {
    createTable();
    PipelineResult r = BigQueryStreamExactlyOnce.main(
        new String[] {
            "--runner=DirectRunner",
            "--projectId=" + projectId,
            "--datasetName=" + datasetName,
            "--tableName=" + tableName,
            "--blockOnRun=false"
        }
    );
    r.waitUntilFinish();
    // Verify that the records are visible in the new table.
    String query = "SELECT * FROM " + tableName;
    QueryJobConfiguration queryConfig =
        QueryJobConfiguration.newBuilder(query).setDefaultDataset(datasetName).build();
    TableResult result = bigquery.query(queryConfig);
    assertEquals(3, result.getTotalRows());
    // Verify that the bad data was written to the error collection.
    String got = bout.toString();
    assertTrue(got.contains("Failed insert: "));
  }
}
