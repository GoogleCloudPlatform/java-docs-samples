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

public class PgCastDataTypeSampleIT extends BaseJdbcPgExamplesIT {

  @Test
  public void testPgCastDataType() {
    String out =
        runExample(
            () ->
                PgCastDataTypeSample.pgCastDataType(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));
    assertTrue(out, out.contains("String: 1"));
    assertTrue(out, out.contains("Int: 2"));
    assertTrue(out, out.contains("Decimal: 3"));
    assertTrue(out, out.contains("Bytes: NA=="));
    assertTrue(out, out.contains("Float: 5.000000"));
    assertTrue(out, out.contains("Bool: true"));
    assertTrue(out, out.contains("Timestamp: 2021-11-03T09:35:01Z"));
  }
}