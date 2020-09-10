package example

import org.scalatest._
import flatspec._
import matchers._

class IntegrationTest extends AnyFlatSpec
    with should.Matchers {

  def getOrThrowException(envName: String): String = {
    sys.env.getOrElse(
      envName,
      throw new IllegalStateException(s"Environment variable '$envName' is required to perform this integration test."))
  }
  val projectId = getOrThrowException("BIGTABLE_SPARK_PROJECT_ID")
  val instanceId = getOrThrowException("BIGTABLE_SPARK_INSTANCE_ID")
  val table_wordcount = getOrThrowException("BIGTABLE_SPARK_WORDCOUNT_TABLE")
  val file = getOrThrowException("BIGTABLE_SPARK_WORDCOUNT_FILE")
  val table_copytable = getOrThrowException("BIGTABLE_SPARK_COPYTABLE_TABLE")

  "IntegrationTest" should "write records to Bigtable, copy them between tables" in {
    import org.apache.spark.{SparkConf, SparkContext}
    val appName = getClass.getSimpleName.replace("$", "")
    val config = new SparkConf().setMaster("local[*]").setAppName(appName)
    SparkContext.getOrCreate(config)

    val wordcountArgs = Array(projectId, instanceId, table_wordcount, file)
    Wordcount.main(wordcountArgs)
    val copytableArgs = Array(projectId, instanceId, table_wordcount, table_copytable)
    CopyTable.main(copytableArgs)

    // Assert that the number of rows in the tables are the same
    // cbt \
    //  -project=$BIGTABLE_SPARK_PROJECT_ID \
    //  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
    //  count $BIGTABLE_SPARK_WORDCOUNT_TABLE
    // cbt \
    //  -project=$BIGTABLE_SPARK_PROJECT_ID \
    //  -instance=$BIGTABLE_SPARK_INSTANCE_ID \
    //  count $BIGTABLE_SPARK_COPYTABLE_TABLE
  }
}
