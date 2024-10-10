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

import com.google.cloud.dataplex.v1.AspectType;
import com.google.cloud.dataplex.v1.AspectTypeName;
import com.google.cloud.dataplex.v1.LocationName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AspectTypeIT {
  private static final String ID = UUID.randomUUID().toString().substring(0, 8);
  private static final String LOCATION = "us-central1";
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
  // Set-up code that will be executed before all tests
  public static void setUp() throws Exception {
    String aspectTypeId = "test-aspect-type" + ID;
    locationName = LocationName.of(PROJECT_ID, LOCATION);
    aspectTypeName = AspectTypeName.of(PROJECT_ID, LOCATION, aspectTypeId);
    expectedAspectType =
        String.format(
            "projects/%s/locations/%s/aspectTypes/%s", PROJECT_ID, LOCATION, aspectTypeId);
    // Create Aspect Type resource that will be used in tests for "get", "list" and "update" methods
    CreateAspectType.createAspectType(locationName, aspectTypeId, new ArrayList<>());
  }

  @Test
  public void listAspectTypes_returnsListContainingAspectTypeCreatedInSetUp() throws IOException {
    List<AspectType> aspectTypes = ListAspectTypes.listAspectTypes(locationName);
    assertThat(aspectTypes.stream().map(AspectType::getName)).contains(expectedAspectType);
  }

  @Test
  public void getAspectType_returnsAspectTypeCreatedInSetUp() throws IOException {
    AspectType aspectType = GetAspectType.getAspectType(aspectTypeName);
    assertThat(aspectType.getName()).isEqualTo(expectedAspectType);
  }

  @Test
  public void updateAspectType_returnsUpdatedAspectType() throws Exception {
    AspectType aspectType = UpdateAspectType.updateAspectType(aspectTypeName, new ArrayList<>());
    assertThat(aspectType.getName()).isEqualTo(expectedAspectType);
  }

  @Test
  public void createAspectType_returnsCreatedAspectType() throws Exception {
    String aspectTypeIdToCreate = "test-aspect-type" + UUID.randomUUID().toString().substring(0, 8);
    AspectTypeName aspectTypeNameToCreate =
        AspectTypeName.of(PROJECT_ID, LOCATION, aspectTypeIdToCreate);
    String expectedAspectTypeToCreate =
        String.format(
            "projects/%s/locations/%s/aspectTypes/%s", PROJECT_ID, LOCATION, aspectTypeIdToCreate);

    AspectType aspectType =
        CreateAspectType.createAspectType(locationName, aspectTypeIdToCreate, new ArrayList<>());
    // Clean-up created Aspect Type
    DeleteAspectType.deleteAspectType(aspectTypeNameToCreate);

    assertThat(aspectType.getName()).isEqualTo(expectedAspectTypeToCreate);
  }

  @Test
  public void deleteAspectType_executesTheCallWithoutException() throws Exception {
    String aspectTypeIdToDelete = "test-aspect-type" + UUID.randomUUID().toString().substring(0, 8);
    AspectTypeName aspectTypeNameToDelete =
        AspectTypeName.of(PROJECT_ID, LOCATION, aspectTypeIdToDelete);
    // Create Aspect Type to be deleted
    CreateAspectType.createAspectType(locationName, aspectTypeIdToDelete, new ArrayList<>());

    // No exception means successful call
    DeleteAspectType.deleteAspectType(aspectTypeNameToDelete);
  }

  @AfterClass
  // Clean-up code that will be executed after all tests
  public static void tearDown() throws Exception {
    // Clean-up Aspect Type resource created in setUp()
    DeleteAspectType.deleteAspectType(aspectTypeName);
  }
}
