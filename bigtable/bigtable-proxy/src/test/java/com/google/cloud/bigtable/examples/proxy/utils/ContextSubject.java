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
 */

package com.google.cloud.bigtable.examples.proxy.utils;

import static com.google.common.truth.Truth.assertAbout;

import com.google.common.truth.ComparableSubject;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import io.grpc.Context;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;

public class ContextSubject extends Subject {
  private final Context context;

  public ContextSubject(FailureMetadata metadata, @Nullable Context actual) {
    super(metadata, actual);
    this.context = actual;
  }

  public static Factory<ContextSubject, Context> context() {
    return ContextSubject::new;
  }

  public static ContextSubject assertThat(Context context) {
    return assertAbout(context()).that(context);
  }

  public ComparableSubject<Duration> hasRemainingDeadlineThat() {
    Duration remaining =
        Duration.ofMillis(context.getDeadline().timeRemaining(TimeUnit.MILLISECONDS));

    return check("getDeadline().timeRemaining()").that(remaining);
  }
}
