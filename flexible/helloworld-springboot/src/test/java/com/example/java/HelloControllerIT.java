/*
 * Copyright 2015 Google LLC
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

package com.example.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloControllerIT {

  @LocalServerPort private int port;

  private URL base;

  @Autowired private TestRestTemplate template;

  @Test
  @SuppressWarnings("CheckReturnValue")
  public void getHello() throws Exception {
    URL base = new URL("http://localhost:" + port + "/");
    ResponseEntity<String> response = template.getForEntity(base.toString(), String.class);
    assertThat(response.getBody().equals("Hello World!"));
  }

  @Test
  @SuppressWarnings("CheckReturnValue")
  public void getHealth() throws Exception {
    URL base = new URL("http://localhost:" + port + "/_ah/health");
    ResponseEntity<String> response = template.getForEntity(base.toString(), String.class);
    assertThat(response.getBody().equals("Hello World!"));
  }
}
