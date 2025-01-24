/*
 * Copyright 2025 Google LLC
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

package aiplatform.vectorsearch;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.aiplatform.v1.DeleteIndexRequest;
import com.google.cloud.aiplatform.v1.IndexName;
import com.google.cloud.aiplatform.v1.IndexServiceClient;
import com.google.cloud.aiplatform.v1.IndexServiceGrpc.IndexServiceImplBase;
import com.google.cloud.aiplatform.v1.IndexServiceSettings;
import com.google.longrunning.Operation;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DeleteIndexSampleTest {

  private static final String PROJECT = "test-project";
  private static final String LOCATION = "test-location";
  private static final String INDEX_ID = "1234";

  @Rule
  public final GrpcCleanupRule grpcCleanupRule = new GrpcCleanupRule();

  private ManagedChannel channel;
  private AtomicReference<DeleteIndexRequest> requestRef = new AtomicReference<>();

  @Before
  public void setUp() throws Exception {
    String serverName = InProcessServerBuilder.generateName();
    grpcCleanupRule.register(InProcessServerBuilder.forName(serverName).directExecutor()
        .addService(new IndexServiceImplBase() {
          @Override
          public void deleteIndex(DeleteIndexRequest request,
              StreamObserver<Operation> responseObserver) {
            requestRef.set(request);
            responseObserver.onNext(
                Operation.newBuilder().setDone(true)
                    .setResponse(Any.pack(Empty.getDefaultInstance())).build());
            responseObserver.onCompleted();
          }
        }).build().start());

    channel = grpcCleanupRule.register(
        InProcessChannelBuilder.forName(serverName).directExecutor().build());
  }

  @Test
  public void testDeleteIndexSample() throws Exception {
    IndexServiceClient client = IndexServiceClient.create(IndexServiceSettings.newBuilder()
        .setTransportChannelProvider(FixedTransportChannelProvider.create(
            GrpcTransportChannel.newBuilder().setManagedChannel(channel).build())).build());

    DeleteIndexSample.deleteIndexSample(PROJECT, LOCATION, INDEX_ID, client);

    assertThat(requestRef.get().getName()).isEqualTo(
        IndexName.of(PROJECT, LOCATION, INDEX_ID).toString());
  }
}
