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

// [START cloudrun_broken_service]
// [START run_broken_service]
import static spark.Spark.get;
import static spark.Spark.port;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

  private static final Logger logger = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
    port(port);

    get(
        "/",
        (req, res) -> {
          logger.info("Hello: received request.");
          // [START cloudrun_broken_service_problem]
          // [START run_broken_service_problem]
          String name = System.getenv("NAME");
          if (name == null) {
            // Standard error logs do not appear in Stackdriver Error Reporting.
            System.err.println("Environment validation failed.");
            String msg = "Missing required server parameter";
            logger.error(msg, new Exception(msg));
            res.status(500);
            return "Internal Server Error";
          }
          // [END run_broken_service_problem]
          // [END cloudrun_broken_service_problem]
          res.status(200);
          return String.format("Hello %s!", name);
        });
    // [END run_broken_service]
    // [END cloudrun_broken_service]
    get(
        "/improved",
        (req, res) -> {
          logger.info("Hello: received request.");
          // [START cloudrun_broken_service_upgrade]
          // [START run_broken_service_upgrade]
          String name = System.getenv().getOrDefault("NAME", "World");
          if (System.getenv("NAME") == null) {
            logger.warn(String.format("NAME not set, default to %s", name));
          }
          // [END run_broken_service_upgrade]
          // [END cloudrun_broken_service_upgrade]
          res.status(200);
          return String.format("Hello %s!", name);
        });
    // [START cloudrun_broken_service]
    // [START run_broken_service]
  }
}
// [END run_broken_service]
// [END cloudrun_broken_service]
