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

package com.example.appengine;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main {

  public static void main(String[] args) throws IOException {
    // Create an instance of HttpServer bound to port defined by the 
    // PORT environment variable when present, otherwise on 8080.
    int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

    // Set root URI path.
    server.createContext("/", (var t) -> {
      byte[] response = "Hello World!".getBytes();
      t.sendResponseHeaders(200, response.length);
      try (OutputStream os = t.getResponseBody()) {
        os.write(response);
      }
    });

    // Create a second URI path.
    server.createContext("/foo", (var t) -> {
      byte[] response = "Foo!".getBytes();
      t.sendResponseHeaders(200, response.length);
      try (OutputStream os = t.getResponseBody()) {
        os.write(response);
      }
    });

    server.start();
  }
}
