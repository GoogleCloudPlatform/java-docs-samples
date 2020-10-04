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

import com.google.cloud.bigtable.hbase.BigtableConfiguration
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.SparkContext

object Wordcount extends App {

  val projectId = args(0)
  val instanceId = args(1)
  val table = args(2)
  val file = args(3)

  var hConf = BigtableConfiguration.configure(projectId, instanceId)
  hConf.set(TableOutputFormat.OUTPUT_TABLE, table)

  import org.apache.hadoop.mapreduce.Job
  val job = Job.getInstance(hConf)
  job.setOutputFormatClass(classOf[TableOutputFormat[ImmutableBytesWritable]])
  hConf = job.getConfiguration

  import org.apache.spark.SparkConf
  val config = new SparkConf()

  // Workaround for a bug in TableOutputFormat
  // See https://stackoverflow.com/a/51959451/1305344
  config.set("spark.hadoop.validateOutputSpecs", "false")

  val sc = SparkContext.getOrCreate(config)
  val wordCounts = sc
    .textFile(file)
    .flatMap(_.split("\\W+"))
    .filter(!_.isEmpty)
    .map { word => (word, 1) }
    .reduceByKey(_ + _)
    .map { case (word, count) =>
      val ColumnFamilyBytes = Bytes.toBytes("cf")
      val ColumnNameBytes = Bytes.toBytes("Count")
      val put = new Put(Bytes.toBytes(word))
        .addColumn(ColumnFamilyBytes, ColumnNameBytes, Bytes.toBytes(count))
      // The KEY is ignored while the output value must be either a Put or a Delete instance
      // The underlying writer ignores keys, only the value matters here.
      (null, put)
    }
  wordCounts.saveAsNewAPIHadoopDataset(hConf)
}
