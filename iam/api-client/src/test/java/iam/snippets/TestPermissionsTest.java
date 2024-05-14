package iam.snippets;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class TestPermissionsTest {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private ByteArrayOutputStream bout;

  private static void requireEnvVar(String varName) {
    assertNotNull(
            System.getenv(varName),
            String.format("Environment variable '%s' is required to perform these tests.", varName));
  }

  @BeforeClass
  public static void checkRequirementsAndInitServiceAccount() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Before
  public void beforeTest() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @After
  public void tearDown() {
    System.setOut(null);
    bout.reset();
  }

  @Test
  public void testTestPermissions() {
    TestPermissions.testPermissions("projects/" + PROJECT_ID);
    String got = bout.toString();
    assertThat(
        got,
        containsString("Of the permissions listed in the request, the caller has the following: "));
  }
}
