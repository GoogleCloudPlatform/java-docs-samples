/*
 * Copyright 2022 Google Inc.
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

public class PgOrderNullsSampleIT extends BaseJdbcPgExamplesIT {

  @Test
  public void testOrderNulls() {
    String out =
        runExample(
            () ->
                PgOrderNullsSample.pgOrderNulls(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertTrue(out, out.contains("Singers ORDER BY Name\n\tAlice\n\tBruce\n\t<null>"));
    assertTrue(out, out.contains("Singers ORDER BY Name DESC\n\t<null>\n\tBruce\n\tAlice"));
    assertTrue(out, out.contains("Singers ORDER BY Name NULLS FIRST\n\t<null>\n\tAlice\n\tBruce"));
    assertTrue(
        out, out.contains("Singers ORDER BY Name DESC NULLS LAST\n\tBruce\n\tAlice\n\t<null>"));
  }
}