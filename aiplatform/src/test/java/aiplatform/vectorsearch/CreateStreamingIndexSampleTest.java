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
import com.google.cloud.aiplatform.v1.CreateIndexRequest;
import com.google.cloud.aiplatform.v1.Index;
import com.google.cloud.aiplatform.v1.Index.IndexUpdateMethod;
import com.google.cloud.aiplatform.v1.IndexServiceClient;
import com.google.cloud.aiplatform.v1.IndexServiceGrpc.IndexServiceImplBase;
import com.google.cloud.aiplatform.v1.IndexServiceSettings;
import com.google.cloud.aiplatform.v1.LocationName;
import com.google.longrunning.Operation;
import com.google.protobuf.Any;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CreateStreamingIndexSampleTest {

  private static final String PROJECT = "test-project";
  private static final String LOCATION = "test-location";
  private static final String DISPLAY_NAME = "test-display-name";
  private static final String METADATA_JSON = "{'some': {'key' : 2}}";

  @Rule
  public final GrpcCleanupRule grpcCleanupRule = new GrpcCleanupRule();

  private ManagedChannel channel;

  @Before
  public void setUp() throws Exception {
    String serverName = InProcessServerBuilder.generateName();
    grpcCleanupRule.register(InProcessServerBuilder.forName(serverName).directExecutor()
        .addService(new IndexServiceImplBase() {
          @Override
          public void createIndex(CreateIndexRequest request,
              StreamObserver<Operation> responseObserver) {
            assertThat(request.getParent()).isEqualTo(
                LocationName.of(PROJECT, LOCATION).toString());
            responseObserver.onNext(
                Operation.newBuilder().setDone(true).setResponse(Any.pack(request.getIndex()))
                    .build());
            responseObserver.onCompleted();
          }
        }).build().start());

    channel = grpcCleanupRule.register(
        InProcessChannelBuilder.forName(serverName).directExecutor().build());
  }

  @Test
  public void testCreateStreamingIndexSample() throws Exception {
    IndexServiceClient client = IndexServiceClient.create(IndexServiceSettings.newBuilder()
        .setTransportChannelProvider(FixedTransportChannelProvider.create(
            GrpcTransportChannel.newBuilder().setManagedChannel(channel).build())).build());

    Index response = CreateStreamingIndexSample.createStreamingIndexSample(PROJECT, LOCATION,
        DISPLAY_NAME, METADATA_JSON, client);

    Value.Builder metadata = Value.newBuilder();
    JsonFormat.parser().merge(METADATA_JSON, metadata);
    assertThat(response).isEqualTo(
        Index.newBuilder().setDisplayName(DISPLAY_NAME).setMetadata(metadata)
            .setIndexUpdateMethod(IndexUpdateMethod.STREAM_UPDATE).build());
  }
}
