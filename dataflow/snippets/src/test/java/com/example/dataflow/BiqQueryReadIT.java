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

import static org.junit.Assert.assertFalse;
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
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BiqQueryReadIT {
  private static final String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");

  private ByteArrayOutputStream bout;
  private PrintStream out;
  private BigQuery bigquery;
  private String datasetName;
  private String tableName;

  @Before
  public void setUp() throws InterruptedException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);

    bigquery = BigQueryOptions.getDefaultInstance().getService();

    // Create a new dataset and a table with the expected schema.
    datasetName = "test_dataset_" + UUID.randomUUID().toString().substring(0, 8);
    tableName = "test_table_" + UUID.randomUUID().toString().substring(0, 8);
    Schema schema = Schema.of(
        Field.of("user_name", StandardSQLTypeName.STRING),
        Field.of("age", StandardSQLTypeName.INT64));
    bigquery.create(DatasetInfo.newBuilder(datasetName).build());
    TableInfo tableInfo =
        TableInfo.newBuilder(TableId.of(datasetName, tableName), StandardTableDefinition.of(schema))
            .build();
    bigquery.create(tableInfo);

    // Insert rows into the new table.
    String query = String.format("INSERT INTO `%s.%s.%s` VALUES('a',18),('b',25),('c',70)",
        projectId, datasetName, tableName);
    QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
    bigquery.query(queryConfig);
  }

  @After
  public void tearDown() {
    bigquery.delete(
        DatasetId.of(projectId, datasetName), DatasetDeleteOption.deleteContents());
    System.setOut(null);
  }

  @Test
  public void readTableRows() {
    BiqQueryReadTableRows.main(
        new String[] {
            "--runner=DirectRunner",
            "--projectId=" + projectId,
            "--datasetName=" + datasetName,
            "--tableName=" + tableName
        });
    String got = bout.toString();
    assertTrue(got.contains("Name: c, Age: 70"));
  }

  @Test
  public void readAvro() {
    BigQueryReadAvro.main(
        new String[] {
            "--runner=DirectRunner",
            "--projectId=" + projectId,
            "--datasetName=" + datasetName,
            "--tableName=" + tableName
        });
    String got = bout.toString();
    assertTrue(got.contains("Name: c, Age: 70"));
  }

  @Test
  public void readWithFilteringAndProjection() {
    BigQueryReadWithProjectionAndFiltering.main(
        new String[] {
            "--runner=DirectRunner",
            "--projectId=" + projectId,
            "--datasetName=" + datasetName,
            "--tableName=" + tableName
        });
    String got = bout.toString();
    assertTrue(got.contains("Name: c, Age: 70"));
    assertFalse(got.contains("18"));
  }

  @Test
  public void readFromQuery() {
    BigQueryReadFromQuery.main(
        new String[] {
            "--runner=DirectRunner"
        });
    String got = bout.toString();
    assertTrue(got.contains("Repo:"));
  }
}
