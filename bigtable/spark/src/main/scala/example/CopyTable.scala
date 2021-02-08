/*
 * Copyright 2020 Google LLC
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
package example

import org.apache.hadoop.hbase.spark.datasources.{HBaseSparkConf, HBaseTableCatalog}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

object CopyTable extends App {

  val appName = this.getClass.getSimpleName.replace("$", "")
  println(s"$appName Spark application is starting up...")

  val (projectId, instanceId, fromTable, toTable) = parse(args)
  println(
    s"""
      |Parameters:
      |projectId: $projectId
      |instanceId: $instanceId
      |copy from $fromTable to $toTable
      |""".stripMargin)

  import org.apache.spark.sql.SparkSession
  val spark = SparkSession.builder().getOrCreate()
  println(s"Spark version: ${spark.version}")

  import com.google.cloud.bigtable.hbase.BigtableConfiguration
  val conf = BigtableConfiguration.configure(projectId, instanceId)
  import org.apache.hadoop.hbase.spark.HBaseContext
  // Creating HBaseContext explicitly to use the conf above
  // That's how to use command-line arguments for projectId and instanceId
  // Otherwise, we'd have to use hbase-site.xml
  // See HBaseSparkConf.USE_HBASECONTEXT option in hbase-connectors project
  new HBaseContext(spark.sparkContext, conf)

  // Creates a configuration JSON for a given table
  // Used for HBaseTableCatalog.tableCatalog option
  // to read from or write to a Bigtable table
  def createCatalogJSON(table: String): String = {
    s"""{
       |"table":{"namespace":"default", "name":"$table", "tableCoder":"PrimitiveType"},
       |"rowkey":"word",
       |"columns":{
       |  "word":{"cf":"rowkey", "col":"word", "type":"string"},
       |  "count":{"cf":"cf", "col":"Count", "type":"int"}
       |}
       |}""".stripMargin
  }

  // The HBaseTableCatalog options are described in the sources themselves only
  // Search for HBaseSparkConf.scala in https://github.com/apache/hbase-connectors

  println(s"Loading records from $fromTable")
  val records = spark
    .read
    .format("org.apache.hadoop.hbase.spark")
    .option(HBaseTableCatalog.tableCatalog, createCatalogJSON(fromTable))
    .load
  println(s"Loading from $fromTable...DONE")

  records.show(truncate = false)

  println(s"Writing records to $toTable")
  records
    .write
    .format("org.apache.hadoop.hbase.spark")
    .option(HBaseTableCatalog.tableCatalog, createCatalogJSON(toTable))
    .save
  println(s"Writing to $toTable...DONE")

  def parse(args: Array[String]): (String, String, String, String) = {
    import scala.util.Try
    val projectId = Try(args(0)).getOrElse {
      throw new IllegalStateException("Missing command-line argument: BIGTABLE_SPARK_PROJECT_ID")
    }
    val instanceId = Try(args(1)).getOrElse {
      throw new IllegalStateException("Missing command-line argument: BIGTABLE_SPARK_INSTANCE_ID")
    }
    val fromTable = Try(args(2)).getOrElse {
      throw new IllegalStateException("Missing command-line argument: BIGTABLE_SPARK_WORDCOUNT_TABLE")
    }
    val toTable = Try(args(3)).getOrElse {
      throw new IllegalStateException("Missing command-line argument: BIGTABLE_SPARK_COPYTABLE_TABLE")
    }
    (projectId, instanceId, fromTable, toTable)
  }
}

case class BigtableRecord(
  col0: String,
  col1: Boolean,
  col2: Double,
  col3: Int)

object BigtableRecord {
  def apply(i: Int): BigtableRecord = {
    val s = s"""row${"%03d".format(i)}"""
    BigtableRecord(s,
      i % 2 == 0,
      i.toDouble,
      i)
  }
}
