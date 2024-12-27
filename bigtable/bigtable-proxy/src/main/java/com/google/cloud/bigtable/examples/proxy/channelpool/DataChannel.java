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

package com.google.cloud.bigtable.examples.proxy.channelpool;

import com.google.bigtable.v2.BigtableGrpc;
import com.google.bigtable.v2.PingAndWarmRequest;
import com.google.bigtable.v2.PingAndWarmResponse;
import com.google.cloud.bigtable.examples.proxy.core.CallLabels;
import com.google.cloud.bigtable.examples.proxy.core.CallLabels.PrimingKey;
import com.google.cloud.bigtable.examples.proxy.metrics.Metrics;
import com.google.cloud.bigtable.examples.proxy.metrics.Tracer;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import io.grpc.CallCredentials;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ClientCall.Listener;
import io.grpc.ConnectivityState;
import io.grpc.Deadline;
import io.grpc.ExperimentalApi;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator for a Bigtable data plane connection to add channel warming via PingAndWarm. Channel
 * warming will happen on creation and then every 3 minutes (with jitter).
 */
public class DataChannel extends ManagedChannel {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataChannel.class);

  private static final Metadata.Key<String> GFE_DEBUG_REQ_HEADER =
      Key.of("X-Return-Encrypted-Headers", Metadata.ASCII_STRING_MARSHALLER);
  private static final Metadata.Key<String> GFE_DEBUG_RESP_HEADER =
      Key.of("X-Encrypted-Debug-Headers", Metadata.ASCII_STRING_MARSHALLER);

  private static final Duration WARM_PERIOD = Duration.ofMinutes(3);
  private static final Duration MAX_JITTER = Duration.ofSeconds(10);

  private final Random random = new Random();
  private final ManagedChannel inner;
  private final Metrics metrics;
  private final ResourceCollector resourceCollector;
  private final CallCredentials callCredentials;
  private final ScheduledExecutorService warmingExecutor;
  private volatile ScheduledFuture<?> antiIdleTask;

  private final AtomicBoolean closed = new AtomicBoolean();
  private final Object scheduleLock = new Object();

  public DataChannel(
      ResourceCollector resourceCollector,
      String userAgent,
      CallCredentials callCredentials,
      String endpoint,
      int port,
      ScheduledExecutorService warmingExecutor,
      Metrics metrics) {
    this.resourceCollector = resourceCollector;

    this.callCredentials = callCredentials;
    inner =
        ManagedChannelBuilder.forAddress(endpoint, port)
            .userAgent(userAgent)
            .disableRetry()
            .maxInboundMessageSize(256 * 1024 * 1024)
            .keepAliveTime(30, TimeUnit.SECONDS)
            .keepAliveTimeout(10, TimeUnit.SECONDS)
            .build();

    this.warmingExecutor = warmingExecutor;
    this.metrics = metrics;

    new StateTransitionWatcher().run();

    try {
      warm();
    } catch (RuntimeException e) {
      try {
        inner.shutdown();
      } catch (RuntimeException e2) {
        e.addSuppressed(e2);
      }
      throw e;
    }

    antiIdleTask =
        warmingExecutor.schedule(this::warmTask, nextWarmup().toMillis(), TimeUnit.MILLISECONDS);
    metrics.updateChannelCount(1);
  }

  private Duration nextWarmup() {
    return WARM_PERIOD.minus(
        Duration.ofMillis((long) (MAX_JITTER.toMillis() * random.nextDouble())));
  }

  private void warmTask() {
    try {
      warm();
    } catch (RuntimeException e) {
      LOGGER.warn("anti idle ping failed, forcing reconnect", e);
      inner.enterIdle();
    } finally {
      synchronized (scheduleLock) {
        if (!closed.get()) {
          antiIdleTask =
              warmingExecutor.schedule(
                  this::warmTask, nextWarmup().toMillis(), TimeUnit.MILLISECONDS);
        }
      }
    }
  }

  private void warm() {
    List<PrimingKey> primingKeys = resourceCollector.getPrimingKeys();
    if (primingKeys.isEmpty()) {
      return;
    }

    LOGGER.debug("Warming channel {} with: {}", inner, primingKeys);

    List<ListenableFuture<PingAndWarmResponse>> futures =
        primingKeys.stream().map(this::sendPingAndWarm).collect(Collectors.toList());

    int successCount = 0;
    int failures = 0;
    for (ListenableFuture<PingAndWarmResponse> future : futures) {
      PrimingKey request = primingKeys.get(successCount + failures);
      try {
        future.get();
        successCount++;
      } catch (ExecutionException e) {
        // All permanent errors are ignored and treated as a success
        // The priming request for that generated the error will be dropped
        if (e.getCause() instanceof PingAndWarmException) {
          PingAndWarmException se = (PingAndWarmException) e.getCause();

          switch (se.getStatus().getCode()) {
            case INTERNAL:
            case PERMISSION_DENIED:
            case NOT_FOUND:
            case UNAUTHENTICATED:
              successCount++;
              // drop the priming request for permenant errors
              resourceCollector.evict(request);
              continue;
            default:
              // noop
          }
          LOGGER.warn(
              "Failed to prime channel with request: {}, status: {}, debug response headers: {}",
              request,
              se.getStatus(),
              Optional.ofNullable(se.getDebugHeaders()).orElse("<missing>"));
        } else {
          LOGGER.warn("Unexpected failure priming channel with request: {}", request, e.getCause());
        }

        failures++;
      } catch (InterruptedException e) {
        throw new RuntimeException("Interrupted while priming channel with request: " + request, e);
      }
    }
    if (successCount < failures) {
      throw new RuntimeException("Most of the priming requests failed");
    }
  }

  private ListenableFuture<PingAndWarmResponse> sendPingAndWarm(PrimingKey primingKey) {
    Metadata metadata = primingKey.composeMetadata();
    metadata.put(GFE_DEBUG_REQ_HEADER, "gfe_response_only");
    PingAndWarmRequest request = primingKey.composeProto();
    request = request.toBuilder().setName(request.getName()).build();

    CallLabels callLabels = CallLabels.create(BigtableGrpc.getPingAndWarmMethod(), metadata);
    Tracer tracer = new Tracer(metrics, callLabels);

    CallOptions callOptions =
        CallOptions.DEFAULT
            .withCallCredentials(callCredentials)
            .withDeadline(Deadline.after(1, TimeUnit.MINUTES));
    callOptions = tracer.injectIntoCallOptions(callOptions);

    ClientCall<PingAndWarmRequest, PingAndWarmResponse> call =
        inner.newCall(BigtableGrpc.getPingAndWarmMethod(), callOptions);

    SettableFuture<PingAndWarmResponse> f = SettableFuture.create();
    call.start(
        new Listener<>() {
          String debugHeaders = null;

          @Override
          public void onMessage(PingAndWarmResponse response) {
            if (!f.set(response)) {
              // TODO: set a metric
              LOGGER.warn("PingAndWarm returned multiple responses");
            }
          }

          @Override
          public void onHeaders(Metadata headers) {
            debugHeaders = headers.get(GFE_DEBUG_RESP_HEADER);
          }

          @Override
          public void onClose(Status status, Metadata trailers) {
            tracer.onCallFinished(status);

            if (status.isOk()) {
              f.setException(
                  new PingAndWarmException(
                      "PingAndWarm was missing a response", debugHeaders, trailers, status));
            } else {
              f.setException(
                  new PingAndWarmException("PingAndWarm failed", debugHeaders, trailers, status));
            }
          }
        },
        metadata);
    call.sendMessage(request);
    call.halfClose();
    call.request(Integer.MAX_VALUE);

    return f;
  }

  static class PingAndWarmException extends RuntimeException {

    private final String debugHeaders;
    private final Metadata trailers;
    private final Status status;

    public PingAndWarmException(
        String message, String debugHeaders, Metadata trailers, Status status) {
      super(String.format("PingAndWarm failed, status: " + status));
      this.debugHeaders = debugHeaders;
      this.trailers = trailers;
      this.status = status;
    }

    public String getDebugHeaders() {
      return debugHeaders;
    }

    public Metadata getTrailers() {
      return trailers;
    }

    public Status getStatus() {
      return status;
    }
  }

  @Override
  public ManagedChannel shutdown() {
    final boolean closing;

    synchronized (scheduleLock) {
      closing = closed.compareAndSet(false, true);
      antiIdleTask.cancel(true);
    }
    if (closing) {
      metrics.updateChannelCount(-1);
    }

    return inner.shutdown();
  }

  @Override
  public boolean isShutdown() {
    return inner.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return inner.isTerminated();
  }

  @Override
  public ManagedChannel shutdownNow() {
    final boolean closing;

    synchronized (scheduleLock) {
      closing = closed.compareAndSet(false, true);
      antiIdleTask.cancel(true);
    }

    if (closing) {
      metrics.updateChannelCount(-1);
    }

    return inner.shutdownNow();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return inner.awaitTermination(timeout, unit);
  }

  @ExperimentalApi("https://github.com/grpc/grpc-java/issues/4359")
  @Override
  public ConnectivityState getState(boolean requestConnection) {
    return inner.getState(requestConnection);
  }

  @ExperimentalApi("https://github.com/grpc/grpc-java/issues/4359")
  @Override
  public void notifyWhenStateChanged(ConnectivityState source, Runnable callback) {
    inner.notifyWhenStateChanged(source, callback);
  }

  @ExperimentalApi("https://github.com/grpc/grpc-java/issues/4056")
  @Override
  public void resetConnectBackoff() {
    inner.resetConnectBackoff();
  }

  @ExperimentalApi("https://github.com/grpc/grpc-java/issues/4056")
  @Override
  public void enterIdle() {
    inner.enterIdle();
  }

  @Override
  public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(
      MethodDescriptor<RequestT, ResponseT> methodDescriptor, CallOptions callOptions) {
    Tracer tracer =
        Optional.ofNullable(Tracer.extractTracerFromCallOptions(callOptions))
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "DataChannel failed to extract Tracer from CallOptions"));
    resourceCollector.collect(tracer.getCallLabels());

    return inner.newCall(methodDescriptor, callOptions);
  }

  @Override
  public String authority() {
    return inner.authority();
  }

  class StateTransitionWatcher implements Runnable {
    private ConnectivityState prevState = null;

    @Override
    public void run() {
      if (closed.get()) {
        return;
      }

      ConnectivityState newState = inner.getState(false);
      metrics.recordChannelStateChange(prevState, newState);
      prevState = newState;
      inner.notifyWhenStateChanged(prevState, this);
    }
  }
}
