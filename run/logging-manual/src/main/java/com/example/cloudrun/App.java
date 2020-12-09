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

import static net.logstash.logback.argument.StructuredArguments.kv;
import static spark.Spark.get;
import static spark.Spark.port;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

  private static final Logger logger = LoggerFactory.getLogger(App.class);
  private static final String project = getProjectId();

  public static void main(String[] args) {
    int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    port(port);

    get(
        "/",
        (req, res) -> {
          // [START cloudrun_manual_logging]
          // [START run_manual_logging]
          // Build structured log messages as an object.
          Object globalLogFields = null;
          // Add log correlation to nest all log messages beneath request log in Log Viewer.
          String traceHeader = req.headers("x-cloud-trace-context");
          if (traceHeader != null && project != null) {
            String trace = traceHeader.split("/")[0];
            globalLogFields =
                kv(
                    "logging.googleapis.com/trace",
                    String.format("projects/%s/traces/%s", project, trace));
          }
          // Create a structured log entry using key value pairs.
          logger.error(
              "This is the default display field.",
              kv("component", "arbitrary-property"),
              kv("severity", "NOTICE"),
              globalLogFields);
          // [END run_manual_logging]
          // [END cloudrun_manual_logging]
          res.status(200);
          return "Hello Logger!";
        });
  }

  // Load the project ID from GCP metadata server.
  public static String getProjectId() {
    OkHttpClient ok =
        new OkHttpClient.Builder()
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .writeTimeout(500, TimeUnit.MILLISECONDS)
            .build();

    String metadataUrl = "http://metadata.google.internal/computeMetadata/v1/project/project-id";
    Request request =
        new Request.Builder().url(metadataUrl).addHeader("Metadata-Flavor", "Google").get().build();

    String project = null;
    try {
      Response response = ok.newCall(request).execute();
      project = response.body().string();
    } catch (IOException e) {
      logger.error("Error getting Project Id", e);
    }
    return project;
  }
}
