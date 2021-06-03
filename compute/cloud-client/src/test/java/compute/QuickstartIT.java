/*
 * Copyright 2020 Google LLC
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

package compute;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class QuickstartIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static String ZONE;
  private static String MACHINE_NAME;
  private static String MACHINE_TYPE;
  private static String SOURCE_IMAGE;
  private static String DISK_GB;

  private ByteArrayOutputStream stdOut;

  public static void requireEnvVar(String envVarName) {
    assertNotNull(System.getenv(envVarName));
  }


  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    ZONE = "us-central1-a";
    MACHINE_NAME = "my-new-test-instance" + UUID.randomUUID().toString();
    MACHINE_TYPE = "zones/us-central1-a/machineTypes/n1-standard-1";
    SOURCE_IMAGE = "https://www.googleapis.com/compute/v1/projects/debian-cloud/global/images/debian-7-wheezy-v20150710";
    DISK_GB = "10";
  }


  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @After
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }


  @Test
  public void testQuickstart() throws IOException, InterruptedException {
    new compute.Quickstart()
        .quickstart(PROJECT_ID, ZONE, MACHINE_NAME, MACHINE_TYPE, SOURCE_IMAGE, DISK_GB);
    assertThat(stdOut.toString()).contains("Successfully completed quickstart ! ! ");
  }

}
