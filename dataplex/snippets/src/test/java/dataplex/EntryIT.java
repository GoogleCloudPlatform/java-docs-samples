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

import com.google.cloud.dataplex.v1.Entry;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class EntryIT {
  private static final String ID = UUID.randomUUID().toString().substring(0, 8);
  private static final String LOCATION = "us-central1";
  private static final String entryGroupId = "test-entry-group-" + ID;
  private static final String entryId = "test-entry-" + ID;
  private static String expectedEntry;

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
    expectedEntry =
        String.format(
            "projects/%s/locations/%s/entryGroups/%s/entries/%s",
            PROJECT_ID, LOCATION, entryGroupId, entryId);
    // Create Entry Group resource that will be used for creating Entry
    CreateEntryGroup.createEntryGroup(PROJECT_ID, LOCATION, entryGroupId);
    // Create Entry that will be used in tests for "get", "lookup", "list" and "update" methods
    CreateEntry.createEntry(PROJECT_ID, LOCATION, entryGroupId, entryId);
  }

  @Test
  public void testListEntries() throws IOException {
    List<Entry> entries = ListEntries.listEntries(PROJECT_ID, LOCATION, entryGroupId);
    assertThat(entries.stream().map(Entry::getName)).contains(expectedEntry);
  }

  @Test
  public void testGetEntry() throws IOException {
    Entry entry = GetEntry.getEntry(PROJECT_ID, LOCATION, entryGroupId, entryId);
    assertThat(entry.getName()).isEqualTo(expectedEntry);
  }

  @Test
  public void testLookupEntry() throws IOException {
    Entry entry = LookupEntry.lookupEntry(PROJECT_ID, LOCATION, entryGroupId, entryId);
    assertThat(entry.getName()).isEqualTo(expectedEntry);
  }

  @Test
  public void testUpdateEntry() throws Exception {
    Entry entry = UpdateEntry.updateEntry(PROJECT_ID, LOCATION, entryGroupId, entryId);
    assertThat(entry.getName()).isEqualTo(expectedEntry);
  }

  @Test
  public void testCreateEntry() throws Exception {
    String entryIdToCreate = "test-entry-" + UUID.randomUUID().toString().substring(0, 8);
    String expectedEntryToCreate =
        String.format(
            "projects/%s/locations/%s/entryGroups/%s/entries/%s",
            PROJECT_ID, LOCATION, entryGroupId, entryIdToCreate);

    Entry entry = CreateEntry.createEntry(PROJECT_ID, LOCATION, entryGroupId, entryIdToCreate);
    // Clean-up created Entry
    DeleteEntry.deleteEntry(PROJECT_ID, LOCATION, entryGroupId, entryIdToCreate);

    assertThat(entry.getName()).isEqualTo(expectedEntryToCreate);
  }

  @Test
  public void testDeleteEntry() throws Exception {
    String entryIdToDelete = "test-entry-" + UUID.randomUUID().toString().substring(0, 8);
    // Create Entry to be deleted
    CreateEntry.createEntry(PROJECT_ID, LOCATION, entryGroupId, entryIdToDelete);

    // No exception means successful call
    DeleteEntry.deleteEntry(PROJECT_ID, LOCATION, entryGroupId, entryIdToDelete);
  }

  @AfterClass
  // Clean-up code that will be executed after all tests
  public static void tearDown() throws Exception {
    // Clean-up Entry Group resource created in setUp()
    // Entry inside this Entry Group will be deleted automatically
    DeleteEntryGroup.deleteEntryGroup(PROJECT_ID, LOCATION, entryGroupId);
  }
}
