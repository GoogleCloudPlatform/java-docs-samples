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

import com.google.cloud.dataplex.v1.EntryGroup;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class EntryGroupIT {
  private static final String ID = UUID.randomUUID().toString().substring(0, 8);
  private static final String LOCATION = "us-central1";
  private static final String entryGroupId = "test-entry-group-" + ID;
  private static String expectedEntryGroup;

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
  // Set-up code that will be executed before all tests
  public static void setUp() throws Exception {
    expectedEntryGroup =
        String.format(
            "projects/%s/locations/%s/entryGroups/%s", PROJECT_ID, LOCATION, entryGroupId);
    // Create Entry Group resource that will be used in tests for "get", "list" and "update" methods
    CreateEntryGroup.createEntryGroup(PROJECT_ID, LOCATION, entryGroupId);
  }

  @Test
  public void testListEntryGroups() throws IOException {
    List<EntryGroup> entryGroups = ListEntryGroups.listEntryGroups(PROJECT_ID, LOCATION);
    assertThat(entryGroups.stream().map(EntryGroup::getName)).contains(expectedEntryGroup);
  }

  @Test
  public void testGetEntryGroup() throws IOException {
    EntryGroup entryGroup = GetEntryGroup.getEntryGroup(PROJECT_ID, LOCATION, entryGroupId);
    assertThat(entryGroup.getName()).isEqualTo(expectedEntryGroup);
  }

  @Test
  public void testUpdateEntryGroup() throws Exception {
    EntryGroup entryGroup = UpdateEntryGroup.updateEntryGroup(PROJECT_ID, LOCATION, entryGroupId);
    assertThat(entryGroup.getName()).isEqualTo(expectedEntryGroup);
  }

  @Test
  public void testCreateEntryGroup() throws Exception {
    String entryGroupIdToCreate =
        "test-entry-group-" + UUID.randomUUID().toString().substring(0, 8);
    String expectedEntryGroupToCreate =
        String.format(
            "projects/%s/locations/%s/entryGroups/%s", PROJECT_ID, LOCATION, entryGroupIdToCreate);

    EntryGroup entryGroup =
        CreateEntryGroup.createEntryGroup(PROJECT_ID, LOCATION, entryGroupIdToCreate);
    // Clean-up created Entry Group
    DeleteEntryGroup.deleteEntryGroup(PROJECT_ID, LOCATION, entryGroupIdToCreate);

    assertThat(entryGroup.getName()).isEqualTo(expectedEntryGroupToCreate);
  }

  @Test
  public void testDeleteEntryGroup() throws Exception {
    String entryGroupIdToDelete =
        "test-entry-group-" + UUID.randomUUID().toString().substring(0, 8);
    // Create Entry Group to be deleted
    CreateEntryGroup.createEntryGroup(PROJECT_ID, LOCATION, entryGroupIdToDelete);

    // No exception means successful call
    DeleteEntryGroup.deleteEntryGroup(PROJECT_ID, LOCATION, entryGroupIdToDelete);
  }

  @AfterClass
  // Clean-up code that will be executed after all tests
  public static void tearDown() throws Exception {
    // Clean-up Entry Group resource created in setUp()
    DeleteEntryGroup.deleteEntryGroup(PROJECT_ID, LOCATION, entryGroupId);
  }
}
