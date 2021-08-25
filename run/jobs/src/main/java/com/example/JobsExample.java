/*
 * Copyright 2020 Google LLC
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

// [START cloudrun_helloworld_service]
// [START run_helloworld_service]

package com.example;

abstract class JobsExample {

  // These values are provided automatically by the Cloud Run Jobs runtime.
  private static int TASK_NUM = Integer.parseInt(
      getenvWithDefault("TASK_NUM", "0"));
  private static int ATTEMPT_NUM = Integer.parseInt(
      getenvWithDefault("ATTEMPT_NUM", "0"));

  // User-provided environment variables
  private static int SLEEP_MS = Integer.parseInt(
      getenvWithDefault("SLEEP_MS", "0"));
  private static double FAIL_RATE = Double.parseDouble(
      getenvWithDefault("FAIL_RATE", "0.0"));

  // Helper function that fetches an environment variable.
  // If the variable is unset, it returns a default value.
  static String getenvWithDefault(String varName, String defaultValue) {
    if (System.getenv(varName) != null) {
      return System.getenv(varName);
    } else {
      return defaultValue;
    }
  }

  static void runJob(double failRate) throws InterruptedException {

    System.out.println(String.format(
        "Starting Task %s, Attempt %s...", TASK_NUM, ATTEMPT_NUM));

    // Simulate work
    if (SLEEP_MS > 0) {
      Thread.sleep(SLEEP_MS);
    }

    // Simulate errors
    if (failRate > 0) {
      randomFailure(failRate);
    }

    System.out.println(String.format("Completed Task %s", TASK_NUM));
  }

  static void randomFailure(double failureRate) {
    if (failureRate < 0 || failureRate > 1) {
      System.err.println(String.format(
          "Invalid FAIL_RATE value: %s. Must be a float between 0 and 1 inclusive.",
          failureRate
      ));
    }

    if (Math.random() < failureRate) {
      String error = String.format(
          "Task %s, Attempt %s failed.", TASK_NUM, ATTEMPT_NUM);
      System.err.println(error);
      throw new RuntimeException(error);
    }
  }

  // Start script
  public static void main(String[] args) {
    try {
      runJob(FAIL_RATE);
    } catch (RuntimeException | InterruptedException e) {
      // Catch error and denote process-level failure to retry Task
      System.exit(1);
    }
  }
}
