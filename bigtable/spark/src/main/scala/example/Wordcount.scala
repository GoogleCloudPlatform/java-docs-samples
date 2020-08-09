package example

import org.apache.hadoop.hbase.spark.datasources.HBaseTableCatalog

import scala.util.Try

object Wordcount extends App {

  println("Starting up...")

  import org.apache.spark.sql.SparkSession
  val spark = SparkSession.builder().master("local[*]").getOrCreate()

  println("Spark version: " + spark.version)

  val table = Try(args(0)).getOrElse("wordcount")
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
  val numRecords = Try(args(1).toInt).getOrElse(10)
  println(s"Saving $numRecords records to $table")
  import spark.implicits._
  val records = (0 until numRecords).map(BigtableRecord.apply).toDF
  val opts = Map(
    HBaseTableCatalog.tableCatalog -> catalog,
    HBaseTableCatalog.newTable -> "5",
    "hbase.spark.use.hbasecontext" -> "false")
  records
    .write
    .format("org.apache.hadoop.hbase.spark")
    .options(opts)
    .save

  println("Done.")
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
