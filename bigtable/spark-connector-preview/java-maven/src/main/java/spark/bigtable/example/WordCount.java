/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.bigtable.example;

import spark.bigtable.example.model.TestRow;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class WordCount {
  private static SparkSession spark;
  private static String projectId;
  private static String instanceId;
  private static String tableName;
  private static String createNewTable = "true";

  private static void parseArguments(String[] args) throws IllegalArgumentException {
    if (args.length < 3) {
      throw new IllegalArgumentException(
          "Arguments Bigtable project ID, instance ID, " +
              "and table name must be specified");
    }
    projectId = args[0];
    instanceId = args[1];
    tableName = args[2];
    if (args.length > 3) {
      createNewTable = args[3];
    }
  }

  public static void main(String[] args) throws IllegalArgumentException {
    parseArguments(args);

    spark = SparkSession.builder().getOrCreate();

    Dataset<Row> df = createTestDataFrame();
    System.out.println("Created the DataFrame:");
    df.show();

    String catalog = "{" +
        "\"table\":{\"namespace\":\"default\", \"name\":\"" + tableName + "\"," +
        "\"tableCoder\":\"PrimitiveType\"}," +
        "\"rowkey\":\"wordCol\"," +
        "\"columns\":{" +
        "\"word\":{\"cf\":\"rowkey\", \"col\":\"wordCol\", \"type\":\"string\"}," +
        "\"count\":{\"cf\":\"example_family\", \"col\":\"countCol\", \"type\":\"int\"}" +
        "}}".replaceAll("\\s+", "");

    writeDataframeToBigtable(df, catalog, createNewTable);
    System.out.println("DataFrame was written to Bigtable.");

    Dataset<Row> readDf = readDataframeFromBigtable(catalog);

    System.out.println("Reading the DataFrame from Bigtable:");
    readDf.show();
  }

  private static Dataset<Row> createTestDataFrame() {
    ArrayList<TestRow> rows = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      rows.add(new TestRow(String.format("word%d", i), i));
    }
    Dataset<Row> df = spark.createDataset(
        rows,
        Encoders.bean(TestRow.class))
        .toDF();

    return df;
  }

  private static void writeDataframeToBigtable(Dataset<Row> dataframe, String catalog,
      String createNewTable) {
    dataframe
        .write()
        .format("bigtable")
        .option("catalog", catalog)
        .option("spark.bigtable.project.id", projectId)
        .option("spark.bigtable.instance.id", instanceId)
        .option("spark.bigtable.create.new.table", createNewTable)
        .save();
  }

  private static Dataset<Row> readDataframeFromBigtable(String catalog) {
    Dataset<Row> dataframe = spark
        .read()
        .format("bigtable")
        .option("catalog", catalog)
        .option("spark.bigtable.project.id", projectId)
        .option("spark.bigtable.instance.id", instanceId)
        .load();
    return dataframe;
  }
}
