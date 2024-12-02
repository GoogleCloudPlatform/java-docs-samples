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

import com.google.cloud.bigtable.examples.proxy.core.CallLabels;
import com.google.cloud.bigtable.examples.proxy.metrics.Metrics.MetricsAttributes;
import com.google.common.base.Stopwatch;
import io.grpc.CallOptions;
import io.grpc.CallOptions.Key;
import io.grpc.ClientStreamTracer;
import io.grpc.Metadata;
import io.grpc.Status;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tracer extends ClientStreamTracer {
  private static final Key<Tracer> CALL_OPTION_KEY = Key.create("bigtable-proxy-tracer");

  private static final Metadata.Key<String> SERVER_TIMING_HEADER_KEY =
      Metadata.Key.of("server-timing", Metadata.ASCII_STRING_MARSHALLER);
  private static final Pattern SERVER_TIMING_HEADER_PATTERN = Pattern.compile(".*dur=(?<dur>\\d+)");

  private final Metrics metrics;
  private final CallLabels callLabels;
  private final MetricsAttributes attrs;
  private final Stopwatch stopwatch;
  private volatile Optional<Duration> grpcQueueDuration = Optional.empty();
  private final AtomicLong responseSize = new AtomicLong();

  public Tracer(Metrics metrics, CallLabels callLabels) {
    this.metrics = metrics;
    this.callLabels = callLabels;
    this.attrs = metrics.createAttributes(callLabels);

    stopwatch = Stopwatch.createStarted();

    metrics.recordCallStarted(attrs);
  }

  public CallOptions injectIntoCallOptions(CallOptions callOptions) {
    return callOptions
        .withOption(CALL_OPTION_KEY, this)
        .withStreamTracerFactory(
            new Factory() {
              @Override
              public ClientStreamTracer newClientStreamTracer(StreamInfo info, Metadata headers) {
                return Tracer.this;
              }
            });
  }

  public static Tracer extractTracerFromCallOptions(CallOptions callOptions) {
    return callOptions.getOption(CALL_OPTION_KEY);
  }

  @Override
  public void outboundMessageSent(int seqNo, long optionalWireSize, long optionalUncompressedSize) {
    grpcQueueDuration =
        Optional.of(Duration.of(stopwatch.elapsed(TimeUnit.MICROSECONDS), ChronoUnit.MICROS));
  }

  @Override
  public void outboundUncompressedSize(long bytes) {
    metrics.recordRequestSize(attrs, bytes);
  }

  @Override
  public void inboundUncompressedSize(long bytes) {
    responseSize.addAndGet(bytes);
  }

  @Override
  public void inboundHeaders(Metadata headers) {
    Optional.ofNullable(headers.get(SERVER_TIMING_HEADER_KEY))
        .map(SERVER_TIMING_HEADER_PATTERN::matcher)
        .filter(Matcher::find)
        .map(m -> m.group("dur"))
        .map(Long::parseLong)
        .map(Duration::ofMillis)
        .ifPresentOrElse(
            d -> metrics.recordGfeLatency(attrs, d), () -> metrics.recordGfeHeaderMissing(attrs));
  }

  public void onCallFinished(Status status) {
    grpcQueueDuration.ifPresent(d -> metrics.recordQueueLatency(attrs, d));
    metrics.recordResponseSize(attrs, responseSize.get());
    metrics.recordCallLatency(
        attrs, status, Duration.ofMillis(stopwatch.elapsed(TimeUnit.MILLISECONDS)));
  }

  public void onCredentialsFetch(Status status, Duration duration) {
    metrics.recordCredLatency(attrs, status, duration);
  }

  public CallLabels getCallLabels() {
    return callLabels;
  }
}
