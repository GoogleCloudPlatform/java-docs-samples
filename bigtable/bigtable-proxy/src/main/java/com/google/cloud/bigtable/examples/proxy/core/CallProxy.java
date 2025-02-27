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

import com.google.cloud.bigtable.examples.proxy.metrics.Tracer;
import com.google.common.base.Stopwatch;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;
import javax.annotation.concurrent.GuardedBy;

/** A per gppc RPC proxy. */
class CallProxy<ReqT, RespT> {

  private final Tracer tracer;
  final RequestProxy serverCallListener;
  final ResponseProxy clientCallListener;

  private final Stopwatch downstreamStopwatch = Stopwatch.createUnstarted();

  /**
   * @param tracer a lifecycle observer to publish metrics.
   * @param serverCall the incoming server call. This will be triggered a customer client.
   * @param clientCall the outgoing call to Bigtable service. This will be created by {@link
   *     ProxyHandler}
   */
  public CallProxy(
      Tracer tracer, ServerCall<ReqT, RespT> serverCall, ClientCall<ReqT, RespT> clientCall) {
    this.tracer = tracer;
    // Listen for incoming request messages and send them to the upstream ClientCall
    // The RequestProxy will respect back pressure from the ClientCall and only request a new
    // message from the incoming rpc when the upstream client call is ready,
    serverCallListener = new RequestProxy(clientCall);

    // Listen from response messages from the upstream ClientCall and relay them to the customer's
    // client. This will respect backpressure and request new messages from the upstream when the
    // customer's client is ready.
    clientCallListener = new ResponseProxy(serverCall);
  }

  /**
   * Back pressure aware message pump of request messages from a customer's downstream client to
   * upstream Bigtable service.
   *
   * <p>Additional messages are requested from the downstream while the upstream's isReady() flag is
   * set. As soon as the upstream signals that is full by returning false for isReady(). {@link
   * RequestProxy} will remember that the need to get more messages from downstream and then wait
   * until the upstream signals readiness via onClientReady().
   *
   * <p>Please note in the current Bigtable protocol, all RPCs a client unary. Until that changes,
   * this proxy will only have a single iteration. However, its designed generically to support
   * future usecases.
   */
  private class RequestProxy extends ServerCall.Listener<ReqT> {

    private final ClientCall<ReqT, ?> clientCall;

    @GuardedBy("this")
    private boolean needToRequest;

    public RequestProxy(ClientCall<ReqT, ?> clientCall) {
      this.clientCall = clientCall;
    }

    @Override
    public void onCancel() {
      clientCall.cancel("Server cancelled", null);
    }

    @Override
    public void onHalfClose() {
      clientCall.halfClose();
    }

    @Override
    public void onMessage(ReqT message) {
      clientCall.sendMessage(message);
      synchronized (this) {
        if (clientCall.isReady()) {
          clientCallListener.serverCall.request(1);
        } else {
          // The outgoing call is not ready for more requests. Stop requesting additional data and
          // wait for it to catch up.
          needToRequest = true;
        }
      }
    }

    @Override
    public void onReady() {
      clientCallListener.onServerReady();
    }

    // Called from ResponseProxy, which is a different thread than the ServerCall.Listener
    // callbacks.
    synchronized void onClientReady() {
      if (needToRequest) {
        // When the upstream client is ready for another request message from the customer's client,
        // ask for one more message.
        clientCallListener.serverCall.request(1);
        needToRequest = false;
      }
    }
  }

  /**
   * Back pressure aware message pump of response messages from upstream Bigtable service to a
   * customer's downstream client.
   *
   * <p>Additional messages are requested from the upstream while the downstream's isReady() flag is
   * set. As soon as the downstream signals that is full by returning false for isReady(). {@link
   * ResponseProxy} will remember that the need to get more messages from upstream and then wait
   * until the downstream signals readiness via onServerReady().
   */
  private class ResponseProxy extends ClientCall.Listener<RespT> {

    private final ServerCall<?, RespT> serverCall;

    @GuardedBy("this")
    private boolean needToRequest;

    public ResponseProxy(ServerCall<?, RespT> serverCall) {
      this.serverCall = serverCall;
    }

    @Override
    public void onClose(Status status, Metadata trailers) {
      tracer.onCallFinished(status);

      serverCall.close(status, trailers);
    }

    @Override
    public void onHeaders(Metadata headers) {
      serverCall.sendHeaders(headers);
    }

    @Override
    public void onMessage(RespT message) {
      serverCall.sendMessage(message);
      synchronized (this) {
        if (serverCall.isReady()) {
          serverCallListener.clientCall.request(1);
        } else {
          // The incoming call is not ready for more responses. Stop requesting additional data
          // and wait for it to catch up.
          needToRequest = true;
          downstreamStopwatch.reset().start();
        }
      }
    }

    @Override
    public void onReady() {
      serverCallListener.onClientReady();
    }

    // Called from RequestProxy, which is a different thread than the ClientCall.Listener
    // callbacks.
    synchronized void onServerReady() {
      if (downstreamStopwatch.isRunning()) {
        tracer.onDownstreamLatency(downstreamStopwatch.elapsed());
        downstreamStopwatch.stop();
      }
      if (needToRequest) {
        serverCallListener.clientCall.request(1);
        needToRequest = false;
      }
    }
  }
}
