/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.demo;

import java.io.File;
import org.junit.Test;
import org.testcontainers.containers.ComposeContainer;

public class GcloudAuthIT {
  @Test
  public void testPrintCred() {
    runSingle("printcred");
  }

  @Test
  public void testUploadTrace() {
    runSingle("uploadtrace");
  }

  private void runSingle(String containerName) {
    try (ComposeContainer env =
        new ComposeContainer(new File("docker-compose.yaml"))
            .withEnv("USERID", System.getenv("USERID"))
            .withEnv("GOOGLE_CLOUD_PROJECT", System.getenv("GOOGLE_CLOUD_PROJECT"))
            .withEnv(
                "GOOGLE_APPLICATION_CREDENTIALS", System.getenv("GOOGLE_APPLICATION_CREDENTIALS"))
            .withServices(containerName)
            .withBuild(true)
            .withTailChildContainers(true)) {
      env.start();
    }
  }
}
