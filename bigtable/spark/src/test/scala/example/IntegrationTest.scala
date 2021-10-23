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

import java.util.UUID

import com.google.bigtable.repackaged.com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient
import com.google.bigtable.repackaged.com.google.cloud.bigtable.admin.v2.models.CreateTableRequest
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
  val projectId: String = getOrThrowException("GOOGLE_CLOUD_PROJECT")
  val instanceId: String = getOrThrowException("BIGTABLE_TESTING_INSTANCE")
  val file = "src/test/resources/Romeo-and-Juliet-prologue.txt"

  val wordcount_table_name: String = "spark-wordcount-" + UUID.randomUUID.toString.substring(0, 20);
  val copytable_table_name: String = "spark-copytable-" + UUID.randomUUID.toString.substring(0, 20);

  val tableClient: BigtableTableAdminClient = BigtableTableAdminClient.create(projectId, instanceId)
  val settings: BigtableDataSettings =
    BigtableDataSettings.newBuilder().setProjectId(projectId).setInstanceId(instanceId).build()
  val dataClient: BigtableDataClient = BigtableDataClient.create(settings)

  "IntegrationTest" should "write records to Bigtable, copy them between tables" in {
    try {
      import org.apache.spark.{SparkConf, SparkContext}
      val appName = getClass.getSimpleName.replace("$", "")
      val config = new SparkConf().setMaster("local[*]").setAppName(appName)
      SparkContext.getOrCreate(config)

      val wordcountRequest = CreateTableRequest.of(wordcount_table_name).addFamily("cf")
      tableClient.createTable(wordcountRequest)

      val copytableRequest = CreateTableRequest.of(copytable_table_name).addFamily("cf")
      tableClient.createTable(copytableRequest);

      val wordcountArgs = Array(projectId, instanceId, wordcount_table_name, file)
      Wordcount.main(wordcountArgs)
      val copytableArgs = Array(projectId, instanceId, wordcount_table_name, copytable_table_name)
      CopyTable.main(copytableArgs)

      import collection.JavaConverters._
      val wordcountRowCount = dataClient.readRows(Query.create(wordcount_table_name)).iterator().asScala.length
      val copytableRowCount = dataClient.readRows(Query.create(copytable_table_name)).iterator().asScala.length
      wordcountRowCount should be(88)
      wordcountRowCount should be(copytableRowCount)
    } finally {
      tableClient.deleteTable(wordcount_table_name)
      tableClient.deleteTable(copytable_table_name)
      tableClient.close()
      dataClient.close()
    }
  }
}
