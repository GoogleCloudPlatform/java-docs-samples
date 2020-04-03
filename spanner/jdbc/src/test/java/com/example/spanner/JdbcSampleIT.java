/*
 * Copyright 2020 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.spanner;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Instance;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.jdbc.SpannerPool;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@code JdbcSample} */
@RunWith(JUnit4.class)
public class JdbcSampleIT {
  // The instance needs to exist for tests to pass.
  private String instanceId = System.getProperty("spanner.test.instance");
  private final String databaseId =
      formatForTest(System.getProperty("spanner.sample.database", "mysample"));
  DatabaseId dbId;
  DatabaseAdminClient dbClient;

  private String runSample(String command) throws Exception {
    PrintStream stdOut = System.out;
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    System.setOut(out);
    JdbcSample.main(
        new String[] {
          command, instanceId, databaseId, System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        });
    System.setOut(stdOut);
    System.out.printf("Finished running sample [%s]\n", command);
    return bout.toString();
  }

  @Before
  public void setUp() throws Exception {
    SpannerOptions options = SpannerOptions.newBuilder().build();
    Spanner spanner = options.getService();
    dbClient = spanner.getDatabaseAdminClient();
    if (instanceId == null) {
      Iterator<Instance> iterator =
          spanner.getInstanceAdminClient().listInstances().iterateAll().iterator();
      if (iterator.hasNext()) {
        instanceId = iterator.next().getId().getInstance();
      }
    }
    dbId = DatabaseId.of(options.getProjectId(), instanceId, databaseId);
    dbClient.dropDatabase(dbId.getInstanceId().getInstance(), dbId.getDatabase());
    dbClient.createDatabase(instanceId, databaseId, Collections.emptyList()).get();
  }

  @After
  public void tearDown() throws Exception {
    SpannerPool.closeSpannerPool();
    dbClient.dropDatabase(dbId.getInstanceId().getInstance(), dbId.getDatabase());
  }

  @Test
  public void testSample() throws Exception {
    assertThat(instanceId).isNotNull();
    assertThat(databaseId).isNotNull();
    String out = runSample("createtable");
    assertThat(out).contains("Created table");

    out = runSample("insertdata");
    assertThat(out).contains("Insert counts: [1, 1, 1, 1, 1]");

    out = runSample("baseurl");
    assertThat(out).contains("Connected to Cloud Spanner at [");
    if (System.getenv("GOOGLE_APPLICATION_CREDENTIALS") != null) {
      out = runSample("customcredentials");
      assertThat(out).contains("Connected to Cloud Spanner at [");
    }
    out = runSample("defaultprojectid");
    assertThat(out).contains("Connected to Cloud Spanner at [");
    out = runSample("connectionurlproperties");
    assertThat(out).contains("Readonly: true");
    assertThat(out).contains("Autocommit: false");
    out = runSample("connectionproperties");
    assertThat(out).contains("Readonly: true");
    assertThat(out).contains("Autocommit: false");
    out = runSample("datasource");
    assertThat(out).contains("Readonly: true");
    assertThat(out).contains("Autocommit: false");

    out = runSample("singleusereadonly");
    assertThat(out).contains("1 Marc Richards");
    assertThat(out).contains("2 Catalina Smith");
    // Sleep for 1 second as the following test will do a read with a 1 second max staleness,
    // and it could otherwise theoretically be that the data were inserted less than 1 second ago.
    Thread.sleep(1000L);
    out = runSample("singleusereadonlytimestampbound");
    assertThat(out).contains("1 Marc Richards");
    assertThat(out).contains("2 Catalina Smith");
    out = runSample("update");
    assertThat(out).contains("Inserted 1 row(s)");
    out = runSample("partitioneddml");
    assertThat(out).contains("Updated 5 row(s)");

    out = runSample("readonlytransaction");
    assertThat(out).contains("1 Marc Richards");
    assertThat(out).contains("2 Catalina Smith");
    assertThat(out).contains("Read-only transaction used read timestamp [");
    out = runSample("readwritetransaction");
    assertThat(out).contains("Transaction committed with commit timestamp [");

    out = runSample("batchdml");
    assertThat(out).contains("Batch insert counts: [1, 1, 1]");
    out = runSample("batchdmlusingsql");
    assertThat(out).contains("Batch insert counts: [1, 1, 1]");
    out = runSample("batchddl");
    assertThat(out).contains("DDL update counts: [-2, -2]");
    out = runSample("batchddlusingsql");
    assertThat(out).contains("Update count for CREATE TABLE Concerts: -2");
    assertThat(out).contains("Update count for CREATE INDEX SingersByFirstLastName: -2");
    assertThat(out).contains("Executed DDL batch");
    out = runSample("abortbatch");
    assertThat(out).contains("Aborted DML batch");

    out = runSample("committimestamp");
    assertThat(out).contains("Commit timestamp: [");
    out = runSample("readtimestamp");
    assertThat(out).contains("Read timestamp: [");

    out = runSample("connectionwithqueryoptions");
    assertThat(out).contains("1 Marc Richards");
    assertThat(out).contains("Optimizer version: 1");
    out = runSample("setqueryoptions");
    assertThat(out).contains("1 Marc Richards");
    assertThat(out).contains("Optimizer version: 1");

    out = runSample("bufferedwrite");
    assertThat(out).contains("Transaction committed at [");
    out = runSample("transactionwithretryloop");
    assertThat(out).contains("Transaction committed at [");
    out = runSample("jdbctransactionwithretryloop");
    assertThat(out).contains("Transaction committed at [");
  }

  static String formatForTest(String name) {
    return name + "-" + UUID.randomUUID().toString().substring(0, 20);
  }
}
