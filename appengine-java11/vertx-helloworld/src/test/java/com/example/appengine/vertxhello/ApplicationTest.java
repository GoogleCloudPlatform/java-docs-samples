/*
 * Copyright 2019 Google LLC
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

package com.example.appengine.vertxhello;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ApplicationTest {

  private static Vertx vertx;

  @BeforeClass
  public static void setUp(TestContext ctx) {
    vertx = Vertx.vertx();
    startMetadataServer(ctx);
    vertx.deployVerticle(new Application(), ctx.asyncAssertSuccess());
  }

  // Start a mock metadata server
  private static void startMetadataServer(TestContext ctx) {
    Application.METADATA_HOST = "localhost";
    Application.METADATA_PORT = 8081;
    vertx
        .createHttpServer()
        .requestHandler(
            req -> {
              req.response().end("this-is-your-project");
            })
        .listen(8081, ctx.asyncAssertSuccess());
  }

  @AfterClass
  public static void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void test(TestContext ctx) {
    WebClient client = WebClient.create(vertx);
    client
        .get(8080, "localhost", "/")
        .expect(ResponsePredicate.SC_OK)
        .send(
            ctx.asyncAssertSuccess(
                response -> {
                  ctx.assertEquals(
                      "Hello World! from this-is-your-project", response.bodyAsString());
                }));
  }
}
