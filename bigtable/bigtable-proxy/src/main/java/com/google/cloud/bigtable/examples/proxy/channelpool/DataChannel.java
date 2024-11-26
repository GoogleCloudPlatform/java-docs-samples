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
import com.google.cloud.bigtable.examples.proxy.metrics.CallLabels;
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
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataChannel extends ManagedChannel {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataChannel.class);

  private final ManagedChannel inner;
  private final Metrics metrics;
  private final ResourceCollector resourceCollector;
  private final CallCredentials callCredentials;
  private final ScheduledFuture<?> antiIdleTask;

  private final AtomicBoolean closed = new AtomicBoolean();

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
    this.metrics = metrics;

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

    antiIdleTask = warmingExecutor.scheduleAtFixedRate(this::warmQuietly, 3, 3, TimeUnit.MINUTES);
    metrics.updateChannelCount(1);
  }

  private void warmQuietly() {
    try {
      warm();
    } catch (RuntimeException e) {
      LOGGER.warn("anti idle ping failed, forcing reconnect", e);
      inner.enterIdle();
    }
  }

  private void warm() {
    List<PingAndWarmRequest> requests = resourceCollector.getRequests();
    if (requests.isEmpty()) {
      return;
    }

    List<ListenableFuture<PingAndWarmResponse>> futures =
        requests.stream().map(this::sendPingAndWarm).collect(Collectors.toList());

    int successCount = 0;
    int failures = 0;
    for (ListenableFuture<PingAndWarmResponse> future : futures) {
      PingAndWarmRequest request = requests.get(successCount + failures);
      try {
        future.get();
        successCount++;
      } catch (ExecutionException e) {
        // All permenant errors are ignored and treated as a success
        // The priming request for that generated the error will be dropped
        if (e.getCause() instanceof StatusRuntimeException) {
          StatusRuntimeException se = (StatusRuntimeException) e.getCause();
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
        }
        LOGGER.warn("Failed to prime channel with request: {}", request, e.getCause());
        failures++;
      } catch (InterruptedException e) {
        throw new RuntimeException("Interrupted while priming channel with request: " + request, e);
      }
    }
    if (successCount < failures) {
      throw new RuntimeException("Most of the priming requests failed");
    }
  }

  private ListenableFuture<PingAndWarmResponse> sendPingAndWarm(PingAndWarmRequest request) {
    CallLabels callLabels =
        CallLabels.create(
            BigtableGrpc.getPingAndWarmMethod(),
            Optional.of("bigtableproxy"),
            Optional.of(request.getName()),
            Optional.of(request.getAppProfileId()));
    Tracer tracer = new Tracer(metrics, callLabels);

    CallOptions callOptions =
        CallOptions.DEFAULT
            .withCallCredentials(callCredentials)
            .withDeadline(Deadline.after(1, TimeUnit.MINUTES));
    callOptions = tracer.injectIntoCallOptions(callOptions);

    ClientCall<PingAndWarmRequest, PingAndWarmResponse> call =
        inner.newCall(BigtableGrpc.getPingAndWarmMethod(), callOptions);

    Metadata metadata = new Metadata();
    metadata.put(
        CallLabels.REQUEST_PARAMS,
        String.format(
            "name=projects/%s/instances/%s",
            URLEncoder.encode(request.getName(), StandardCharsets.UTF_8),
            URLEncoder.encode(request.getAppProfileId(), StandardCharsets.UTF_8)));

    SettableFuture<PingAndWarmResponse> f = SettableFuture.create();
    call.start(
        new Listener<PingAndWarmResponse>() {
          @Override
          public void onMessage(PingAndWarmResponse response) {
            f.set(response);
          }

          @Override
          public void onClose(Status status, Metadata trailers) {
            tracer.onCallFinished(status);

            if (status.isOk()) {
              f.setException(new IllegalStateException("PingAndWarm was missing a response"));
            } else {
              f.setException(status.asRuntimeException());
            }
          }
        },
        metadata);

    return f;
  }

  @Override
  public ManagedChannel shutdown() {
    if (closed.compareAndSet(false, true)) {
      metrics.updateChannelCount(-1);
    }
    antiIdleTask.cancel(true);
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
    if (closed.compareAndSet(false, true)) {
      metrics.updateChannelCount(-1);
    }
    antiIdleTask.cancel(true);
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
}
