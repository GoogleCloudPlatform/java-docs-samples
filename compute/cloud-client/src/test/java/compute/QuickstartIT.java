package src.test.java.compute;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class QuickstartIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_PROJECT_ID");
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
    requireEnvVar("GOOGLE_PROJECT_ID");

    ZONE = "us-central1-a";
    MACHINE_NAME = "my-new-test-instance";
    MACHINE_TYPE = "n1-standard-1";
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
    new Quickstart()
        .quickstart(PROJECT_ID, ZONE, MACHINE_NAME, MACHINE_TYPE, SOURCE_IMAGE, DISK_GB);
    assertThat(stdOut.toString()).contains("Successfully completed quickstart ! ! ");
  }

}
