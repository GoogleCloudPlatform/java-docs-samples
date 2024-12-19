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

package compute.disks;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.AttachDiskInstanceRequest;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Operation.Status;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

@RunWith(JUnit4.class)
@Timeout(value = 1, unit = TimeUnit.MINUTES)
public class InstanceAttachDiskIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ZONE = "us-west1-a";
  private static final String REGION = "us-west1";
  private static final String ATTACHED_DISK = "disk-regional";
  private static final String INSTANCE_NAME = "instance";

  @Test
  public void testAttachRegionalDiskForceAttach()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    try (MockedStatic<InstancesClient> mockedResourcePoliciesClient =
                 mockStatic(InstancesClient.class)) {
      Operation operation = mock(Operation.class);
      InstancesClient mockClient = mock(InstancesClient.class);
      OperationFuture mockFuture = mock(OperationFuture.class);

      mockedResourcePoliciesClient.when(InstancesClient::create).thenReturn(mockClient);
      when(mockClient.attachDiskAsync(any(AttachDiskInstanceRequest.class)))
              .thenReturn(mockFuture);
      when(mockFuture.get(anyLong(), any(TimeUnit.class))).thenReturn(operation);
      when(operation.getStatus()).thenReturn(Status.DONE);

      Status status = AttachRegionalDiskForce
              .attachRegionalDiskForce(PROJECT_ID, ZONE, INSTANCE_NAME, REGION, ATTACHED_DISK);

      verify(mockClient, times(1)).attachDiskAsync(any(AttachDiskInstanceRequest.class));
      verify(mockFuture, times(1)).get(anyLong(), any(TimeUnit.class));
      assertEquals(Status.DONE, status);
    }
  }
}
