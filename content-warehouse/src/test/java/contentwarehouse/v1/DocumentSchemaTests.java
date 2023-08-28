/*
 * Copyright 2023 Google LLC
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

package contentwarehouse.v1;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DocumentSchemaTests {
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION = "us";
  private static final String DOCUMENT_SCHEMA_ID = "27hhcik7eddv0";
  private static final String DELETE_DOCUMENT_SCHEMA_ID = "1en66na9epak0";

  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;

  private static void requireEnvVar(String varName) {
    assertNotNull(
        String.format("Environment variable '%s' must be set to perform these tests.", varName),
        System.getenv(varName));
  }

  @Before
  public void checkRequirements() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @Test
  public void testCreateDocumentSchema()
      throws InterruptedException, ExecutionException, IOException, TimeoutException {
    CreateDocumentSchema.createDocumentSchema(PROJECT_ID, LOCATION);
    String got = bout.toString();
    assertThat(got).contains("document");
  }

  @Test
  public void testGetDocumentSchemas()
      throws InterruptedException, ExecutionException, IOException, TimeoutException {
    GetDocumentSchema.getDocumentSchema(PROJECT_ID, LOCATION, DOCUMENT_SCHEMA_ID);
    String got = bout.toString();
    System.out.println(got);
    assertThat(got).contains("document");
  }
  
  @Test
  public void testListDocumentSchemas()
      throws InterruptedException, ExecutionException, IOException, TimeoutException {
    ListDocumentSchema.listDocumentSchemas(PROJECT_ID, LOCATION);
    String got = bout.toString();
    System.out.println(got);
    assertThat(got).contains("document");
  }
    
  @Test
  public void testUpdateDocumentSchema()
      throws InterruptedException, ExecutionException, IOException, TimeoutException {
    UpdateDocumentSchema.updateDocumentSchema(PROJECT_ID, LOCATION, DOCUMENT_SCHEMA_ID);
    String got = bout.toString();
    assertThat(got).contains("Schema");
  }

  @Test(expected = NotFoundException.class)
  public void testDeleteDocumentSchemas()
      throws InterruptedException, ExecutionException, IOException, TimeoutException {
    DeleteDocumentSchema.deleteDocumentSchema(PROJECT_ID, LOCATION, DELETE_DOCUMENT_SCHEMA_ID);
  }

  @After
  public void tearDown() {
    System.out.flush();
    System.setOut(originalPrintStream);
  }
}
