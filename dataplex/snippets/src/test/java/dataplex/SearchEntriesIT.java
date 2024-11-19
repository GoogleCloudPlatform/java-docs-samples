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

public class SearchEntriesIT {
  private static final String ID = UUID.randomUUID().toString().substring(0, 8);
  private static final String LOCATION = "us-central1";
  private static final String entryGroupId = "test-entry-group-" + ID;
  private static final String entryId = "test-entry-" + ID;
  private static final String expectedEntry =
      String.format("locations/%s/entryGroups/%s/entries/%s", LOCATION, entryGroupId, entryId);

  private static final String PROJECT_ID = requireProjectIdEnvVar();

  private static String requireProjectIdEnvVar() {
    String value = System.getenv("GOOGLE_CLOUD_PROJECT");
    assertNotNull(
        "Environment variable GOOGLE_CLOUD_PROJECT is required to perform these tests.", value);
    return value;
  }

  @BeforeClass
  public static void setUp() throws Exception {
    requireProjectIdEnvVar();
    CreateEntryGroup.createEntryGroup(PROJECT_ID, LOCATION, entryGroupId);
    CreateEntry.createEntry(PROJECT_ID, LOCATION, entryGroupId, entryId);
    Thread.sleep(30000);
  }

  @Test
  public void testSearchEntries() throws IOException {
    String query = "name:test-entry- AND description:description AND aspect:generic";
    List<Entry> entries = SearchEntries.searchEntries(PROJECT_ID, query);
    assertThat(
            entries.stream()
                .map(Entry::getName)
                .map(entryName -> entryName.substring(entryName.indexOf("location"))))
        .contains(expectedEntry);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    // Entry inside this Entry Group will be deleted automatically
    DeleteEntryGroup.deleteEntryGroup(PROJECT_ID, LOCATION, entryGroupId);
  }
}
