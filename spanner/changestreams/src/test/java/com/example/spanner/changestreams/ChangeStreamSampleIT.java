/*
 * Copyright 2022 Google LLC
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

package com.example.spanner.changestreams;

import static org.junit.Assert.assertNotNull;

import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Instance;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import com.google.cloud.spanner.connection.ConnectionOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for ChangeStreamSample.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class ChangeStreamSampleIT {
  private static String instanceId = System.getProperty("spanner.test.instance");
  private static final String databaseId =
      formatForTest(System.getProperty("spanner.sample.database", "cssample"));
  private static final String prefix = "prefix";
  private static DatabaseId dbId;
  private static DatabaseAdminClient dbClient;

  private ByteArrayOutputStream bout;
  private final PrintStream stdOut = System.out;
  private PrintStream out;

  static String formatForTest(String name) {
    return name + "-" + UUID.randomUUID().toString().substring(0, 20);
  }

  @Before
  public void setUp() {
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
    try {
      dbClient.createDatabase(instanceId, databaseId, Collections.emptyList())
          .get(10, TimeUnit.MINUTES);
    } catch (Exception e) {
      e.printStackTrace();
    }

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    ConnectionOptions.closeSpanner();
    dbClient.dropDatabase(dbId.getInstanceId().getInstance(), dbId.getDatabase());

    try {
      bout.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.setOut(stdOut);
  }

  @Test
  public void testChangeStreamSample() {
    assertNotNull(instanceId);
    assertNotNull(databaseId);
    assertNotNull(prefix);
    ChangeStreamSample.run(instanceId, databaseId, prefix);

    String got = bout.toString();
    System.setOut(stdOut);
    Assert.assertTrue(got, got.contains("Received a ChildPartitionsRecord"));
    Assert.assertTrue(got, got.contains("Received a DataChangeRecord"));
    Assert.assertTrue(got, got.contains("mods=[Mod{keysJson={\"SingerId\":\"1\"}, "
        + "oldValuesJson='', "
        + "newValuesJson="
        + "'{\"FirstName\":\"singer_1_first_name\",\"LastName\":\"singer_1_last_name\"}'}, "
        + "Mod{keysJson={\"SingerId\":\"2\"}, "
        + "oldValuesJson='', "
        + "newValuesJson="
        + "'{\"FirstName\":\"singer_2_first_name\",\"LastName\":\"singer_2_last_name\"}'}]"));
  }
}
