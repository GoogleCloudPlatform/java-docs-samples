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

package com.example.appengine.demos.springboot;

import java.net.URI;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory; 
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import io.restassured.RestAssured;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootExampleApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)

public class SpringBootExampleApplicationTests {

 

    @Value("http://localhost:${local.server.port}${server.contextPath:}/aliens")
    URI aliensUri;

    @Autowired
    private AbstractServletWebServerFactory container;

    @Test
    public void applicationShouldStartWithEmbeddedJetty() {
        Assertions.assertThat(container).isInstanceOf(JettyServletWebServerFactory.class);
    }

    /**
     * Test the actual {@literal /aliens} service with an HTTP GET and make
     * assertions on the JSON response.
     */
    @Test
    public void aliens() {
        // @formatter:off
        RestAssured
            .given()
            .when()
                .get(aliensUri)
            .then()
                .statusCode(200)
                .body("aliens[0].name", Matchers.is("E.T."))
                .body("aliens[0].home", Matchers.is("Home"))
                .body("aliens[1].name", Matchers.is("Marvin the Martian"))
                .body("aliens[1].home", Matchers.is("Mars"));
        // @formatter:on
    }

}
