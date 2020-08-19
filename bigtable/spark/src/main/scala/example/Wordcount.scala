package example

import com.google.cloud.bigtable.hbase.BigtableConfiguration
import org.apache.hadoop.hbase.TableName
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

  // FIXME Command-line option to create the table or not?
  var admin: Admin = _
  try {
    admin = ConnectionFactory.createConnection(hConf).getAdmin
    val td = TableDescriptorBuilder
      .newBuilder(TableName.valueOf(table))
      .setColumnFamily(ColumnFamilyDescriptorBuilder.of(ColumnFamily))
      .build()
    if (admin.tableExists(TableName.valueOf(table))) {
      println(s">>> Table $table exists")
    } else {
      println(s">>> Table $table does not exist. Creating it.")
      admin.createTable(td)
    }
  } finally {
    admin.close()
  }

  import org.apache.spark.SparkConf
  val sparkConf = new SparkConf()

  // FIXME Is this workaround still required?
  // Workaround for a bug in TableOutputFormat in HBase 1.6.0
  // See https://stackoverflow.com/a/51959451/1305344
  sparkConf.set("spark.hadoop.validateOutputSpecs", "false")

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
      // The KEY is ignored while the output value must be either a Put or a Delete instance
      // https://github.com/apache/hbase/blob/rel/2.2.3/hbase-mapreduce/src/main/java/org/apache/hadoop/hbase/mapreduce/TableOutputFormat.java#L46-L48
      // The underlying writer ignores keys, only the value matter here.
      // https://github.com/apache/hbase/blob/rel/2.2.3/hbase-mapreduce/src/main/java/org/apache/hadoop/hbase/mapreduce/TableOutputFormat.java#L138-L145
      (null, put)
    }
  wordCounts.saveAsNewAPIHadoopDataset(hConf)

}
