/*
 * Copyright 2021 Google LLC
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

// [START cloudrun_jobs_quickstart]
package com.example;

abstract class JobsExample {
  // [START cloudrun_jobs_env_vars]
  // These values are provided automatically by the Cloud Run Jobs runtime.
  private static String CLOUD_RUN_TASK_INDEX = System.getenv().getOrDefault("CLOUD_RUN_TASK_INDEX", "0");
  private static String CLOUD_RUN_TASK_ATTEMPT = System.getenv().getOrDefault("CLOUD_RUN_TASK_ATTEMPT", "0");

  // User-provided environment variables
  private static int SLEEP_MS = Integer.parseInt(System.getenv().getOrDefault("SLEEP_MS", "0"));
  private static float FAIL_RATE =
      Float.parseFloat(System.getenv().getOrDefault("FAIL_RATE", "0.0"));
  // [END cloudrun_jobs_env_vars]

  // Start script
  public static void main(String[] args) {
    System.out.println(String.format("Starting Task #%s, Attempt #%s...", CLOUD_RUN_TASK_INDEX, CLOUD_RUN_TASK_ATTEMPT));
    try {
      runTask(SLEEP_MS, FAIL_RATE);
    } catch (RuntimeException | InterruptedException e) {
      System.err.println(String.format("Task #%s, Attempt #%s failed.", CLOUD_RUN_TASK_INDEX, CLOUD_RUN_TASK_ATTEMPT));
      // [START cloudrun_jobs_exit_process]
      // Catch error and denote process-level failure to retry Task
      System.exit(1);
      // [END cloudrun_jobs_exit_process]
    }
  }

  static void runTask(int sleepTime, float failureRate) throws InterruptedException {
    // Simulate work
    if (sleepTime > 0) {
      Thread.sleep(sleepTime);
    }

    // Simulate errors
    if (failureRate < 0 || failureRate > 1) {
      System.err.println(
          String.format(
              "Invalid FAIL_RATE value: %s. Must be a float between 0 and 1 inclusive.",
              failureRate));
      return;
    }
    if (Math.random() < failureRate) {
      throw new RuntimeException("Task Failed.");
    }
    System.out.println(String.format("Completed Task #%s", CLOUD_RUN_TASK_INDEX));
  }
}
// [END cloudrun_jobs_quickstart]