package example

import org.apache.hadoop.hbase.spark.datasources.{HBaseSparkConf, HBaseTableCatalog}

import scala.util.Try

object DataFrameDemo extends App {

  println("Starting up...")

  val projectId = args(0)
  val instanceId = args(1)
  val table = Try(args(2)).getOrElse("dataframe-demo")
  val numRecords = Try(args(3).toInt).getOrElse(10)

  import org.apache.spark.sql.SparkSession
  val spark = SparkSession.builder().master("local[*]").getOrCreate()

  println("Spark version: " + spark.version)

  val catalog =
    s"""{
       |"table":{"namespace":"default", "name":"$table", "tableCoder":"PrimitiveType"},
       |"rowkey":"key",
       |"columns":{
       |"col0":{"cf":"rowkey", "col":"key", "type":"string"},
       |"col1":{"cf":"cf1", "col":"col1", "type":"boolean"},
       |"col2":{"cf":"cf2", "col":"col2", "type":"double"},
       |"col3":{"cf":"cf3", "col":"col3", "type":"int"}
       |}
       |}""".stripMargin
  println(s"Saving $numRecords records to $table")
  import spark.implicits._
  val records = (0 until numRecords).map(BigtableRecord.apply).toDF
  val opts = Map(
    HBaseTableCatalog.tableCatalog -> catalog,
    HBaseTableCatalog.newTable -> "5",
    HBaseSparkConf.USE_HBASECONTEXT -> "false") // accepts xml configs only

  // Hack to specify HBase properties on command line
  // BEGIN
  // import org.apache.hadoop.hbase.HBaseConfiguration
  // val conf = HBaseConfiguration.create()
  // conf.set("google.bigtable.project.id", projectId)
  // conf.set("google.bigtable.instance.id", instanceId)
  import com.google.cloud.bigtable.hbase.BigtableConfiguration
  val conf = BigtableConfiguration.configure(projectId, instanceId)
  import org.apache.hadoop.hbase.spark.HBaseContext
  val hbaseContext = new HBaseContext(spark.sparkContext, conf)
  val opts_nouse = opts.filterNot { case (k, _) => k == HBaseSparkConf.USE_HBASECONTEXT }
  // END

  records
    .write
    .format("org.apache.hadoop.hbase.spark")
    .options(opts_nouse)
    .save

  println(s"Writing to $table...DONE")

  println(s"Loading $table")
  spark
    .read
    .format("org.apache.hadoop.hbase.spark")
    .options(opts_nouse)
    .load
    .show(truncate = false)
  println(s"Loading $table...DONE")

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
