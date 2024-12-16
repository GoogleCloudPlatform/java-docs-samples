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

package aiplatform.vectorsearch;

import static com.google.common.truth.Truth.assertThat;

import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.aiplatform.v1.Index;
import com.google.cloud.aiplatform.v1.IndexServiceClient;
import com.google.cloud.aiplatform.v1.IndexServiceClient.ListIndexesPagedResponse;
import com.google.cloud.aiplatform.v1.IndexServiceGrpc.IndexServiceImplBase;
import com.google.cloud.aiplatform.v1.IndexServiceSettings;
import com.google.cloud.aiplatform.v1.ListIndexesRequest;
import com.google.cloud.aiplatform.v1.ListIndexesResponse;
import com.google.cloud.aiplatform.v1.LocationName;
import com.google.common.collect.Lists;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ListIndexesSampleTest {

  private static final String PROJECT = "test-project";
  private static final String LOCATION = "test-location";
  private static final Index INDEX_1 = Index.newBuilder().setDisplayName("index-1").build();
  private static final Index INDEX_2 = Index.newBuilder().setDisplayName("index-2").build();

  @Rule
  public final GrpcCleanupRule grpcCleanupRule = new GrpcCleanupRule();

  private ManagedChannel channel;

  @Before
  public void setUp() throws Exception {
    String serverName = InProcessServerBuilder.generateName();
    grpcCleanupRule.register(InProcessServerBuilder.forName(serverName).directExecutor()
        .addService(new IndexServiceImplBase() {
          @Override
          public void listIndexes(ListIndexesRequest request,
              StreamObserver<ListIndexesResponse> responseObserver) {
            assertThat(request.getParent()).isEqualTo(
                LocationName.of(PROJECT, LOCATION).toString());
            responseObserver.onNext(
                ListIndexesResponse.newBuilder().addIndexes(INDEX_1).addIndexes(INDEX_2).build());
            responseObserver.onCompleted();
          }
        }).build().start());

    channel = grpcCleanupRule.register(
        InProcessChannelBuilder.forName(serverName).directExecutor().build());
  }

  @Test
  public void testListIndexesSample() throws Exception {
    IndexServiceClient client = IndexServiceClient.create(IndexServiceSettings.newBuilder()
        .setTransportChannelProvider(FixedTransportChannelProvider.create(
            GrpcTransportChannel.newBuilder().setManagedChannel(channel).build())).build());

    ListIndexesPagedResponse response = ListIndexesSample.listIndexesSample(PROJECT, LOCATION,
        client);

    List<Index> indexes = Lists.newArrayList(response.iterateAll());
    assertThat(indexes).containsExactly(INDEX_1, INDEX_2);
  }
}
