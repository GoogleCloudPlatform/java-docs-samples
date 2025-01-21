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

package com.google.cloud.bigtable.examples.proxy.core;

import com.google.cloud.bigtable.examples.proxy.metrics.Metrics;
import com.google.cloud.bigtable.examples.proxy.metrics.Tracer;
import io.grpc.CallCredentials;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;

/** A factory pairing of an incoming server call to an outgoing client call. */
public final class ProxyHandler<ReqT, RespT> implements ServerCallHandler<ReqT, RespT> {
  private static final Metadata.Key<String> AUTHORIZATION_KEY =
      Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

  private final Metrics metrics;
  private final Channel channel;
  private final CallCredentials callCredentials;

  public ProxyHandler(Metrics metrics, Channel channel, CallCredentials callCredentials) {
    this.metrics = metrics;
    this.channel = channel;
    this.callCredentials = callCredentials;
  }

  @Override
  public ServerCall.Listener<ReqT> startCall(ServerCall<ReqT, RespT> serverCall, Metadata headers) {
    CallLabels callLabels = CallLabels.create(serverCall.getMethodDescriptor(), headers);
    Tracer tracer = new Tracer(metrics, callLabels);

    // Inject proxy credentials
    CallOptions callOptions = CallOptions.DEFAULT.withCallCredentials(callCredentials);
    callOptions = tracer.injectIntoCallOptions(callOptions);

    // Strip incoming credentials
    headers.removeAll(AUTHORIZATION_KEY);

    ClientCall<ReqT, RespT> clientCall =
        channel.newCall(serverCall.getMethodDescriptor(), callOptions);

    CallProxy<ReqT, RespT> proxy = new CallProxy<>(tracer, serverCall, clientCall);
    clientCall.start(proxy.clientCallListener, headers);
    serverCall.request(1);
    clientCall.request(1);
    return proxy.serverCallListener;
  }
}
