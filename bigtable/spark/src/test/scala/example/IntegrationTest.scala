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

import com.google.bigtable.repackaged.com.google.cloud.bigtable.data.v2.models.Query
import com.google.bigtable.repackaged.com.google.cloud.bigtable.data.v2.{BigtableDataClient, BigtableDataSettings}
import org.scalatest.flatspec._
import org.scalatest.matchers._

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
  val file = "src/test/resources/Romeo-and-Juliet-prologue.txt"
  val table_copytable = getOrThrowException("BIGTABLE_SPARK_COPYTABLE_TABLE")
  val rowCount = 88

  "IntegrationTest" should "write records to Bigtable, copy them between tables" in {
    import org.apache.spark.{SparkConf, SparkContext}
    val appName = getClass.getSimpleName.replace("$", "")
    val config = new SparkConf().setMaster("local[*]").setAppName(appName)
    SparkContext.getOrCreate(config)

    val wordcountArgs = Array(projectId, instanceId, table_wordcount, file)
    Wordcount.main(wordcountArgs)
    val copytableArgs = Array(projectId, instanceId, table_wordcount, table_copytable)
    CopyTable.main(copytableArgs)

    val settings =
      BigtableDataSettings.newBuilder().setProjectId(projectId).setInstanceId(instanceId).build()
    val dataClient = BigtableDataClient.create(settings)
    import collection.JavaConverters._
    val wordcountRowCount = dataClient.readRows(Query.create(table_wordcount)).iterator().asScala.length
    val copytableRowCount = dataClient.readRows(Query.create(table_copytable)).iterator().asScala.length
    wordcountRowCount should be(rowCount)
    wordcountRowCount should be(copytableRowCount)
  }
}
