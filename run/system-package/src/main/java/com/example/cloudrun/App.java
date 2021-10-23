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

import static spark.Spark.get;
import static spark.Spark.port;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class App {
  public static void main(String[] args) {
    int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    port(port);
    // [START cloudrun_system_package_handler]
    // [START run_system_package_handler]
    get(
        "/diagram.png",
        (req, res) -> {
          InputStream image = null;
          try {
            String dot = req.queryParams("dot");
            image = createDiagram(dot);
            res.header("Content-Type", "image/png");
            res.header("Content-Length", Integer.toString(image.available()));
            res.header("Cache-Control", "public, max-age=86400");
          } catch (Exception e) {
            if (e.getMessage().contains("syntax")) {
              res.status(400);
              return String.format("Bad Request: %s", e.getMessage());
            } else {
              res.status(500);
              return "Internal Server Error";
            }
          }
          return image;
        });
    // [END run_system_package_handler]
    // [END cloudrun_system_package_handler]
  }

  // [START cloudrun_system_package_exec]
  // [START run_system_package_exec]
  // Generate a diagram based on a graphviz DOT diagram description.
  public static InputStream createDiagram(String dot) {
    if (dot == null || dot.isEmpty()) {
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
      // The Graphviz dot program reads from stdin.
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
  // [END cloudrun_system_package_exec]
}
