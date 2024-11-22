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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class QuickstartIT {
  private static final String LOCATION = "us-central1";
  private static final String PROJECT_ID = requireProjectIdEnvVar();
  private static ByteArrayOutputStream bout;
  private static PrintStream originalPrintStream;

  private static String requireProjectIdEnvVar() {
    String value = System.getenv("GOOGLE_CLOUD_PROJECT");
    assertNotNull(
        "Environment variable GOOGLE_CLOUD_PROJECT is required to perform these tests.", value);
    return value;
  }

  @BeforeClass
  public static void setUp() {
    requireProjectIdEnvVar();
    // Re-direct print stream to capture logging
    bout = new ByteArrayOutputStream();
    originalPrintStream = System.out;
    System.setOut(new PrintStream(bout));
  }

  @Test
  public void testQuickstart() {
    List<String> expectedLogs =
        List.of(
            String.format(
                "Step 1: Created aspect type -> projects/%s/locations/global/aspectTypes"
                    + "/dataplex-quickstart-aspect-type",
                PROJECT_ID),
            String.format(
                "Step 2: Created entry type -> projects/%s/locations/global/entryTypes"
                    + "/dataplex-quickstart-entry-type",
                PROJECT_ID),
            String.format(
                "Step 3: Created entry group -> projects/%s/locations/%s/entryGroups"
                    + "/dataplex-quickstart-entry-group",
                PROJECT_ID, LOCATION),
            String.format(
                "Step 4: Created entry -> projects/%s/locations/%s/entryGroups"
                    + "/dataplex-quickstart-entry-group/entries/dataplex-quickstart-entry",
                PROJECT_ID, LOCATION),
            String.format(
                "Step 5: Retrieved entry -> projects/%s/locations/%s/entryGroups"
                    + "/dataplex-quickstart-entry-group/entries/dataplex-quickstart-entry",
                PROJECT_ID, LOCATION),
            // Step 6 - result from Search
            "locations/us-central1/entryGroups/dataplex-quickstart-entry-group"
                + "/entries/dataplex-quickstart-entry",
            "Step 7: Successfully cleaned up resources");

    Quickstart.quickstart(PROJECT_ID, LOCATION);
    String output = bout.toString();

    expectedLogs.forEach(expectedLog -> assertThat(output).contains(expectedLog));
  }

  @AfterClass
  public static void tearDown() {
    // Restore print statements
    System.setOut(originalPrintStream);
    bout.reset();
  }
}
