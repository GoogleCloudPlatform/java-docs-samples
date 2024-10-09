package compute.tpu;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.tpu.v2.Node;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TpuVmTest {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "europe-west4-a";
  private static final String TPU_VM_NAME = "tpu-name";
  private static final String ACCELERATOR_TYPE = "v2-8";
  private static final String VERSION = "tpu-vm-tf-2.14.1";
  private static final String TPU_VM_PATH_NAME = String.format("projects/%s/locations/%s/nodes/%s", PROJECT_ID, ZONE, TPU_VM_NAME);  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp() throws IOException, ExecutionException, InterruptedException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
//    DeleteTpuVm.deleteTpuVm(PROJECT_ID, ZONE, TPU_VM_NAME);
//    TimeUnit.MINUTES.sleep(3);

  }

  @AfterAll
  public static void cleanup() throws Exception {
//   DeleteTpuVm.deleteTpuVm(PROJECT_ID, ZONE, TPU_VM_NAME);
//    assertNull(GetTpuVm.getTpuVm(PROJECT_ID, ZONE, TPU_VM_NAME));
//    // Test that reservations are deleted
//    Assertions.assertThrows(
//        NotFoundException.class,
//        () -> GetTpuVm.getTpuVm(PROJECT_ID, ZONE, TPU_VM_NAME));
  }

  @Test
  public void firstCreateTpuVmTest() throws IOException, ExecutionException, InterruptedException {
    final PrintStream out = System.out;
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
    CreateTpuVm.createTpuVm(PROJECT_ID, ZONE, TPU_VM_NAME, ACCELERATOR_TYPE, VERSION);
    TimeUnit.MINUTES.sleep(5);

    assertThat(stdOut.toString()).contains("TPU VM created: " + TPU_VM_PATH_NAME);
    stdOut.close();
    System.setOut(out);
  }

  @Test
  public void secondGetTpuVmTest() throws IOException {
    Node node = GetTpuVm.getTpuVm(PROJECT_ID, ZONE, TPU_VM_NAME);
    assertNotNull(node);
    assertThat(node.getName()).isEqualTo(TPU_VM_PATH_NAME);
  }
}