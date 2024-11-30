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

package com.google.cloud.bigtable.examples.proxy.metrics;

import com.google.common.base.Stopwatch;
import io.grpc.CallCredentials;
import io.grpc.InternalMayRequireSpecificExecutor;
import io.grpc.Metadata;
import io.grpc.Status;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

public class InstrumentedCallCredentials extends CallCredentials
    implements InternalMayRequireSpecificExecutor {
  private final CallCredentials inner;
  private final boolean specificExecutorRequired;

  public InstrumentedCallCredentials(CallCredentials inner) {
    this.inner = inner;
    this.specificExecutorRequired =
        (inner instanceof InternalMayRequireSpecificExecutor)
            && ((InternalMayRequireSpecificExecutor) inner).isSpecificExecutorRequired();
  }

  @Override
  public void applyRequestMetadata(
      RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
    @Nullable Tracer tracer = Tracer.extractTracerFromCallOptions(requestInfo.getCallOptions());
    if (tracer == null) {
      applier.fail(
          Status.INTERNAL.withDescription(
              "InstrumentedCallCredentials failed to extract tracer from CallOptions"));
      return;
    }
    final Stopwatch stopwatch = Stopwatch.createStarted();

    inner.applyRequestMetadata(
        requestInfo,
        appExecutor,
        new MetadataApplier() {
          @Override
          public void apply(Metadata headers) {
            tracer.onCredentialsFetch(
                Status.OK, Duration.ofMillis(stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            applier.apply(headers);
          }

          @Override
          public void fail(Status status) {
            tracer.onCredentialsFetch(
                status, Duration.ofMillis(stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            applier.fail(status);
          }
        });
  }

  @Override
  public boolean isSpecificExecutorRequired() {
    return specificExecutorRequired;
  }
}
