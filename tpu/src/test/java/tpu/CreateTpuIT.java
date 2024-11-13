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

package tpu;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.tpu.v2.CreateNodeRequest;
import com.google.cloud.tpu.v2.TpuClient;
import com.google.cloud.tpu.v2.TpuSettings;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class CreateTpuIT {
  private static final String PROJECT_ID = "project-id";
  private static final String ZONE = "asia-east1-c";
  private static final String NODE_NAME = "test-tpu";
  private static final String TPU_TYPE = "v2-8";
  private static final String TPU_SOFTWARE_VERSION = "tpu-vm-tf-2.12.1";

  @Test
  public void testCreateTpuVm() throws Exception {
    TpuClient mockTpuClient = mock(TpuClient.class);
    try (MockedStatic<TpuClient> mockedTpuClient = Mockito.mockStatic(TpuClient.class)) {
      mockedTpuClient.when(() -> TpuClient.create(any(TpuSettings.class)))
          .thenReturn(mockTpuClient);

      OperationFuture mockFuture = mock(OperationFuture.class);
      when(mockTpuClient.createNodeAsync(any(CreateNodeRequest.class)))
          .thenReturn(mockFuture);
      CreateTpuVm.createTpuVm(
          PROJECT_ID, ZONE, NODE_NAME,
          TPU_TYPE, TPU_SOFTWARE_VERSION);

      verify(mockTpuClient, times(1))
          .createNodeAsync(any(CreateNodeRequest.class));
      verify(mockTpuClient, times(1)).close();
    }
  }
}
