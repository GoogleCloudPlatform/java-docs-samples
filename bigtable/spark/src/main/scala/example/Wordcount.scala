package example

import com.google.cloud.bigtable.hbase.BigtableConfiguration
import org.apache.hadoop.hbase.HConstants
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.SparkContext

object Wordcount extends App {

  val projectId = args(0)
  val instanceId = args(1)
  val table = args(2)
  val file = args(3)

  val ColumnFamily = "cf"
  val ColumnFamilyBytes = Bytes.toBytes(ColumnFamily)
  val ColumnNameBytes = Bytes.toBytes("Count")

  import org.apache.spark.SparkConf
  val sparkConf = new SparkConf()

  // Workaround for a bug in TableOutputFormat in HBase 1.6.0
  // See https://stackoverflow.com/a/51959451/1305344
  sparkConf.set("spark.hadoop.validateOutputSpecs", "false")

  var conf = BigtableConfiguration.configure(projectId, instanceId)
  conf.set(TableOutputFormat.OUTPUT_TABLE, table)

  // workaround: https://issues.apache.org/jira/browse/SPARK-21549
  conf.set("mapreduce.output.fileoutputformat.outputdir", "/tmp")
  import org.apache.hadoop.mapreduce.Job
  val job = new Job(conf)
  job.setOutputFormatClass(classOf[TableOutputFormat[ImmutableBytesWritable]])
  conf = job.getConfiguration()

  val sc = new SparkContext(sparkConf)
  val wordCounts = sc
    .textFile(file)
    .flatMap(_.split("\\W+"))
    .filter(!_.isEmpty)
    .map { word => (word, 1) }
    .reduceByKey(_ + _)
    .map { case (word, count) =>
      val put = new Put(Bytes.toBytes(word))
        .addColumn(ColumnFamilyBytes, ColumnNameBytes, Bytes.toBytes(count))
      // The underlying writer ignores keys, only the value matter here.
      // https://github.com/apache/hbase/blob/1b9269/hbase-mapreduce/src/main/java/org/apache/hadoop/hbase/mapreduce/TableOutputFormat.java#L138-L145
      (null, put)
    }
  wordCounts.saveAsNewAPIHadoopDataset(conf)

}
