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

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;

/** A factory pairing of an incoming server call to an outgoing client call. */
public final class ProxyHandler<ReqT, RespT> implements ServerCallHandler<ReqT, RespT> {
  private final Channel channel;

  public ProxyHandler(Channel channel) {
    this.channel = channel;
  }

  @Override
  public ServerCall.Listener<ReqT> startCall(ServerCall<ReqT, RespT> serverCall, Metadata headers) {
    CallOptions callOptions = CallOptions.DEFAULT;

    ClientCall<ReqT, RespT> clientCall =
        channel.newCall(serverCall.getMethodDescriptor(), callOptions);

    CallProxy<ReqT, RespT> proxy = new CallProxy<>(serverCall, clientCall);
    clientCall.start(proxy.clientCallListener, headers);
    serverCall.request(1);
    clientCall.request(1);
    return proxy.serverCallListener;
  }
}
