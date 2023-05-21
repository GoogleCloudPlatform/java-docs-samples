/*
 * Copyright 2023 Google LLC
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

package com.example.prometheus;

// [START monitoring_sli_metrics_prometheus_setup]
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.prometheus.client.Histogram.Timer;
import io.prometheus.client.exporter.MetricsServlet;
// [END monitoring_sli_metrics_prometheus_setup]
import java.io.IOException;
import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class PrometheusApplication {
  // [START monitoring_sli_metrics_prometheus_create_metrics]
  static final Counter requestCount = Counter.build()
      .name("java_request_count").help("total request count").register();
  static final Counter failedRequestCount = Counter.build()
      .name("java_failed_request_count").help("failed request count").register();
  static final Histogram responseLatency = Histogram.build()
      .name("java_response_latency").help("response latencies").register();
  // [END monitoring_sli_metrics_prometheus_create_metrics]

  @RestController
  static class PrometheusController {
    @Autowired
    private Random random;
    @Autowired
    private MetricsServlet metricsServlet;

    @GetMapping("/")
    public ResponseEntity<String> home() throws InterruptedException {
      ResponseEntity<String> response;
      // [START monitoring_sli_metrics_prometheus_latency]
      Timer timer = responseLatency.startTimer();
      // [START monitoring_sli_metrics_prometheus_counts]
      requestCount.inc();
      // fail 10% of the time
      if (random.nextDouble() <= 0.1) {
        failedRequestCount.inc();
        // [END monitoring_sli_metrics_prometheus_counts]
        response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Intentional failure encountered!");
      } else {
        long randomDelayMs = random.nextInt(1000);
        // delay for a bit to vary latency measurement
        Thread.sleep(randomDelayMs);
        response = ResponseEntity.status(HttpStatus.OK)
            .body("Succeeded after " + randomDelayMs + "ms.");
      }
      timer.observeDuration();
      // [END monitoring_sli_metrics_prometheus_latency]
      return response;
    }

    // [START monitoring_sli_metrics_prometheus_metrics_endpoint]
    @GetMapping("/metrics")
    public void metrics(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
      metricsServlet.service(request, response);
    }
    // [END monitoring_sli_metrics_prometheus_metrics_endpoint]
  }

  @Bean
  Random random() {
    return new Random();
  }

  @Bean
  MetricsServlet metricsServlet() {
    return new MetricsServlet(CollectorRegistry.defaultRegistry);
  }

  public static void main(String[] args) {
    SpringApplication.run(PrometheusApplication.class, args);
  }
}
