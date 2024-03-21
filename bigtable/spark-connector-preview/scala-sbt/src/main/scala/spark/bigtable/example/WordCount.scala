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
package spark.bigtable.example

import org.apache.spark.sql.SparkSession

object WordCount extends App {
  val (projectId, instanceId, tableName, createNewTable) = parse(args)

  val spark = SparkSession.builder().getOrCreate()

  val catalog =
    s"""{
       |"table":{"namespace":"default", "name":"$tableName", "tableCoder":"PrimitiveType"},
       |"rowkey":"wordCol",
       |"columns":{
       |  "word":{"cf":"rowkey", "col":"wordCol", "type":"string"},
       |  "count":{"cf":"example_family", "col":"countCol", "type":"int"}
       |}
       |}""".stripMargin

  import spark.implicits._
  val data = (0 to 9).map(i => ("word%d".format(i), i))
  val rdd = spark.sparkContext.parallelize(data)
  val df = rdd.toDF("word", "count")

  println("Created the DataFrame:");
  df.show()

  df
    .write
    .format("bigtable")
    .option("catalog", catalog)
    .option("spark.bigtable.project.id", projectId)
    .option("spark.bigtable.instance.id", instanceId)
    .option("spark.bigtable.create.new.table", createNewTable)
    .save
  println("DataFrame was written to Bigtable.")

  val readDf = spark
    .read
    .format("bigtable")
    .option("catalog", catalog)
    .option("spark.bigtable.project.id", projectId)
    .option("spark.bigtable.instance.id", instanceId)
    .load

  println("Reading the DataFrame from Bigtable:");
  readDf.show()

  def parse(args: Array[String]): (String, String, String, String) = {
    import scala.util.Try
    val projectId = Try(args(0)).getOrElse {
      throw new IllegalArgumentException("Missing command-line argument: SPARK_BIGTABLE_PROJECT_ID")
    }
    val instanceId = Try(args(1)).getOrElse {
      throw new IllegalArgumentException("Missing command-line argument: SPARK_BIGTABLE_INSTANCE_ID")
    }
    val tableName = Try(args(2)).getOrElse {
      throw new IllegalArgumentException("Missing command-line argument: SPARK_BIGTABLE_TABLE_NAME")
    }
    val createNewTable = Try(args(3)).getOrElse {
      "true"
    }
    (projectId, instanceId, tableName, createNewTable)
  }
}

