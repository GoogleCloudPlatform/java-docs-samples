/*
 * Copyright 2019 Google Inc.
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

package com.m.examples.bigtable;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminSettings;
import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.BigtableDataSettings;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Integration tests for {@link Quickstart} */
public class QuickstartTest {
  private static void requireSysProp(String varName) {
    assertNotNull(
        System.getenv(varName),
        "System property '%s' is required to perform these tests.".format(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireSysProp(PROJECT_PROPERTY_NAME);
    requireSysProp(INSTANCE_PROPERTY_NAME);
  }

  private static final String PROJECT_PROPERTY_NAME = "bigtable.project";
  private static final String INSTANCE_PROPERTY_NAME = "bigtable.instance";
  private static final String TABLE_ID = "quickstart-table";
  private static BigtableDataClient dataClient;
  private static BigtableTableAdminClient adminClient;
  private static String projectId;
  private static String instanceId;
  private ByteArrayOutputStream bout;

  @BeforeClass
  public static void beforeClass() throws IOException {
    projectId = System.getProperty(PROJECT_PROPERTY_NAME);
    instanceId = System.getProperty(INSTANCE_PROPERTY_NAME);

    BigtableDataSettings settings =
        BigtableDataSettings.newBuilder().setProjectId(projectId).setInstanceId(instanceId).build();
    dataClient = BigtableDataClient.create(settings);
    BigtableTableAdminSettings adminSettings =
        BigtableTableAdminSettings.newBuilder()
            .setProjectId(projectId)
            .setInstanceId(instanceId)
            .build();
    adminClient = BigtableTableAdminClient.create(adminSettings);
  }

  @AfterClass
  public static void afterClass() throws Exception {
    dataClient.close();
    adminClient.close();
  }

  @Before
  public void setup() throws IOException {
    if (adminClient == null || dataClient == null) {
      throw new AssumptionViolatedException(
          PROJECT_PROPERTY_NAME
              + " or "
              + INSTANCE_PROPERTY_NAME
              + " property is not set, skipping integration tests.");
    }
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @Test
  public void testQuickstart() {
    try {
      Quickstart.quickstart(projectId, instanceId, TABLE_ID);
    } catch (Exception e) {
      System.out.println("Failed to run quickstart.");
      System.out.println(e);
    }

    String output = bout.toString();
    assertThat(output, CoreMatchers.containsString("Reading a single row by row key"));
    assertThat(output, CoreMatchers.containsString("Row: r1"));
    assertThat(
        output, CoreMatchers.containsString("Family: cf1    Qualifier: c1    Value: quickstart"));
  }
}
