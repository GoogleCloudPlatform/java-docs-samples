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

import com.google.cloud.dataplex.v1.AspectTypeName;
import com.google.cloud.dataplex.v1.LocationName;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class UpdateAspectTypeIT {

  private static final String ID = UUID.randomUUID().toString().substring(0, 8);
  private static final String LOCATION = "us-central1";
  private static String aspectTypeId;
  private static ByteArrayOutputStream bout;
  private static PrintStream originalPrintStream;

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
    // Create temporary aspect type for testing purpose
    aspectTypeId = "test-aspect-type" + ID;
    LocationName locationName = LocationName.of(PROJECT_ID, LOCATION);
    CreateAspectType.createAspectType(locationName, aspectTypeId);
    // Redirect print statements
    bout = new ByteArrayOutputStream();
    originalPrintStream = System.out;
    System.setOut(new PrintStream(bout));
  }

  @AfterClass
  public static void tearDown() throws Exception {
    // Restore print statements
    System.out.flush();
    System.setOut(originalPrintStream);
    // Clean-up previously created aspect type
    AspectTypeName aspectTypeName = AspectTypeName.of(PROJECT_ID, LOCATION, aspectTypeId);
    DeleteAspectType.deleteAspectType(aspectTypeName);
  }

  @Test
  public void testUpdateAspectType() throws Exception {
    String expectedAspectType =
        String.format(
            "projects/%s/locations/%s/aspectTypes/%s", PROJECT_ID, LOCATION, aspectTypeId);
    AspectTypeName aspectTypeName = AspectTypeName.of(PROJECT_ID, LOCATION, aspectTypeId);
    UpdateAspectType.updateAspectType(aspectTypeName);
    assertThat(bout.toString())
        .isEqualTo("Successfully updated aspect type: " + expectedAspectType + "\n");
  }
}
