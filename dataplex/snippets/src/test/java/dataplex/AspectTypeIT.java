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
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AspectTypeIT {
  private static final String ID = UUID.randomUUID().toString().substring(0, 8);
  private static final String LOCATION = "us-central1";
  private static String aspectTypeId;
  private static ByteArrayOutputStream bout;
  private static PrintStream originalPrintStream;
  private static LocationName locationName;
  private static AspectTypeName aspectTypeName;
  private static String expectedAspectType;

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
  public static void setUp() {
    aspectTypeId = "test-aspect-type" + ID;
    locationName = LocationName.of(PROJECT_ID, LOCATION);
    aspectTypeName = AspectTypeName.of(PROJECT_ID, LOCATION, aspectTypeId);
    expectedAspectType =
        String.format(
            "projects/%s/locations/%s/aspectTypes/%s", PROJECT_ID, LOCATION, aspectTypeId);
    // Redirect print statements
    bout = new ByteArrayOutputStream();
    originalPrintStream = System.out;
    System.setOut(new PrintStream(bout));
  }

  @Test
  public void test1_createAspectTypes() throws Exception {
    CreateAspectType.createAspectType(locationName, aspectTypeId);
    assertThat(bout.toString()).contains("Successfully created aspect type: " + expectedAspectType);
  }

  @Test
  public void test2_updateAspectTypes() throws Exception {
    UpdateAspectType.updateAspectType(aspectTypeName);
    assertThat(bout.toString()).contains("Successfully updated aspect type: " + expectedAspectType);
  }

  @Test
  public void test3_listAspectTypes() throws IOException {
    ListAspectTypes.listAspectTypes(locationName);
    assertThat(bout.toString()).contains("Aspect type name: " + expectedAspectType);
  }

  @Test
  public void test4_getAspectType() throws IOException {
    GetAspectType.getAspectType(aspectTypeName);
    assertThat(bout.toString())
        .contains("Aspect type retrieved successfully: " + expectedAspectType);
  }

  @Test
  public void test5_deleteAspectType() throws Exception {
    DeleteAspectType.deleteAspectType(aspectTypeName);
    assertThat(bout.toString()).contains("Successfully deleted aspect type");
  }

  @AfterClass
  public static void tearDown() {
    // Restore print statements
    System.out.flush();
    System.setOut(originalPrintStream);
  }
}
