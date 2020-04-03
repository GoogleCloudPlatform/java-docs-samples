/*
 * Copyright 2020 Google LLC
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

package com.example.dialogflow;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.cloud.dialogflow.v2beta1.KnowledgeBaseName;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class ListDocumentsTest {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String TEST_KNOWLEDGE_BASE_ID = "MTA4MzE0ODY5NTczMTQzNzU2ODA";
  private static String TEST_DOCUMENT_ID = "MTUwNjk0ODg1NTU4NzkzMDExMg";
  private ByteArrayOutputStream bout;
  private PrintStream out;

  private static void requireEnvVar(String varName) {
    assertNotNull(
        "Environment variable '%s' is required to perform these tests.".format(varName),
        String.format(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testGetDocument() throws Exception {
    KnowledgeBaseName knowledgeBaseName =
        KnowledgeBaseName.of(PROJECT_ID, TEST_KNOWLEDGE_BASE_ID);
    DocumentManagement.listDocuments(knowledgeBaseName.toString());
    String got = bout.toString();
    assertThat(got).contains(TEST_DOCUMENT_ID);
  }
}
