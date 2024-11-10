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

package compute.reservation;

import static com.google.cloud.compute.v1.ReservationAffinity.ConsumeReservationType.NO_RESERVATION;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstanceTemplate;
import compute.DeleteInstance;
import compute.DeleteInstanceTemplate;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class WithoutConsumingReservationIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-central1-a";
  static String templateUUID = UUID.randomUUID().toString();
  private static final String INSTANCE_NOT_CONSUME_RESERVATION_NAME =
      "test-instance-not-consume-" + templateUUID;
  private static final String TEMPLATE_NOT_CONSUME_RESERVATION_NAME =
      "test-template-not-consume-"  + templateUUID;
  private static final String MACHINE_TYPE_NAME = "n1-standard-1";
  private static final String SOURCE_IMAGE = "projects/debian-cloud/global/images/family/debian-11";
  private static final String NETWORK_NAME = "default";
  private static final long DISK_SIZE_GD = 10L;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @AfterAll
  public static void cleanup()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    // Delete the instance created for testing.
    DeleteInstance.deleteInstance(PROJECT_ID, ZONE, INSTANCE_NOT_CONSUME_RESERVATION_NAME);
    DeleteInstanceTemplate.deleteInstanceTemplate(
        PROJECT_ID, TEMPLATE_NOT_CONSUME_RESERVATION_NAME);
  }

  @Test
  public void testCreateInstanceNotConsumeReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Instance instance = CreateInstanceWithoutConsumingReservation
        .createInstanceWithoutConsumingReservationAsync(
            PROJECT_ID, ZONE, INSTANCE_NOT_CONSUME_RESERVATION_NAME, MACHINE_TYPE_NAME,
        SOURCE_IMAGE, DISK_SIZE_GD, NETWORK_NAME);

    Assertions.assertNotNull(instance);
    Assertions.assertEquals(NO_RESERVATION.toString(),
        instance.getReservationAffinity().getConsumeReservationType());
  }

  @Test
  public void testCreateTemplateNotConsumeReservation()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    InstanceTemplate template =
        CreateTemplateWithoutConsumingReservation.createTemplateWithoutConsumingReservationAsync(
        PROJECT_ID, TEMPLATE_NOT_CONSUME_RESERVATION_NAME,
            MACHINE_TYPE_NAME, SOURCE_IMAGE);

    Assertions.assertNotNull(template);
    Assertions.assertEquals(NO_RESERVATION.toString(),
        template.getPropertiesOrBuilder().getReservationAffinity().getConsumeReservationType());
  }
}
