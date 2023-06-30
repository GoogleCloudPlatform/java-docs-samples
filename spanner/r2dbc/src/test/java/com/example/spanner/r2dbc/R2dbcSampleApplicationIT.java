/*
 * Copyright 2021 Google Inc.
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

package com.example.spanner.r2dbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.cloud.ServiceOptions;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class R2dbcSampleApplicationIT {

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("project", () -> ServiceOptions.getDefaultProjectId());
    // Spanner DB name limit is 30 characters; cannot end with "-".
    String suffix = UUID.randomUUID().toString().substring(0, 23);
    registry.add("database", () -> "r2dbc-" + suffix);

    assertNotNull(
        "Please provide spanner.test.instance environment variable",
        System.getProperty("spanner.test.instance"));
    registry.add("instance", () -> System.getProperty("spanner.test.instance"));
  }

  @Value("${database}")
  String databaseName;

  @Value("${instance}")
  String instance;

  @Autowired private WebTestClient webTestClient;

  @Autowired DatabaseClient databaseClient;

  DatabaseAdminClient dbAdminClient;

  // setup/teardown cannot be static because then properties will not be injected yet
  @Before
  public void createDatabase() {
    SpannerOptions options = SpannerOptions.newBuilder().build();
    Spanner spanner = options.getService();
    dbAdminClient = spanner.getDatabaseAdminClient();
    dbAdminClient.createDatabase(instance, this.databaseName, Collections.emptyList());
  }

  @After
  public void dropDatabase() {
    dbAdminClient.dropDatabase(instance, this.databaseName);
  }

  @Test
  public void testAllWebEndpoints() {

    // DDL takes time; extend timeout to avoid "Timeout on blocking read" exceptions.
    webTestClient = webTestClient.mutate().responseTimeout(Duration.ofSeconds(240)).build();

    this.webTestClient
        .post()
        .uri("/createTable")
        .exchange()
        .expectBody(String.class)
        .isEqualTo("table NAMES created successfully");

    // initially empty table
    this.webTestClient
        .get()
        .uri("/listRows")
        .exchange()
        .expectBody(String[].class)
        .isEqualTo(new String[0]);

    this.webTestClient
        .post()
        .uri("/addRow")
        .body(Mono.just("Bob"), String.class)
        .exchange()
        .expectBody(String.class)
        .isEqualTo("row inserted successfully");

    AtomicReference<String> uuid = new AtomicReference<>();
    this.webTestClient
        .get()
        .uri("/listRows")
        .exchange()
        .expectBody(Name[].class)
        .consumeWith(
            result -> {
              Name[] names = result.getResponseBody();
              assertEquals("1 row expected", 1, names.length);
              assertEquals("where is Bob?", "Bob", names[0].getName());
              uuid.set(names[0].getUuid());
            });

    this.webTestClient
        .post()
        .uri("/deleteRow")
        .body(Mono.just(uuid.get()), String.class)
        .exchange()
        .expectBody(String.class)
        .isEqualTo("row deleted successfully");

    this.webTestClient
        .post()
        .uri("/deleteRow")
        .body(Mono.just("nonexistent"), String.class)
        .exchange()
        .expectBody(String.class)
        .isEqualTo("row did not exist");

    this.webTestClient
        .post()
        .uri("/dropTable")
        .exchange()
        .expectBody(String.class)
        .isEqualTo("table NAMES dropped successfully");
  }
}
