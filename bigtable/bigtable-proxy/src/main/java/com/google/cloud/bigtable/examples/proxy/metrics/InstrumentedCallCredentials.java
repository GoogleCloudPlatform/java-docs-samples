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

import com.google.cloud.bigtable.examples.proxy.channelpool.DataChannel;
import com.google.cloud.bigtable.examples.proxy.core.CallLabels.PrimingKey;
import com.google.cloud.bigtable.examples.proxy.core.ProxyHandler;
import com.google.common.base.Stopwatch;
import io.grpc.CallCredentials;
import io.grpc.CallOptions;
import io.grpc.InternalMayRequireSpecificExecutor;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CallCredentials} decorator that tracks latency for fetching credentials.
 *
 * <p>This expects that all RPCs that use these credentials embed a {@link Tracer} in the {@link
 * io.grpc.CallOptions} using {@link Tracer#injectIntoCallOptions(CallOptions)}.
 *
 * <p>Known callers:
 *
 * <ul>
 *   <li>{@link DataChannel#sendPingAndWarm(PrimingKey)}
 *   <li>{@link ProxyHandler#startCall(ServerCall, Metadata)}
 * </ul>
 */
public class InstrumentedCallCredentials extends CallCredentials
    implements InternalMayRequireSpecificExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(InstrumentedCallCredentials.class);

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
            Duration latency = Duration.ofMillis(stopwatch.elapsed(TimeUnit.MILLISECONDS));
            // Most credentials fetches should very fast because they are cached
            if (latency.compareTo(Duration.ofMillis(1)) >= 1) {
              LOG.debug("Fetching Credentials took {}", latency);
            }
            tracer.onCredentialsFetch(Status.OK, latency);
            applier.apply(headers);
          }

          @Override
          public void fail(Status status) {
            Duration latency = Duration.ofMillis(stopwatch.elapsed(TimeUnit.MILLISECONDS));

            LOG.warn("Failed to fetch Credentials after {}: {}", latency, status);
            tracer.onCredentialsFetch(status, latency);
            applier.fail(status);
          }
        });
  }

  @Override
  public boolean isSpecificExecutorRequired() {
    return specificExecutorRequired;
  }
}
