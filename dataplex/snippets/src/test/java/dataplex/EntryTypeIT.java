/*
 * Copyright 2024 Google LLC
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

package dataplex;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.cloud.dataplex.v1.EntryType;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class EntryTypeIT {
  private static final String ID = UUID.randomUUID().toString().substring(0, 8);
  private static final String LOCATION = "us-central1";
  private static final String entryTypeId = "test-entry-type" + ID;
  private static String expectedEntryType;

  private static final String PROJECT_ID = requireProjectIdEnvVar();

  private static String requireProjectIdEnvVar() {
    String value = System.getenv("GOOGLE_CLOUD_PROJECT");
    assertNotNull(
        "Environment variable GOOGLE_CLOUD_PROJECT is required to perform these tests.", value);
    return value;
  }

  @BeforeClass
  public static void checkRequirements() {
    requireProjectIdEnvVar();
  }

  @BeforeClass
  public static void setUp() throws Exception {
    expectedEntryType =
        String.format("projects/%s/locations/%s/entryTypes/%s", PROJECT_ID, LOCATION, entryTypeId);
    // Create Entry Type resource that will be used in tests for "get", "list" and "update" methods
    CreateEntryType.createEntryType(PROJECT_ID, LOCATION, entryTypeId);
  }

  @Test
  public void testListEntryTypes() throws IOException {
    List<EntryType> entryTypes = ListEntryTypes.listEntryTypes(PROJECT_ID, LOCATION);
    assertThat(entryTypes.stream().map(EntryType::getName)).contains(expectedEntryType);
  }

  @Test
  public void testGetEntryType() throws IOException {
    EntryType entryType = GetEntryType.getEntryType(PROJECT_ID, LOCATION, entryTypeId);
    assertThat(entryType.getName()).isEqualTo(expectedEntryType);
  }

  @Test
  public void testUpdateEntryType() throws Exception {
    EntryType entryType = UpdateEntryType.updateEntryType(PROJECT_ID, LOCATION, entryTypeId);
    assertThat(entryType.getName()).contains(expectedEntryType);
  }

  @Test
  public void testCreateEntryType() throws Exception {
    String entryTypeIdToCreate = "test-entry-type" + UUID.randomUUID().toString().substring(0, 8);
    String expectedEntryTypeToCreate =
        String.format(
            "projects/%s/locations/%s/entryTypes/%s", PROJECT_ID, LOCATION, entryTypeIdToCreate);

    EntryType entryType =
        CreateEntryType.createEntryType(PROJECT_ID, LOCATION, entryTypeIdToCreate);
    // Clean-up created Entry Type
    DeleteEntryType.deleteEntryType(PROJECT_ID, LOCATION, entryTypeIdToCreate);

    assertThat(entryType.getName()).contains(expectedEntryTypeToCreate);
  }

  @Test
  public void testDeleteEntryType() throws Exception {
    String entryTypeIdToDelete = "test-entry-type" + UUID.randomUUID().toString().substring(0, 8);
    // Create Entry Group to be deleted
    CreateEntryType.createEntryType(PROJECT_ID, LOCATION, entryTypeIdToDelete);

    // No exception means successful call.
    DeleteEntryType.deleteEntryType(PROJECT_ID, LOCATION, entryTypeIdToDelete);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    // Clean-up Entry Group resource created in setUp()
    DeleteEntryType.deleteEntryType(PROJECT_ID, LOCATION, entryTypeId);
  }
}
