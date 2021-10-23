/*
 * Copyright 2020 Google LLC
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

package com.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.endpoints.controllers.RepeatController;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {RepeatController.class, RestTemplate.class})
public class RepeatControllerIT {

  @LocalServerPort
  private int port;

  @Autowired
  private RestTemplate restTemplate;

  private static final String ROOT_URI = "http://127.0.0.1:%s";

  private static final String BASE_URI = "/api/v1/repeat";

  private static final String SPRINGDOCS_URI = "/v3/api-docs.yaml";

  @Test
  public void testRepeat_realRequest_shouldReturnExpected() {
    //Given
    String text = "Hello World";
    int times = 2;
    String expected = "Hello World, Hello World!";

    //When
    ResponseEntity<String> actual = this.restTemplate.getForEntity(
        String.format(ROOT_URI, this.port) + BASE_URI
            + String.format("?text=%s&times=%s", text, times),
        String.class);

    //Then
    assertEquals(expected, actual.getBody());
  }

  @Test
  public void testSpringDoc_apiDocs_shouldReturnExpected() {
    //Given
    String expected = this.getExpectedApiDocs();

    //When
    ResponseEntity<String> actual = this.restTemplate.getForEntity(
        String.format(ROOT_URI, this.port) + SPRINGDOCS_URI,
        String.class);

    //Then
    assertEquals(expected, actual.getBody());
  }

  private String getExpectedApiDocs() {
    return String.format("openapi: 3.0.1\n"
        + "info:\n"
        + "  title: OpenAPI definition\n"
        + "  version: v0\n"
        + "servers:\n"
        + "- url: http://127.0.0.1:%s\n"
        + "  description: Generated server url\n"
        + "paths:\n"
        + "  /api/v1/repeat:\n"
        + "    get:\n"
        + "      tags:\n"
        + "      - repeat-controller\n"
        + "      operationId: repeat\n"
        + "      parameters:\n"
        + "      - name: text\n"
        + "        in: query\n"
        + "        required: true\n"
        + "        schema:\n"
        + "          type: string\n"
        + "      - name: times\n"
        + "        in: query\n"
        + "        required: true\n"
        + "        schema:\n"
        + "          type: integer\n"
        + "          format: int32\n"
        + "      responses:\n"
        + "        \"200\":\n"
        + "          description: OK\n"
        + "          content:\n"
        + "            '*/*':\n"
        + "              schema:\n"
        + "                type: string\n"
        + "components: {}\n", this.port);
  }
}
