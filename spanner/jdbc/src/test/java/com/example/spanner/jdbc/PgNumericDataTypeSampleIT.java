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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.cloud.ServiceOptions;
import org.junit.Test;

public class PgNumericDataTypeSampleIT extends BaseJdbcPgExamplesIT {

  @Test
  public void testNumericDataType() {
    String out =
        runExample(
            () ->
                PgNumericDataTypeSample.pgNumericDataType(
                    ServiceOptions.getDefaultProjectId(), instanceId, databaseId));

    assertTrue(out, out.contains("Revenues of Venue 1: 3150.25"));
    assertTrue(out, out.contains("Revenues of Venue 1 as double: 3150.25"));
    assertTrue(out, out.contains("Revenues of Venue 1 as BigDecimal: 3150.25"));
    assertTrue(out, out.contains("Revenues of Venue 1 as String: 3150.25"));

    assertTrue(out, out.contains("Revenues of Venue 2: null"));
    assertTrue(out, out.contains("Revenues of Venue 2 as double: null"));
    assertTrue(out, out.contains("Revenues of Venue 2 as BigDecimal: null"));
    assertTrue(out, out.contains("Revenues of Venue 2 as String: null"));

    assertTrue(out, out.contains("Revenues of Venue 3: NaN"));
    assertTrue(out, out.contains("Revenues of Venue 3 as double: NaN"));
    assertFalse(out, out.contains("Revenues of Venue 3 as BigDecimal:"));
    assertTrue(out, out.contains("Revenues of Venue 3 as String: NaN"));

    assertTrue(out, out.contains("Inserted 2 Venues using mutations"));
  }
}