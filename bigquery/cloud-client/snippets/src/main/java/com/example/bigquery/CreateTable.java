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

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;

public class CreateTable {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    // Project and dataset name to create a new table
    String projectId = "MY_PROJECT_ID";
    String datasetName = "MY_DATASET_NAME";
    String tableName = "MY_TABLE_NAME";

    // Schema for a Google BigQuery Table.
    Schema schema =
        Schema.of(
            Field.of("stringField", StandardSQLTypeName.STRING),
            Field.of("isBooleanField", StandardSQLTypeName.BOOL));
    createTable(projectId, datasetName, tableName, schema);
  }

  public static void createTable(
      String projectId, String datasetName, String tableName, Schema schema) {
    try {
      // Initialize client that will be used to send requests. This client only needs
      // to be created once, and can be reused for multiple requests.
      BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

      // Create table identity given the projectId, the datasetName and the tableName.
      TableId tableId = TableId.of(projectId, datasetName, tableName);
      // Create table definition to build the table information
      TableDefinition tableDefinition = StandardTableDefinition.of(schema);
      TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();

      // Create table
      Table table = bigquery.create(tableInfo);
      System.out.println("Table \"" + table.getTableId().getTable() + "\" created successfully");
    } catch (BigQueryException e) {
      System.out.println("Table was not created. \n" + e.toString());
    }
  }
}
