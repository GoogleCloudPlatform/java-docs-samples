/*
 * Copyright 2024 Google LLC
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
 *
 *
 * Create features in bulk for an existing entity type. See
 * https://cloud.google.com/vertex-ai/docs/featurestore/setup
 * before running the code snippet
 */

package com.example;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public final class OptimizeToursTest {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");

  @Test
  public void optimizeTours_success() throws Exception {
    assertThat(OptimizeTours.optimizeTours(PROJECT_ID).hasMetrics()).isTrue();
  }
}
