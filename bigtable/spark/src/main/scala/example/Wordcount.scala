package example

import com.google.cloud.bigtable.hbase.BigtableConfiguration
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.SparkContext

// FIXME Explain the purpose of the app
object Wordcount extends App {

  val projectId = args(0)
  val instanceId = args(1)
  val table = args(2)
  val file = args(3)

  val ColumnFamily = "cf"
  val ColumnFamilyBytes = Bytes.toBytes(ColumnFamily)
  val ColumnNameBytes = Bytes.toBytes("Count")

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
  // Without the property:
  // org.apache.hadoop.mapred.InvalidJobConfException: Output directory not set.
  // at org.apache.hadoop.mapreduce.lib.output.FileOutputFormat.checkOutputSpecs(FileOutputFormat.java:138)
  // at org.apache.spark.internal.io.HadoopMapReduceWriteConfigUtil.assertConf(SparkHadoopWriter.scala:393)
  // at org.apache.spark.internal.io.SparkHadoopWriter$.write(SparkHadoopWriter.scala:71)
  // at org.apache.spark.rdd.PairRDDFunctions$$anonfun$saveAsNewAPIHadoopDataset$1.apply$mcV$sp(PairRDDFunctions.scala:1083)
  config.set("spark.hadoop.validateOutputSpecs", "false")

  val sc = SparkContext.getOrCreate(config)
  val wordCounts = sc
    .textFile(file)
    .flatMap(_.split("\\W+"))
    .filter(!_.isEmpty)
    .map { word => (word, 1) }
    .reduceByKey(_ + _)
    .map { case (word, count) =>
      val put = new Put(Bytes.toBytes(word))
        .addColumn(ColumnFamilyBytes, ColumnNameBytes, Bytes.toBytes(count))
      // The KEY is ignored while the output value must be either a Put or a Delete instance
      // https://github.com/apache/hbase/blob/rel/2.2.3/hbase-mapreduce/src/main/java/org/apache/hadoop/hbase/mapreduce/TableOutputFormat.java#L46-L48
      // The underlying writer ignores keys, only the value matter here.
      // https://github.com/apache/hbase/blob/rel/2.2.3/hbase-mapreduce/src/main/java/org/apache/hadoop/hbase/mapreduce/TableOutputFormat.java#L138-L145
      (null, put)
    }
  wordCounts.saveAsNewAPIHadoopDataset(hConf)

}
