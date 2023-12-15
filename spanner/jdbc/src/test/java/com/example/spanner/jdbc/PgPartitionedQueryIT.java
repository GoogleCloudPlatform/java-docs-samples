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

package com.example.spanner.jdbc;

import static org.junit.Assert.assertTrue;

import com.google.cloud.ServiceOptions;
import org.junit.Test;

public class PgPartitionedQueryIT extends BaseJdbcPgExamplesIT {

  @Override
  protected boolean createTestTable() {
    return true;
  }

  @Test
  public void testPartitionQuery() {
    String out = runExample(() -> PgPartitionQueryExample.partitionQuery(
        ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertOutputContainsAllSingers(out);
  }

  @Test
  public void testAutoPartitionMode() {
    String out = runExample(() -> PgAutoPartitionModeExample.autoPartitionMode(
        ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertOutputContainsAllSingers(out);
  }

  @Test
  public void testDataBoost() {
    String out = runExample(() -> PgDataBoostExample.dataBoost(
        ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertOutputContainsAllSingers(out);
  }

  @Test
  public void testRunPartitionedQuery() {
    String out = runExample(() -> PgRunPartitionedQueryExample.runPartitionedQuery(
        ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertOutputContainsAllSingers(out);
  }

  void assertOutputContainsAllSingers(String out) {
    for (Singer singer : TEST_SINGERS) {
      assertTrue(out + " should contain " + singer.toString(),
          out.contains(singer.toString()));
    }
  }

}
