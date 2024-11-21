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

import static com.google.cloud.bigtable.examples.proxy.utils.ContextSubject.assertThat;
import static com.google.cloud.bigtable.examples.proxy.utils.MetadataSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.auth.Credentials;
import com.google.bigtable.admin.v2.BigtableInstanceAdminGrpc;
import com.google.bigtable.admin.v2.BigtableInstanceAdminGrpc.BigtableInstanceAdminFutureStub;
import com.google.bigtable.admin.v2.BigtableInstanceAdminGrpc.BigtableInstanceAdminImplBase;
import com.google.bigtable.admin.v2.BigtableTableAdminGrpc;
import com.google.bigtable.admin.v2.BigtableTableAdminGrpc.BigtableTableAdminFutureStub;
import com.google.bigtable.admin.v2.BigtableTableAdminGrpc.BigtableTableAdminImplBase;
import com.google.bigtable.admin.v2.GetInstanceRequest;
import com.google.bigtable.admin.v2.GetTableRequest;
import com.google.bigtable.admin.v2.Instance;
import com.google.bigtable.admin.v2.Table;
import com.google.bigtable.v2.BigtableGrpc;
import com.google.bigtable.v2.BigtableGrpc.BigtableFutureStub;
import com.google.bigtable.v2.BigtableGrpc.BigtableImplBase;
import com.google.bigtable.v2.CheckAndMutateRowRequest;
import com.google.bigtable.v2.CheckAndMutateRowResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.longrunning.GetOperationRequest;
import com.google.longrunning.Operation;
import com.google.longrunning.OperationsGrpc;
import com.google.longrunning.OperationsGrpc.OperationsFutureStub;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Context;
import io.grpc.Deadline;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ServeTest {
  private final String targetServerName = UUID.randomUUID().toString();

  @Rule
  public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule().setTimeout(1, TimeUnit.MINUTES);

  // Fake targets
  private CallContextInterceptor callContextInterceptor;
  private MetadataInterceptor metadataInterceptor;
  private FakeDataService dataService;
  private FakeInstanceAdminService instanceAdminService;
  private FakeTableAdminService tableAdminService;
  private OperationService operationService;
  private ManagedChannel fakeServiceChannel;
  private FakeCredentials fakeCredentials;

  // Proxy
  private Serve serve;
  private ManagedChannel proxyChannel;

  @Before
  public void setUp() throws IOException {
    // Create the fake target
    callContextInterceptor = new CallContextInterceptor();
    metadataInterceptor = new MetadataInterceptor();
    dataService = new FakeDataService();
    instanceAdminService = new FakeInstanceAdminService();
    tableAdminService = new FakeTableAdminService();
    operationService = new OperationService();

    fakeCredentials = new FakeCredentials();

    grpcCleanup.register(
        InProcessServerBuilder.forName(targetServerName)
            .intercept(callContextInterceptor)
            .intercept(metadataInterceptor)
            .addService(dataService)
            .addService(instanceAdminService)
            .addService(tableAdminService)
            .addService(operationService)
            .build()
            .start());

    fakeServiceChannel =
        grpcCleanup.register(
            InProcessChannelBuilder.forName(targetServerName).usePlaintext().build());

    // Create the proxy
    // Inject fakes for upstream calls. For unit tests we want to shim communications to the
    // bigtable service.
    serve = createAndStartCommand(fakeServiceChannel, fakeCredentials);

    proxyChannel =
        grpcCleanup.register(
            ManagedChannelBuilder.forAddress("localhost", serve.listenPort).usePlaintext().build());
  }

  @After
  public void tearDown() throws InterruptedException {
    if (serve != null) {
      serve.cleanup();
    }
  }

  @Test
  public void testDataRpcOk() throws InterruptedException, ExecutionException, TimeoutException {
    BigtableFutureStub proxyStub = BigtableGrpc.newFutureStub(proxyChannel);

    CheckAndMutateRowRequest request =
        CheckAndMutateRowRequest.newBuilder().setTableName("some-table").build();
    final ListenableFuture<CheckAndMutateRowResponse> proxyFuture =
        proxyStub.checkAndMutateRow(request);
    StreamObserver<CheckAndMutateRowResponse> serverObserver =
        dataService
            .calls
            .computeIfAbsent(request, (ignored) -> new LinkedBlockingDeque<>())
            .poll(1, TimeUnit.SECONDS);

    assertWithMessage("Timed out waiting for the proxied RPC on the fake server")
        .that(serverObserver)
        .isNotNull();

    CheckAndMutateRowResponse expectedResponse =
        CheckAndMutateRowResponse.newBuilder().setPredicateMatched(true).build();

    serverObserver.onNext(expectedResponse);
    serverObserver.onCompleted();

    CheckAndMutateRowResponse r = proxyFuture.get(1, TimeUnit.SECONDS);
    assertThat(r).isEqualTo(expectedResponse);
  }

  @Test
  public void testInstanceRpcOk()
      throws InterruptedException, ExecutionException, TimeoutException {
    BigtableInstanceAdminFutureStub proxyStub =
        BigtableInstanceAdminGrpc.newFutureStub(proxyChannel);

    GetInstanceRequest request = GetInstanceRequest.newBuilder().setName("some-instance").build();
    final ListenableFuture<Instance> proxyFuture = proxyStub.getInstance(request);
    StreamObserver<Instance> serverObserver =
        instanceAdminService
            .calls
            .computeIfAbsent(request, (ignored) -> new LinkedBlockingDeque<>())
            .poll(1, TimeUnit.SECONDS);

    assertWithMessage("Timed out waiting for the proxied RPC on the fake server")
        .that(serverObserver)
        .isNotNull();

    Instance expectedResponse = Instance.newBuilder().setName("some-instance").build();

    serverObserver.onNext(expectedResponse);
    serverObserver.onCompleted();

    Instance r = proxyFuture.get(1, TimeUnit.SECONDS);
    assertThat(r).isEqualTo(expectedResponse);
  }

  @Test
  public void testTableRpcOk() throws InterruptedException, ExecutionException, TimeoutException {
    BigtableTableAdminFutureStub proxyStub = BigtableTableAdminGrpc.newFutureStub(proxyChannel);

    GetTableRequest request = GetTableRequest.newBuilder().setName("some-table").build();
    final ListenableFuture<Table> proxyFuture = proxyStub.getTable(request);
    StreamObserver<Table> serverObserver =
        tableAdminService
            .calls
            .computeIfAbsent(request, (ignored) -> new LinkedBlockingDeque<>())
            .poll(1, TimeUnit.SECONDS);

    assertWithMessage("Timed out waiting for the proxied RPC on the fake server")
        .that(serverObserver)
        .isNotNull();

    Table expectedResponse = Table.newBuilder().setName("some-table").build();

    serverObserver.onNext(expectedResponse);
    serverObserver.onCompleted();

    Table r = proxyFuture.get(1, TimeUnit.SECONDS);
    assertThat(r).isEqualTo(expectedResponse);
  }

  @Test
  public void testOpRpcOk() throws InterruptedException, ExecutionException, TimeoutException {
    OperationsFutureStub proxyStub = OperationsGrpc.newFutureStub(proxyChannel);

    GetOperationRequest request = GetOperationRequest.newBuilder().setName("some-table").build();
    final ListenableFuture<Operation> proxyFuture = proxyStub.getOperation(request);
    StreamObserver<Operation> serverObserver =
        operationService
            .calls
            .computeIfAbsent(request, (ignored) -> new LinkedBlockingDeque<>())
            .poll(1, TimeUnit.SECONDS);

    if (proxyFuture.isDone()) {
      proxyFuture.get();
    }
    assertWithMessage("Timed out waiting for the proxied RPC on the fake server")
        .that(serverObserver)
        .isNotNull();

    Operation expectedResponse = Operation.newBuilder().setName("some-table").build();

    serverObserver.onNext(expectedResponse);
    serverObserver.onCompleted();

    Operation r = proxyFuture.get(1, TimeUnit.SECONDS);
    assertThat(r).isEqualTo(expectedResponse);
  }

  @Test
  public void testMetadataProxy()
      throws InterruptedException, ExecutionException, TimeoutException {
    Metadata responseMetadata = new Metadata();
    responseMetadata.put(Key.of("resp-header", Metadata.ASCII_STRING_MARSHALLER), "resp-value");
    metadataInterceptor.responseHeaders = () -> responseMetadata;

    Metadata trailers = new Metadata();
    trailers.put(Key.of("trailer", Metadata.ASCII_STRING_MARSHALLER), "trailer-value");
    metadataInterceptor.responseTrailers = () -> trailers;

    AtomicReference<Metadata> clientRecvHeader = new AtomicReference<>();
    AtomicReference<Metadata> clientRecvTrailer = new AtomicReference<>();

    BigtableFutureStub proxyStub =
        BigtableGrpc.newFutureStub(proxyChannel)
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
                        headers.put(
                            Key.of("client-sent-header", Metadata.ASCII_STRING_MARSHALLER),
                            "client-sent-header-value");
                        super.start(
                            new SimpleForwardingClientCallListener<RespT>(responseListener) {
                              @Override
                              public void onHeaders(Metadata headers) {
                                clientRecvHeader.set(headers);
                                super.onHeaders(headers);
                              }

                              @Override
                              public void onClose(Status status, Metadata trailers) {
                                clientRecvTrailer.set(trailers);
                                super.onClose(status, trailers);
                              }
                            },
                            headers);
                      }
                    };
                  }
                });

    CheckAndMutateRowRequest request =
        CheckAndMutateRowRequest.newBuilder().setTableName("some-table").build();
    final ListenableFuture<CheckAndMutateRowResponse> proxyFuture =
        proxyStub.checkAndMutateRow(request);
    StreamObserver<CheckAndMutateRowResponse> serverObserver =
        dataService
            .calls
            .computeIfAbsent(request, (ignored) -> new LinkedBlockingDeque<>())
            .poll(1, TimeUnit.SECONDS);

    assertWithMessage("Timed out waiting for the proxied RPC on the fake server")
        .that(serverObserver)
        .isNotNull();

    serverObserver.onNext(CheckAndMutateRowResponse.newBuilder().setPredicateMatched(true).build());
    serverObserver.onCompleted();

    proxyFuture.get(1, TimeUnit.SECONDS);

    assertThat(metadataInterceptor.requestHeaders.poll(1, TimeUnit.SECONDS))
        .hasValue("client-sent-header", "client-sent-header-value");

    assertThat(clientRecvHeader.get()).hasValue("resp-header", "resp-value");
    assertThat(clientRecvTrailer.get()).hasValue("trailer", "trailer-value");
  }

  @Test
  public void testDeadlinePropagation()
      throws InterruptedException, ExecutionException, TimeoutException {

    Deadline originalDeadline = Deadline.after(10, TimeUnit.MINUTES);

    BigtableFutureStub proxyStub =
        BigtableGrpc.newFutureStub(proxyChannel).withDeadline(originalDeadline);

    CheckAndMutateRowRequest request =
        CheckAndMutateRowRequest.newBuilder().setTableName("some-table").build();
    final ListenableFuture<CheckAndMutateRowResponse> proxyFuture =
        proxyStub.checkAndMutateRow(request);
    StreamObserver<CheckAndMutateRowResponse> serverObserver =
        dataService
            .calls
            .computeIfAbsent(request, (ignored) -> new LinkedBlockingDeque<>())
            .poll(1, TimeUnit.SECONDS);

    assertWithMessage("Timed out waiting for the proxied RPC on the fake server")
        .that(serverObserver)
        .isNotNull();

    serverObserver.onNext(CheckAndMutateRowResponse.newBuilder().setPredicateMatched(true).build());
    serverObserver.onCompleted();

    proxyFuture.get(1, TimeUnit.SECONDS);

    Context serverContext = callContextInterceptor.contexts.poll(1, TimeUnit.SECONDS);
    assertThat(serverContext)
        .hasRemainingDeadlineThat()
        .isIn(Range.closed(Duration.ofMinutes(9), Duration.ofMinutes(10)));
  }

  @Test
  public void testCredentials() throws InterruptedException, ExecutionException, TimeoutException {
    BigtableFutureStub proxyStub = BigtableGrpc.newFutureStub(proxyChannel);

    CheckAndMutateRowRequest request =
        CheckAndMutateRowRequest.newBuilder().setTableName("some-table").build();
    final ListenableFuture<CheckAndMutateRowResponse> proxyFuture =
        proxyStub.checkAndMutateRow(request);
    StreamObserver<CheckAndMutateRowResponse> serverObserver =
        dataService
            .calls
            .computeIfAbsent(request, (ignored) -> new LinkedBlockingDeque<>())
            .poll(1, TimeUnit.SECONDS);

    assertWithMessage("Timed out waiting for the proxied RPC on the fake server")
        .that(serverObserver)
        .isNotNull();

    serverObserver.onNext(CheckAndMutateRowResponse.newBuilder().setPredicateMatched(true).build());
    serverObserver.onCompleted();
    proxyFuture.get(1, TimeUnit.SECONDS);

    assertThat(metadataInterceptor.requestHeaders.poll(1, TimeUnit.SECONDS))
        .hasValue("authorization", "fake-token");
  }

  @Test
  public void testCredentialsClobber()
      throws InterruptedException, ExecutionException, TimeoutException {
    BigtableFutureStub proxyStub =
        BigtableGrpc.newFutureStub(proxyChannel)
            .withInterceptors(
                new ClientInterceptor() {
                  @Override
                  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                      MethodDescriptor<ReqT, RespT> methodDescriptor,
                      CallOptions callOptions,
                      Channel channel) {
                    return new SimpleForwardingClientCall<ReqT, RespT>(
                        channel.newCall(methodDescriptor, callOptions)) {
                      @Override
                      public void start(Listener<RespT> responseListener, Metadata headers) {
                        headers.put(
                            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER),
                            "pre-proxied-value");
                        super.start(responseListener, headers);
                      }
                    };
                  }
                });

    CheckAndMutateRowRequest request =
        CheckAndMutateRowRequest.newBuilder().setTableName("some-table").build();
    final ListenableFuture<CheckAndMutateRowResponse> proxyFuture =
        proxyStub.checkAndMutateRow(request);
    StreamObserver<CheckAndMutateRowResponse> serverObserver =
        dataService
            .calls
            .computeIfAbsent(request, (ignored) -> new LinkedBlockingDeque<>())
            .poll(1, TimeUnit.SECONDS);

    assertWithMessage("Timed out waiting for the proxied RPC on the fake server")
        .that(serverObserver)
        .isNotNull();

    serverObserver.onNext(CheckAndMutateRowResponse.newBuilder().setPredicateMatched(true).build());
    serverObserver.onCompleted();
    proxyFuture.get(1, TimeUnit.SECONDS);

    Metadata serverRequestHeaders = metadataInterceptor.requestHeaders.poll(1, TimeUnit.SECONDS);
    assertThat(serverRequestHeaders).hasValue("authorization", "fake-token");
  }

  private static Serve createAndStartCommand(
      ManagedChannel targetChannel, FakeCredentials targetCredentials) throws IOException {
    for (int i = 10; i >= 0; i--) {
      Serve s = new Serve();
      s.dataChannel = targetChannel;
      s.adminChannel = targetChannel;
      s.credentials = targetCredentials;

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

  static class CallContextInterceptor implements ServerInterceptor {
    BlockingQueue<Context> contexts = new LinkedBlockingDeque<>();

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

      contexts.add(Context.current());
      return next.startCall(call, headers);
    }
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
    private final ConcurrentHashMap<
            CheckAndMutateRowRequest, BlockingDeque<StreamObserver<CheckAndMutateRowResponse>>>
        calls = new ConcurrentHashMap<>();

    @Override
    public void checkAndMutateRow(
        CheckAndMutateRowRequest request,
        StreamObserver<CheckAndMutateRowResponse> responseObserver) {
      calls
          .computeIfAbsent(request, (ignored) -> new LinkedBlockingDeque<>())
          .add(responseObserver);
    }
  }

  private static class FakeInstanceAdminService extends BigtableInstanceAdminImplBase {
    private final ConcurrentHashMap<GetInstanceRequest, BlockingDeque<StreamObserver<Instance>>>
        calls = new ConcurrentHashMap<>();

    @Override
    public void getInstance(GetInstanceRequest request, StreamObserver<Instance> responseObserver) {
      calls
          .computeIfAbsent(request, (ignored) -> new LinkedBlockingDeque<>())
          .add(responseObserver);
    }
  }

  private static class FakeTableAdminService extends BigtableTableAdminImplBase {
    private final ConcurrentHashMap<GetTableRequest, BlockingDeque<StreamObserver<Table>>> calls =
        new ConcurrentHashMap<>();

    @Override
    public void getTable(GetTableRequest request, StreamObserver<Table> responseObserver) {
      calls
          .computeIfAbsent(request, (ignored) -> new LinkedBlockingDeque<>())
          .add(responseObserver);
    }
  }

  private static class OperationService extends OperationsGrpc.OperationsImplBase {
    private final ConcurrentHashMap<GetOperationRequest, BlockingDeque<StreamObserver<Operation>>>
        calls = new ConcurrentHashMap<>();

    @Override
    public void getOperation(
        GetOperationRequest request, StreamObserver<Operation> responseObserver) {
      calls
          .computeIfAbsent(request, (ignored) -> new LinkedBlockingDeque<>())
          .add(responseObserver);
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
