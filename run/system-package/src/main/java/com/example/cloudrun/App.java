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
 
package com.example.cloudrun;

import io.javalin.Javalin;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class App {
  public static void main(String[] args) {
    int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    Javalin app = Javalin.create().start(port);

    // [START run_system_package_handler]
    app.get(
        "/diagram.png",
        ctx -> {
          try {
            String dot = ctx.queryParam("dot");
            InputStream image = createDiagram(dot);
            ctx.header("Content-Type", "image/png");
            ctx.header("Content-Length", Integer.toString(image.available()));
            ctx.header("Cache-Control", "public, max-age=86400");
            ctx.result(image);
          } catch (Exception e) {
            System.out.println(e);
            if (e.getMessage().contains("syntax")) {
              ctx.status(400).result(String.format("Bad Request: %s", e.getMessage()));
            } else {
              ctx.status(500).result("Internal Server Error");
            }
          }
        });
    // [END run_system_package_handler]
  }
  // [START run_system_package_exec]
  // Generate a diagram based on a graphviz DOT diagram description.
  public static InputStream createDiagram(String dot) {
    if (dot == null) {
      throw new NullPointerException("syntax: no graphviz definition provided");
    }

    // Adds a watermark to the dot graphic.
    List<String> args = new ArrayList<String>();
    args.add("/usr/bin/dot");
    args.add("-Glabel=\"Made on Cloud Run\"");
    args.add("-Gfontsize=10");
    args.add("-Glabeljust=right");
    args.add("-Glabelloc=bottom");
    args.add("-Gfontcolor=gray");
    args.add("-Tpng");

    StringBuilder output = new StringBuilder();
    InputStream stdout = null;
    try {
      ProcessBuilder pb = new ProcessBuilder(args);
      Process process = pb.start();

      OutputStream stdin = process.getOutputStream();
      stdout = process.getInputStream();

      Writer writer = new OutputStreamWriter(stdin, "UTF-8");
      writer.write(dot);
      writer.close();

      process.waitFor();

    } catch (Exception e) {
      System.out.println(e);
    }
    return stdout;
  }
  // [END run_system_package_exec]
}
