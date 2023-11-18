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

import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class DockerComposeTestsIT {
    Logger logger = LoggerFactory.getLogger(getClass());

    @ClassRule
    public static ComposeContainer environment = new ComposeContainer(
            new File("docker-compose.yaml"))
            .withEnv("USERID", System.getenv("USERID"))
            .withEnv("GOOGLE_CLOUD_PROJECT", System.getenv("GOOGLE_CLOUD_PROJECT"))
            .withEnv("GOOGLE_APPLICATION_CREDENTIALS",
                    System.getenv("GOOGLE_APPLICATION_CREDENTIALS"))
            .withTailChildContainers(true)
            .withExposedService("app", 8080)
            .waitingFor("app", Wait.forHttp("/multi"))
            .withBuild(true);

    @Test
    public void testApp() throws InterruptedException {
        // Let the docker compose app run until some spans/logs/metrics are sent to
        // GCP
        Thread.sleep(30_000);
    }
}
