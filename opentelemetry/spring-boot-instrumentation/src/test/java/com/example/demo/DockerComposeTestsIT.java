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

import static com.google.common.truth.Truth.assertThat;

import com.google.api.client.util.Preconditions;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class DockerComposeTestsIT {
  @ClassRule
  public static ComposeContainer environment =
      new ComposeContainer(new File("docker-compose.yaml"), new File("docker-compose.adc.yaml"))
          .withEnv("USERID", System.getenv("USERID"))
          .withEnv(
              "GOOGLE_CLOUD_PROJECT",
              Preconditions.checkNotNull(System.getenv("GOOGLE_CLOUD_PROJECT")))
          .withEnv(
              "GOOGLE_APPLICATION_CREDENTIALS",
              Preconditions.checkNotNull(System.getenv("GOOGLE_APPLICATION_CREDENTIALS")))
          .withExposedService("app", 8080)
          .withExposedService("otelcol", 8888)
          .waitingFor("app", Wait.forHttp("/multi"))
          .withTailChildContainers(true)
          .withBuild(true);

  @Test
  public void testApp() throws InterruptedException, IOException, URISyntaxException {
    // Let the docker compose app run until some spans/logs/metrics are sent to
    // GCP
    Thread.sleep(60_000);

    WebClient client = WebClient.create();
    String collectorHost = environment.getServiceHost("otelcol", 8888);
    int collectorPromPort = environment.getServicePort("otelcol", 8888);
    URI promUri = new URI("http://" + collectorHost + ":" + collectorPromPort + "/metrics");

    String promText = client.get().uri(promUri).retrieve().bodyToMono(String.class).block();

    // Check the collector's self-observability prometheus metrics to see that RPCs to cloud APIs
    // were successfull. Looking for metric otelcol_grpc_io_client_completed_rpcs with labels
    // grpc_client_method and grpc_client_status and non-zero count.
    for (String clientMethod :
        List.of(
            "google.devtools.cloudtrace.v2.TraceService/BatchWriteSpans",
            "google.logging.v2.LoggingServiceV2/WriteLogEntries",
            "google.monitoring.v3.MetricService/CreateTimeSeries")) {

      Pattern re =
          Pattern.compile(
              "^"
                  + Pattern.quote(
                      "otelcol_grpc_io_client_completed_rpcs{grpc_client_method=\""
                          + clientMethod
                          + "\",grpc_client_status=\"OK\"")
                  + ".+\\} [1-9][0-9]*$",
              Pattern.MULTILINE);
      assertThat(promText).containsMatch(re);
    }
  }
}
