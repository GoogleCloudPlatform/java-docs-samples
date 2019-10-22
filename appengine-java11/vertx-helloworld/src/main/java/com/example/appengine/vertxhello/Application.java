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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;

public class Application extends AbstractVerticle {

  static String METADATA_HOST = "metadata.google.internal";
  static int METADATA_PORT = 80;
  WebClient webClient;

  @Override
  public void start(Future<Void> startFuture) {
    webClient = WebClient.create(vertx);
    Router router = Router.router(vertx);
    router.route().handler(this::handleDefault);

    // Get the PORT environment variable for the server object to listen on
    int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(port, ar -> startFuture.handle(ar.mapEmpty()));
  }

  private void handleDefault(RoutingContext routingContext) {
    webClient
        .get(METADATA_PORT, METADATA_HOST, "/computeMetadata/v1/project/project-id")
        .putHeader("Metadata-Flavor", "Google")
        .expect(ResponsePredicate.SC_OK)
        .send(
            res -> {
              if (res.succeeded()) {
                HttpResponse<Buffer> response = res.result();
                routingContext
                    .response()
                    .putHeader("content-type", "text/html")
                    .end("Hello World! from " + response.body());
              } else {
                routingContext.fail(res.cause());
              }
            });
  }
}
