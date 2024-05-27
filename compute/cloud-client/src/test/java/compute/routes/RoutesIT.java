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

package compute.routes;

import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.compute.v1.Operation;
import com.google.cloud.compute.v1.Route;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@Timeout(value = 10, unit = TimeUnit.MINUTES)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RoutesIT {

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String ROUTE_NAME =
          "route-name-" + UUID.randomUUID().toString().substring(0, 8);

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
            .that(System.getenv(envVarName)).isNotEmpty();
  }

  @BeforeAll
  public static void setUp() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  }

  @Test
  public void stage1_CreateRoute()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    Assert.assertEquals(Operation.Status.DONE, CreateRoute.createRoute(PROJECT_ID, ROUTE_NAME));
  }

  @Test
  public void stage2_ListRoute() throws IOException {
    List<Route> routes = ListRoute.listRoutes(PROJECT_ID);
    Assert.assertNotNull(routes);
    Assert.assertFalse(routes.isEmpty());
    Assert.assertTrue(routes.stream().anyMatch(route -> route.getName().equals(ROUTE_NAME)));
  }

  @Test
  public void stage3_DeleteRoute()
          throws IOException, ExecutionException, InterruptedException, TimeoutException {
    DeleteRoute.deleteRoute(PROJECT_ID, ROUTE_NAME);
    // wait to apply new changes
    Thread.sleep(10000);
    List<Route> routes = ListRoute.listRoutes(PROJECT_ID);
    Assert.assertFalse(routes.stream().anyMatch(route -> route.getName().equals(ROUTE_NAME)));
  }
}
