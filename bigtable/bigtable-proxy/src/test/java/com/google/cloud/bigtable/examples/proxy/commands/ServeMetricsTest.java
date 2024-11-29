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

package com.google.cloud.bigtable.examples.proxy.commands;

import static org.junit.Assert.assertThrows;
import static org.mockito.AdditionalMatchers.geq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.auth.Credentials;
import com.google.bigtable.v2.BigtableGrpc;
import com.google.bigtable.v2.BigtableGrpc.BigtableBlockingStub;
import com.google.bigtable.v2.BigtableGrpc.BigtableImplBase;
import com.google.bigtable.v2.CheckAndMutateRowRequest;
import com.google.bigtable.v2.CheckAndMutateRowResponse;
import com.google.cloud.bigtable.examples.proxy.core.CallLabels;
import com.google.cloud.bigtable.examples.proxy.metrics.Metrics;
import com.google.cloud.bigtable.examples.proxy.metrics.Metrics.MetricsAttributes;
import com.google.common.collect.Lists;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(JUnit4.class)
public class ServeMetricsTest {
  @Rule public final MockitoRule mockitoTestRule = MockitoJUnit.rule();

  @Mock Metrics mockMetrics;

  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule().setTimeout(1, TimeUnit.MINUTES);

  private MetadataInterceptor serverMetadataInterceptor = new MetadataInterceptor();
  @Spy FakeDataService dataService = new FakeDataService();
  @Spy FakeCredentials fakeCredentials = new FakeCredentials();
  private ManagedChannel fakeServiceChannel;
  private Serve serve;
  private ManagedChannel proxyChannel;

  @Before
  public void setUp() throws Exception {
    Server server = grpcCleanup.register(createServer());

    fakeServiceChannel =
        grpcCleanup.register(
            ManagedChannelBuilder.forAddress("localhost", server.getPort()).usePlaintext().build());

    serve = createAndStartCommand(fakeServiceChannel, fakeCredentials, mockMetrics);

    proxyChannel =
        grpcCleanup.register(
            ManagedChannelBuilder.forAddress("localhost", serve.listenPort).usePlaintext().build());
  }

  @After
  public void tearDown() throws Exception {
    if (serve != null) {
      serve.cleanup();
    }
  }

  private Server createServer() throws IOException {
    for (int i = 10; i >= 0; i--) {
      int port;
      try (ServerSocket serverSocket = new ServerSocket(0)) {
        port = serverSocket.getLocalPort();
      }
      try {
        return ServerBuilder.forPort(port)
            .intercept(serverMetadataInterceptor)
            .addService(dataService)
            .build()
            .start();
      } catch (IOException e) {
        if (i == 0) {
          throw e;
        }
      }
    }
    throw new IllegalStateException(
        "Should never happen, if the server could be started it should've been returned or the last"
            + " attempt threw an exception");
  }

  private static Serve createAndStartCommand(
      ManagedChannel targetChannel, FakeCredentials targetCredentials, Metrics metrics)
      throws IOException {
    for (int i = 10; i >= 0; i--) {
      Serve s = new Serve();
      s.dataChannel = targetChannel;
      s.adminChannel = targetChannel;
      s.credentials = targetCredentials;
      s.metrics = metrics;

      try (ServerSocket serverSocket = new ServerSocket(0)) {
        s.listenPort = serverSocket.getLocalPort();
      }

      try {
        s.start();
        return s;
      } catch (IOException e) {
        if (i == 0) {
          throw e;
        }
      }
    }
    throw new IllegalStateException(
        "Should never happen, if the server could be started it should've been returned or the last"
            + " attempt threw an exception");
  }

  @Test
  public void testHappyPath() throws IOException {
    serverMetadataInterceptor.responseHeaders =
        () -> {
          Metadata md = new Metadata();
          md.put(Key.of("server-timing", Metadata.ASCII_STRING_MARSHALLER), "dur=1234");
          return md;
        };

    BigtableBlockingStub stub =
        BigtableGrpc.newBlockingStub(proxyChannel)
            .withInterceptors(
                new ClientInterceptor() {
                  @Override
                  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                      MethodDescriptor<ReqT, RespT> methodDescriptor,
                      CallOptions callOptions,
                      Channel channel) {
                    return new SimpleForwardingClientCall<>(
                        channel.newCall(methodDescriptor, callOptions)) {
                      @Override
                      public void start(Listener<RespT> responseListener, Metadata headers) {
                        // inject call labels
                        headers.put(
                            Key.of("x-goog-request-params", Metadata.ASCII_STRING_MARSHALLER),
                            String.format(
                                "table_name=projects/%s/instances/%s/tables/%s&app_profile_id=%s",
                                "fake-project", "fake-instance", "fake-table", "fake-app-profile"));
                        headers.put(
                            Key.of("x-goog-api-client", Metadata.ASCII_STRING_MARSHALLER),
                            "fake-client");

                        super.start(responseListener, headers);
                      }
                    };
                  }
                });

