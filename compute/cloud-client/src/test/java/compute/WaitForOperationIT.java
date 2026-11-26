/*
 * Copyright 2026 Google LLC
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
import static com.google.common.truth.Truth.assertWithMessage;
import static compute.Util.getZone;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.compute.v1.Disk;
import com.google.cloud.compute.v1.DisksClient;
import com.google.cloud.compute.v1.InsertDiskRequest;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import com.google.cloud.compute.v1.ZoneOperationsClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Integration tests for {@link WaitForOperation}.
 *
 * <p>Environment variables for local execution:
 *
 * <ul>
 *   <li>{@code GOOGLE_CLOUD_PROJECT} (required): Google Cloud project ID with Compute Engine API
 *       enabled.
 *   <li>{@code GOOGLE_APPLICATION_CREDENTIALS} (optional if Application Default Credentials are
 *       configured via {@code gcloud auth application-default login}): Path to service account
 *       credentials JSON.
 *   <li>{@code JAVA_DOCS_COMPUTE_TEST_ZONES} (optional): Comma-separated Compute Engine zones used
 *       by {@link Util#getZone()} (defaults to {@code us-central1-a,us-west1-a,asia-south1-a}).
 * </ul>
 */
@Timeout(value = 5, unit = TimeUnit.MINUTES)
public class WaitForOperationIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = getZone();
  private static final String DISK_NAME_PREFIX = "operation-create-zonal-disk-";

  private ByteArrayOutputStream stdOut;
  private PrintStream originalOut;

  /**
   * Auto-closing fixture that provisions a temporary zonal disk for a test and ensures the disk is
   * deleted on close after creation settles.
   */
  private static final class TempZonalDisk implements AutoCloseable {
    private final String projectId;
    private final String zone;
    private final String diskName;
    private final Operation initialOperation;

    private TempZonalDisk(
        String projectId, String zone, String diskName, Operation initialOperation) {
      this.projectId = projectId;
      this.zone = zone;
      this.diskName = diskName;
      this.initialOperation = initialOperation;
    }

    /**
     * Initiates creation of a temporary 10 GB standard zonal disk without waiting for completion.
     *
     * @param projectId Google Cloud project ID.
     * @param zone Compute Engine zone in which to create the disk.
     * @return a handle holding the initial creation {@link Operation}.
     */
    static TempZonalDisk create(String projectId, String zone) throws IOException {
      String diskName = DISK_NAME_PREFIX + UUID.randomUUID().toString().substring(0, 8);
      String diskType = String.format("zones/%s/diskTypes/pd-standard", zone);

      try (DisksClient disksClient = DisksClient.create()) {
        Disk disk =
            Disk.newBuilder()
                .setName(diskName)
                .setZone(zone)
                .setType(diskType)
                .setSizeGb(10L)
                .build();
        InsertDiskRequest request =
            InsertDiskRequest.newBuilder()
                .setProject(projectId)
                .setZone(zone)
                .setDiskResource(disk)
                .build();
        Operation initialOperation = disksClient.insertCallable().call(request);
        return new TempZonalDisk(projectId, zone, diskName, initialOperation);
      }
    }

    Operation operation() {
      return initialOperation;
    }

    private void waitUntilDone(Duration timeout)
        throws IOException, InterruptedException, TimeoutException {
      Operation current = initialOperation;
      if (current.getStatus() == Status.DONE) {
        return;
      }
      Instant deadline = Instant.now().plus(timeout);
      try (ZoneOperationsClient zoneOperationsClient = ZoneOperationsClient.create()) {
        while (current.getStatus() != Status.DONE) {
          if (Instant.now().isAfter(deadline)) {
            throw new TimeoutException(
                "Timed out waiting for disk operation " + current.getName());
          }
          current = zoneOperationsClient.wait(projectId, zone, current.getName());
          if (current.getStatus() != Status.DONE) {
            TimeUnit.SECONDS.sleep(1);
          }
        }
      }
    }

    /** Waits for disk creation to finish if still in progress, then deletes the temporary disk. */
    @Override
    public void close() throws Exception {
      Exception waitError = null;
      boolean interrupted = false;
      try {
        waitUntilDone(Duration.ofMinutes(3));
      } catch (InterruptedException e) {
        interrupted = true;
        waitError = e;
      } catch (Exception e) {
        waitError = e;
      }

      try (DisksClient disksClient = DisksClient.create()) {
        disksClient.deleteAsync(projectId, zone, diskName).get(3, TimeUnit.MINUTES);
      } catch (ExecutionException e) {
        if (!(e.getCause() instanceof NotFoundException)) {
          if (waitError != null) {
            e.addSuppressed(waitError);
          }
          throw e;
        }
      } catch (InterruptedException e) {
        interrupted = true;
        if (waitError != null) {
          e.addSuppressed(waitError);
        }
        throw e;
      } catch (Exception e) {
        if (waitError != null) {
          e.addSuppressed(waitError);
        }
        throw e;
      } finally {
        if (interrupted) {
          Thread.currentThread().interrupt();
        }
      }

      if (waitError != null) {
        throw waitError;
      }
    }
  }

  private static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s'", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeAll
  public static void checkEnvironment() {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @BeforeEach
  public void setUp() {
    stdOut = new ByteArrayOutputStream();
    originalOut = System.out;
    System.setOut(new PrintStream(stdOut, true, StandardCharsets.UTF_8));
  }

  @AfterEach
  public void tearDown() {
    System.out.flush();
    System.setOut(originalOut);
  }

  /** Verifies waiting on an in-progress zonal disk creation operation completes with DONE. */
  @Test
  public void testWaitForOperation() throws Exception {
    try (TempZonalDisk disk = TempZonalDisk.create(PROJECT_ID, ZONE)) {
      stdOut.reset();
      WaitForOperation.waitForOperation(PROJECT_ID, disk.operation());

      String output = stdOut.toString(StandardCharsets.UTF_8);
      assertThat(output).contains("Operation Status: DONE");
      assertThat(output).doesNotContain("Error");
    }
  }

  /** Verifies passing an already-completed zonal operation prints DONE without re-waiting. */
  @Test
  public void testWaitForOperationAlreadyDone() throws IOException {
    Operation doneOperation =
        Operation.newBuilder()
            .setName("non-existent-done-operation-" + UUID.randomUUID())
            .setZone(
                String.format(
                    "https://www.googleapis.com/compute/v1/projects/%s/zones/%s",
                    PROJECT_ID, ZONE))
            .setStatus(Status.DONE)
            .build();

    WaitForOperation.waitForOperation(PROJECT_ID, doneOperation);

    String output = stdOut.toString(StandardCharsets.UTF_8);
    assertThat(output).contains("Operation Status: DONE");
    assertThat(output).doesNotContain("Error");
  }

  /** Verifies waiting on a non-existent zonal operation raises {@link NotFoundException}. */
  @Test
  public void testWaitForOperationInvalid() {
    Operation invalidOperation =
        Operation.newBuilder()
            .setName("non-existent-operation-" + UUID.randomUUID())
            .setZone(
                String.format(
                    "https://www.googleapis.com/compute/v1/projects/%s/zones/%s",
                    PROJECT_ID, ZONE))
            .setStatus(Status.RUNNING)
            .build();

    assertThrows(
        NotFoundException.class,
        () -> WaitForOperation.waitForOperation(PROJECT_ID, invalidOperation));
  }
}
