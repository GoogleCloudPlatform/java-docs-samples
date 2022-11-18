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

package com.example.asset;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

/** Tests for real time savedQuery sample. */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SavedQuery {
  private static final String savedQueryId = UUID.randomUUID().toString();
  private static final String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
  private final String projectNumber = getProjectNumber(projectId);
  private final String savedQueryName = String.format("projects/%s/savedQueries/%s",
      projectNumber, savedQueryId);
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;

  private String getProjectNumber(String projectId) {
    ResourceManager resourceManager = ResourceManagerOptions.getDefaultInstance().getService();
    ProjectInfo project = resourceManager.get(projectId);
    return Long.toString(project.getProjectNumber());
  }

  @Before
  public void beforeTest() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown() {
    // restores print statements in the original method
    System.out.flush();
    System.setOut(originalPrintStream);
  }

  @Test
  public void test1CreateSavedQueryExample() throws Exception {
    CreateSavedQueryExample.createSavedQuery(
        savedQueryId, "saved_query_foo", projectId);
    String got = bout.toString();
    assertThat(got).contains("SavedQuery created successfully: " + savedQueryName);
  }

  @Test
  public void test2GetSavedQueryExample() throws Exception {
    GetSavedQueryExample.getSavedQuery(savedQueryName);
    String got = bout.toString();
    assertThat(got).contains("Get a savedQuery: " + savedQueryName);
  }

  @Test
  public void test3ListSavedQuerysExample() throws Exception {
    ListSavedQueriesExample.listSavedQueries(projectId);
    String got = bout.toString();
    assertThat(got).contains("Listed savedQueries under: " + projectId);
  }

  @Test
  public void test4UpdateSavedQueryExample() throws Exception {
    UpdateSavedQueryExample.updateSavedQuery(savedQueryName, "New Description");
    String got = bout.toString();
    assertThat(got).contains("SavedQuery updated successfully: " + savedQueryName);
  }

  @Test
  public void test5DeleteSavedQueryExample() throws Exception {
    DeleteSavedQueryExample.deleteSavedQuery(savedQueryName);
    String got = bout.toString();
    assertThat(got).contains("SavedQuery deleted");
  }
}
