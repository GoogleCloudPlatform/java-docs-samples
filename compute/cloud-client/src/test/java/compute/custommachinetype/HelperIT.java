/*
 * Copyright 2022 Google LLC
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

package compute.custommachinetype;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import compute.custommachinetype.HelperClass.CpuSeries;
import compute.custommachinetype.HelperClass.CustomMachineType;
import compute.custommachinetype.HelperClass.Limits;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HelperIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "europe-central2-b";

  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
    stdOut.close();
    System.setOut(out);
  }


  @AfterAll
  public static void cleanup()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    stdOut.close();
    System.setOut(out);
  }

  @BeforeEach
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @AfterEach
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }


  @Test
  public void testCustomMachineTypeGood() {
    CustomMachineType customMachineType = null;

    // N1
    customMachineType = HelperClass.createCustomMachineType(ZONE, CpuSeries.N1.getCpuSeries(), 8192,
        8, Limits.CPUSeries_N1.getTypeLimits());
    assertWithMessage("Error in createCustomMachineType").that(stdOut.toString())
        .doesNotContain("Error in validation: null");
    Assertions.assertTrue(customMachineType.toString()
        .equalsIgnoreCase(String.format("zones/%s/machineTypes/custom-8-8192", ZONE)));
    Assertions.assertTrue(customMachineType.shortString().equalsIgnoreCase("custom-8-8192"));

    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // N2
    customMachineType = HelperClass.createCustomMachineType(ZONE, CpuSeries.N2.getCpuSeries(), 4096,
        4, Limits.CPUSeries_N2.getTypeLimits());
    assertWithMessage("Error in createCustomMachineType").that(stdOut.toString())
        .doesNotContain("Error in validation: null");
    Assertions.assertTrue(customMachineType.toString()
        .equalsIgnoreCase(String.format("zones/%s/machineTypes/n2-custom-4-4096", ZONE)));
    Assertions.assertTrue(customMachineType.shortString().equalsIgnoreCase("n2-custom-4-4096"));

    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // N2D
    customMachineType = HelperClass.createCustomMachineType(ZONE, CpuSeries.N2D.getCpuSeries(),
        8192, 4, Limits.CPUSeries_N2D.getTypeLimits());
    assertWithMessage("Error in createCustomMachineType").that(stdOut.toString())
        .doesNotContain("Error in validation: null");
    Assertions.assertTrue(customMachineType.toString()
        .equalsIgnoreCase(String.format("zones/%s/machineTypes/n2d-custom-4-8192", ZONE)));
    Assertions.assertTrue(customMachineType.shortString().equalsIgnoreCase("n2d-custom-4-8192"));

    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // E2
    customMachineType = HelperClass.createCustomMachineType(ZONE, CpuSeries.E2.getCpuSeries(), 8192,
        8, Limits.CPUSeries_E2.getTypeLimits());
    assertWithMessage("Error in createCustomMachineType").that(stdOut.toString())
        .doesNotContain("Error in validation: null");
    Assertions.assertTrue(customMachineType.toString()
        .equalsIgnoreCase(String.format("zones/%s/machineTypes/e2-custom-8-8192", ZONE)));
    Assertions.assertTrue(customMachineType.shortString().equalsIgnoreCase("e2-custom-8-8192"));

    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // E2SMALL
    customMachineType = HelperClass.createCustomMachineType(ZONE, CpuSeries.E2_SMALL.getCpuSeries(),
        4096, 0, Limits.CPUSeries_E2SMALL.getTypeLimits());
    assertWithMessage("Error in createCustomMachineType").that(stdOut.toString())
        .doesNotContain("Error in validation: null");
    Assertions.assertTrue(customMachineType.toString()
        .equalsIgnoreCase(String.format("zones/%s/machineTypes/e2-custom-small-4096", ZONE)));
    Assertions.assertTrue(customMachineType.shortString().equalsIgnoreCase("e2-custom-small-4096"));

    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // E2MICRO
    customMachineType = HelperClass.createCustomMachineType(ZONE, CpuSeries.E2_MICRO.getCpuSeries(),
        2048, 0, Limits.CPUSeries_E2MICRO.getTypeLimits());
    assertWithMessage("Error in createCustomMachineType").that(stdOut.toString())
        .doesNotContain("Error in validation: null");
    Assertions.assertTrue(customMachineType.toString()
        .equalsIgnoreCase(String.format("zones/%s/machineTypes/e2-custom-micro-2048", ZONE)));
    Assertions.assertTrue(customMachineType.shortString().equalsIgnoreCase("e2-custom-micro-2048"));

    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // E2MEDIUM
    customMachineType = HelperClass.createCustomMachineType(ZONE,
        CpuSeries.E2_MEDIUM.getCpuSeries(), 8192, 0, Limits.CPUSeries_E2MEDIUM.getTypeLimits());
    assertWithMessage("Error in createCustomMachineType").that(stdOut.toString())
        .doesNotContain("Error in validation: null");
    Assertions.assertTrue(customMachineType.toString()
        .equalsIgnoreCase(String.format("zones/%s/machineTypes/e2-custom-medium-8192", ZONE)));
    Assertions.assertTrue(
        customMachineType.shortString().equalsIgnoreCase("e2-custom-medium-8192"));

    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    // N2
    customMachineType = HelperClass.createCustomMachineType(ZONE, CpuSeries.N2.getCpuSeries(),
        638720, 8, Limits.CPUSeries_N2.getTypeLimits());
    assertWithMessage("Error in createCustomMachineType").that(stdOut.toString())
        .doesNotContain("Error in validation: null");
    Assertions.assertTrue(customMachineType.toString()
        .equalsIgnoreCase(String.format("zones/%s/machineTypes/n2-custom-8-638720-ext", ZONE)));
    Assertions.assertTrue(
        customMachineType.shortString().equalsIgnoreCase("n2-custom-8-638720-ext"));
  }

  @Test
  public void testCustomMachineTypeBad() {
    // bad memory 256
    HelperClass.createCustomMachineType(ZONE, CpuSeries.N1.getCpuSeries(), 8194, 8,
        Limits.CPUSeries_N1.getTypeLimits());
    assertThat(stdOut.toString()).contains("Requested memory must be a multiple of 256 MB");

    // wrong cpu count
    HelperClass.createCustomMachineType(ZONE, CpuSeries.N2.getCpuSeries(), 8194, 66,
        Limits.CPUSeries_N2.getTypeLimits());
    String expectedOutput = String.format(
        "Invalid number of cores requested. Allowed number of cores for %s is: %s",
        CpuSeries.N2.getCpuSeries(),
        Arrays.toString(Limits.CPUSeries_N2.getTypeLimits().allowedCores));
    assertThat(stdOut.toString()).contains(expectedOutput);
  }

}
