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

package com.example;

abstract class JobsExample {
  // [START cloudrun_jobs_env_vars]
  // These values are provided automatically by the Cloud Run Jobs runtime.
  private static String TASK_NUM = System.getenv().getOrDefault("TASK_NUM", "0");
  private static String ATTEMPT_NUM = System.getenv().getOrDefault("ATTEMPT_NUM", "0");

  // User-provided environment variables
  private static int SLEEP_MS = Integer.parseInt(System.getenv().getOrDefault("SLEEP_MS", "0"));
  private static float FAIL_RATE =
      Float.parseFloat(System.getenv().getOrDefault("FAIL_RATE", "0.0"));
  // [END cloudrun_jobs_env_vars]

  // Start script
  public static void main(String[] args) {
    System.out.println(String.format("Starting Task #%s, Attempt #%s...", TASK_NUM, ATTEMPT_NUM));
    try {
      runTask(SLEEP_MS, FAIL_RATE);
    } catch (RuntimeException | InterruptedException e) {
      System.err.println(String.format("Task #%s, Attempt #%s failed.", TASK_NUM, ATTEMPT_NUM));
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
    System.out.println(String.format("Completed Task #%s", TASK_NUM));
  }
}
