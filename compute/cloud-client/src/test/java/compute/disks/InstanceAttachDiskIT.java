package compute.disks;

import com.google.cloud.compute.v1.Operation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.google.common.truth.Truth.assertThat;

public class InstanceAttachDiskIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-west1-a";
  private static final String REGION = ZONE.substring(0, ZONE.length() - 2);
  private static String REGIONAL_ATTACHED_DISK = "disk-regional";
  private static String INSTANCE_NAME = "test-disks";

  @Test
  public void testAttachRegionalDiskForceAttach()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Operation.Status status = AttachRegionalDiskForce
            .attachRegionalDiskForce(PROJECT_ID, ZONE, INSTANCE_NAME, REGION, REGIONAL_ATTACHED_DISK);

    assertThat(status).isEqualTo(Operation.Status.DONE);
  }
}
