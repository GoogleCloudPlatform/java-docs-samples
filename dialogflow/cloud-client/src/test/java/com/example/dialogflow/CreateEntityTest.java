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

import com.google.cloud.dialogflow.v2.EntityType;
import com.google.cloud.dialogflow.v2.EntityType.Kind;
import com.google.cloud.dialogflow.v2.EntityTypesClient;
import com.google.cloud.dialogflow.v2.ProjectAgentName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class CreateEntityTest {

  private static String PROJECT_ID = System.getenv().get("GOOGLE_CLOUD_PROJECT");
  private static String ENTITY_TYPE_DISPLAY_NAME =
      "entity_" + UUID.randomUUID().toString().substring(0, 23);
  private static String ENTITY_VALUE = UUID.randomUUID().toString();
  private static List<String> SYNONYMS =
      Arrays.asList("fake_synonym_for_testing_1", "fake_synonym_for_testing_2");
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private String entityTypeName;

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
  public void setUp() throws IOException {
    // Create a entity type to hold an entity
    try (EntityTypesClient client = EntityTypesClient.create()) {
      ProjectAgentName parent = ProjectAgentName.of(PROJECT_ID);
      EntityType entityType =
          EntityType.newBuilder()
              .setDisplayName(ENTITY_TYPE_DISPLAY_NAME)
              .setKind(Kind.valueOf("KIND_MAP"))
              .build();
      EntityType response = client.createEntityType(parent, entityType);
      entityTypeName = response.getName();
    }

    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() throws IOException {
    // Delete the created entity type
    try (EntityTypesClient client = EntityTypesClient.create()) {
      client.deleteEntityType(entityTypeName);
    }

    System.setOut(null);
  }

  @Test
  public void testCreateEntity() throws Exception {
    String entityTypeId = entityTypeName.split("/")[entityTypeName.split("/").length - 1];
    EntityManagement.createEntity(
        PROJECT_ID, entityTypeId, ENTITY_VALUE, Collections.singletonList(""));
    String got = bout.toString();
    assertThat(got).contains("Entity created");
  }
}
