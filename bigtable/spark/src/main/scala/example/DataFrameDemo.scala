package example

import org.apache.hadoop.hbase.spark.datasources.{HBaseSparkConf, HBaseTableCatalog}

object DataFrameDemo extends App {

  val appName = this.getClass.getSimpleName.replace("$", "")
  println(s"$appName Spark application is starting up...")

  val (projectId, instanceId, table) = parse(args)
  println(
    s"""
      |Parameters:
      |projectId: $projectId
      |instanceId: $instanceId
      |table: $table
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
  // https://github.com/apache/hbase-connectors/blob/rel/1.0.0/spark/hbase-spark/src/main/scala/org/apache/hadoop/hbase/spark/datasources/HBaseSparkConf.scala#L44
  new HBaseContext(spark.sparkContext, conf)

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

  // The options are described in the sources themselves only
  // https://github.com/apache/hbase-connectors/blob/rel/1.0.0/spark/hbase-spark/src/main/scala/org/apache/hadoop/hbase/spark/datasources/HBaseSparkConf.scala
  val opts = Map(
    HBaseTableCatalog.tableCatalog -> catalog,
    // If defined and larger than 3, a new table will be created with the nubmer of region specified.
    // https://github.com/apache/hbase-connectors/blob/rel/1.0.0/spark/hbase-spark/src/main/scala/org/apache/hadoop/hbase/spark/datasources/HBaseTableCatalog.scala#L208-L209
    // 5 is simply larger than 3 and creates BIGTABLE_SPARK_TABLE unless available
    HBaseTableCatalog.newTable -> "5")

  val numRecords = 10
  println(s"Writing $numRecords records to $table")
  import spark.implicits._
  (0 until numRecords)
    .map(BigtableRecord.apply)
    .toDF
    .write
    .format("org.apache.hadoop.hbase.spark")
    .options(opts)
    .save
  println(s"Writing to $table...DONE")

  println(s"Loading $table")
  spark
    .read
    .format("org.apache.hadoop.hbase.spark")
    .options(opts)
    .load
    .show(truncate = false)
  println(s"Loading $table...DONE")

  def parse(args: Array[String]): (String, String, String) = {
    import scala.util.Try
    val projectId = Try(args(0)).getOrElse {
      throw new IllegalStateException("Missing command-line argument: BIGTABLE_SPARK_PROJECT_ID")
    }
    val instanceId = Try(args(1)).getOrElse {
      throw new IllegalStateException("Missing command-line argument: BIGTABLE_SPARK_INSTANCE_ID")
    }
    val table = Try(args(2)).getOrElse {
      throw new IllegalStateException("Missing command-line argument: BIGTABLE_SPARK_TABLE")
    }
    (projectId, instanceId, table)
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
