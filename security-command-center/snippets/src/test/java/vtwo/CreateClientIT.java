/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vtwo;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.securitycenter.v2.SecurityCenterClient;
import java.io.IOException;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import vtwo.client.CreateClientWithEndpoint;

// Test v2 Create Client samples.
@RunWith(JUnit4.class)
public class CreateClientIT {

  @Test
  public void testCreateClientWithEndpoint() throws IOException {
    Map<String, SecurityCenterClient> clients =
        CreateClientWithEndpoint.createClientWithEndpoint(
            "securitycenter.me-central2.rep.googleapis.com:443");
    assertThat(clients.get("client").getSettings().getEndpoint())
        .isEqualTo("securitycenter.googleapis.com:443");
    assertThat(clients.get("regionalClient").getSettings().getEndpoint())
        .isEqualTo("securitycenter.me-central2.rep.googleapis.com:443");
  }
}