    MetricsAttributes fakeAttrs = new MetricsAttributes() {};

    doReturn(fakeAttrs).when(mockMetrics).createAttributes(any());
    doAnswer(
            invocation -> {
              Thread.sleep(10);
              return invocation.callRealMethod();
            })
        .when(dataService)
        .checkAndMutateRow(any(), any());

    doAnswer(
            invocation -> {
              Thread.sleep(10);
              return invocation.callRealMethod();
            })
        .when(fakeCredentials)
        .getRequestMetadata(any());

    CheckAndMutateRowRequest request =
        CheckAndMutateRowRequest.newBuilder()
            .setTableName("project/fake-project/instances/fake-instance/tables/fake-table")
            .build();
    CheckAndMutateRowResponse response = stub.checkAndMutateRow(request);

    verify(mockMetrics)
        .createAttributes(
            eq(
                CallLabels.create(
                    BigtableGrpc.getCheckAndMutateRowMethod(),
                    Optional.of("fake-client"),
                    Optional.of("projects/fake-project/instances/fake-instance/tables/fake-table"),
                    Optional.of("fake-app-profile"))));

    verify(mockMetrics).recordCallStarted(eq(fakeAttrs));
    verify(mockMetrics).recordCredLatency(eq(fakeAttrs), eq(Status.OK), geq(Duration.ofMillis(10)));
    verify(mockMetrics).recordGfeLatency(eq(fakeAttrs), eq(Duration.ofMillis(1234)));
    verify(mockMetrics).recordQueueLatency(eq(fakeAttrs), geq(Duration.ZERO));
    verify(mockMetrics).recordRequestSize(eq(fakeAttrs), eq((long) request.getSerializedSize()));
    verify(mockMetrics).recordResponseSize(eq(fakeAttrs), eq((long) response.getSerializedSize()));
    verify(mockMetrics).recordCallLatency(eq(fakeAttrs), eq(Status.OK), geq(Duration.ofMillis(20)));
  }

  @Test
  public void testMissingGfe() throws IOException {
    BigtableBlockingStub stub =
        BigtableGrpc.newBlockingStub(proxyChannel)
            .withInterceptors(
                new ClientInterceptor() {
                  @Override
                  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                      MethodDescriptor<ReqT, RespT> methodDescriptor,
                      CallOptions callOptions,
                      Channel channel) {
                    return new SimpleForwardingClientCall<>(
                        channel.newCall(methodDescriptor, callOptions)) {
                      @Override
                      public void start(Listener<RespT> responseListener, Metadata headers) {
                        // inject call labels
                        headers.put(
                            Key.of("x-goog-request-params", Metadata.ASCII_STRING_MARSHALLER),
                            String.format(
                                "table_name=projects/%s/instances/%s/tables/%s&app_profile_id=%s",
                                "fake-project", "fake-instance", "fake-table", "fake-app-profile"));
                        headers.put(
                            Key.of("x-goog-api-client", Metadata.ASCII_STRING_MARSHALLER),
                            "fake-client");

                        super.start(responseListener, headers);
                      }
                    };
                  }
                });

    MetricsAttributes fakeAttrs = new MetricsAttributes() {};
    doReturn(fakeAttrs).when(mockMetrics).createAttributes(any());

    CheckAndMutateRowRequest request =
        CheckAndMutateRowRequest.newBuilder()
            .setTableName("project/fake-project/instances/fake-instance/tables/fake-table")
            .build();
    CheckAndMutateRowResponse response = stub.checkAndMutateRow(request);

    verify(mockMetrics)
        .createAttributes(
            eq(
                CallLabels.create(
                    BigtableGrpc.getCheckAndMutateRowMethod(),
                    Optional.of("fake-client"),
                    Optional.of("projects/fake-project/instances/fake-instance/tables/fake-table"),
                    Optional.of("fake-app-profile"))));

    verify(mockMetrics).recordGfeHeaderMissing(eq(fakeAttrs));
  }

  @Test
  public void testError() throws IOException {
    final BigtableBlockingStub stub =
        BigtableGrpc.newBlockingStub(proxyChannel)
            .withInterceptors(
                new ClientInterceptor() {
                  @Override
                  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                      MethodDescriptor<ReqT, RespT> methodDescriptor,
                      CallOptions callOptions,
                      Channel channel) {
                    return new SimpleForwardingClientCall<>(
                        channel.newCall(methodDescriptor, callOptions)) {
                      @Override
                      public void start(Listener<RespT> responseListener, Metadata headers) {
                        // inject call labels
                        headers.put(
                            Key.of("x-goog-request-params", Metadata.ASCII_STRING_MARSHALLER),
                            String.format(
                                "table_name=projects/%s/instances/%s/tables/%s&app_profile_id=%s",
                                "fake-project", "fake-instance", "fake-table", "fake-app-profile"));
                        headers.put(
                            Key.of("x-goog-api-client", Metadata.ASCII_STRING_MARSHALLER),
                            "fake-client");

                        super.start(responseListener, headers);
                      }
                    };
                  }
                });

    doAnswer(
            invocation -> {
              Thread.sleep(10);
              return invocation.callRealMethod();
            })
        .when(fakeCredentials)
        .getRequestMetadata(any());

    doAnswer(
            invocation -> {
              Thread.sleep(10);
              invocation
                  .getArgument(1, StreamObserver.class)
                  .onError(Status.INTERNAL.asRuntimeException());
              return null;
            })
        .when(dataService)
        .checkAndMutateRow(any(), any());

    MetricsAttributes fakeAttrs = new MetricsAttributes() {};
    doReturn(fakeAttrs).when(mockMetrics).createAttributes(any());

    CheckAndMutateRowRequest request =
        CheckAndMutateRowRequest.newBuilder()
            .setTableName("project/fake-project/instances/fake-instance/tables/fake-table")
            .build();
    assertThrows(StatusRuntimeException.class, () -> stub.checkAndMutateRow(request));

    verify(mockMetrics)
        .createAttributes(
            eq(
                CallLabels.create(
                    BigtableGrpc.getCheckAndMutateRowMethod(),
                    Optional.of("fake-client"),
                    Optional.of("projects/fake-project/instances/fake-instance/tables/fake-table"),
                    Optional.of("fake-app-profile"))));

    verify(mockMetrics).recordCallStarted(eq(fakeAttrs));
    verify(mockMetrics).recordCredLatency(eq(fakeAttrs), eq(Status.OK), geq(Duration.ofMillis(10)));
    verify(mockMetrics).recordQueueLatency(eq(fakeAttrs), geq(Duration.ZERO));
    verify(mockMetrics).recordRequestSize(eq(fakeAttrs), eq((long) request.getSerializedSize()));
    verify(mockMetrics).recordResponseSize(eq(fakeAttrs), eq(0L));
    verify(mockMetrics)
        .recordCallLatency(eq(fakeAttrs), eq(Status.INTERNAL), geq(Duration.ofMillis(20)));
  }

  static class MetadataInterceptor implements ServerInterceptor {
    private BlockingQueue<Metadata> requestHeaders = new LinkedBlockingDeque<>();
    volatile Supplier<Metadata> responseHeaders = Metadata::new;
    volatile Supplier<Metadata> responseTrailers = Metadata::new;

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {
      requestHeaders.add(metadata);
      return next.startCall(
          new SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void sendHeaders(Metadata headers) {
              headers.merge(responseHeaders.get());
              super.sendHeaders(headers);
            }

            @Override
            public void close(Status status, Metadata trailers) {
              trailers.merge(responseTrailers.get());
              super.close(status, trailers);
            }
          },
          metadata);
    }
  }

  private static class FakeDataService extends BigtableImplBase {

    @Override
    public void checkAndMutateRow(
        CheckAndMutateRowRequest request,
        StreamObserver<CheckAndMutateRowResponse> responseObserver) {
      responseObserver.onNext(
          CheckAndMutateRowResponse.newBuilder().setPredicateMatched(true).build());
      responseObserver.onCompleted();
    }
  }

  private static class FakeCredentials extends Credentials {
    private static final String HEADER_NAME = "authorization";
    private String fakeValue = "fake-token";

    @Override
    public String getAuthenticationType() {
      return "fake";
    }

    @Override
    public Map<String, List<String>> getRequestMetadata(URI uri) throws IOException {
      return Map.of(HEADER_NAME, Lists.newArrayList(fakeValue));
    }

    @Override
    public boolean hasRequestMetadata() {
      return true;
    }

    @Override
    public boolean hasRequestMetadataOnly() {
      return true;
    }

    @Override
    public void refresh() throws IOException {
      // noop
    }
  }
}
