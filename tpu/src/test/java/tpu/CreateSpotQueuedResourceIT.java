package tpu;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.tpu.v2.Node;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 25, unit = TimeUnit.MINUTES)
public class CreateSpotQueuedResourceIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "europe-west4-a";
  static String javaVersion = System.getProperty("java.version").substring(0, 2);
  private static final String NODE_NAME = "test-tpu-queued-resource-spot-" + javaVersion + "-"
      + UUID.randomUUID().toString().substring(0, 8);
  private static final String TPU_TYPE = "v2-8";
  private static final String TPU_SOFTWARE_VERSION = "tpu-vm-tf-2.14.1";
  private static final String QUEUED_RESOURCE_NAME = "queued-resource-spot-" + javaVersion + "-"
      + UUID.randomUUID().toString().substring(0, 8);

  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp() throws IOException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    // Cleanup existing stale resources.
    Util.cleanUpExistingQueuedResources("queued-resource-spot-", PROJECT_ID, ZONE);
  }

  @AfterAll
  public static void cleanup() throws IOException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    DeleteForceQueuedResource.deleteForceQueuedResource(PROJECT_ID, ZONE, QUEUED_RESOURCE_NAME);

    // Test that resources are deleted
    assertThat(stdOut.toString()).contains("Deleted Queued Resource:");

    stdOut.close();
    System.setOut(out);
  }

  @Test
  public void testGetSpotQueuedResource() throws Exception {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    CreateSpotQueuedResource.createQueuedResource(
        PROJECT_ID,
        ZONE,
        QUEUED_RESOURCE_NAME,
        NODE_NAME,
        TPU_TYPE,
        TPU_SOFTWARE_VERSION);
    TimeUnit.MINUTES.sleep(10);

    Node node = GetTpuVm.getTpuVm(PROJECT_ID, ZONE, NODE_NAME);
    assertThat(stdOut.toString()).contains(" Queued Resource created: " + QUEUED_RESOURCE_NAME);
    assertThat(node.getSchedulingConfig().getPreemptible()).isTrue();
    stdOut.close();
    System.setOut(out);
  }
}
