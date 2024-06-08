package examples;

import com.google.api.core.ApiFuture;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.api.gax.rpc.ApiCallContext;
import com.google.api.gax.rpc.OperationCallable;
import com.google.cloud.managedkafka.v1.ClusterName;
import com.google.cloud.managedkafka.v1.Cluster;
import com.google.cloud.managedkafka.v1.OperationMetadata;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

public class MockOperationFuture {

    public static OperationFuture<Cluster, OperationMetadata> getFuture() {
        return new OperationFuture<Cluster, OperationMetadata>() {
            @Override
            public String getName() throws InterruptedException, ExecutionException { return null; }

            @Override
            public ApiFuture<OperationSnapshot> getInitialFuture() { return null; }

            @Override
            public RetryingFuture<OperationSnapshot> getPollingFuture() { return null; }

            @Override
            public ApiFuture<OperationMetadata> peekMetadata() { return null; }

            @Override
            public ApiFuture<OperationMetadata> getMetadata() { return null; }

            @Override
            public void addListener(Runnable listener, Executor executor) {}

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) { return false; }

            @Override
            public boolean isCancelled() { return false; }

            @Override
            public boolean isDone() { return true; }

            @Override
            public Cluster get() throws InterruptedException, ExecutionException {
                return Cluster.newBuilder().setName(ClusterName.of(ClustersTest.projectId, ClustersTest.region, ClustersTest.clusterId).toString()).build();
            }

            @Override
            public Cluster get(long timeout, java.util.concurrent.TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return Cluster.newBuilder().setName(ClusterName.of(ClustersTest.projectId, ClustersTest.region, ClustersTest.clusterId).toString()).build();
            }
        };
    }

    public static <T extends com.google.protobuf.GeneratedMessageV3> OperationCallable<T, Cluster, OperationMetadata> getOperableCallable() {
        return new OperationCallable<T, Cluster, OperationMetadata>() {
            @Override
            public OperationFuture<Cluster, OperationMetadata> futureCall(T request, ApiCallContext context) {
                return getFuture();
            }

            @Override
            public OperationFuture<Cluster, OperationMetadata> resumeFutureCall(String operationName, ApiCallContext context) {
                return getFuture();
            }

            @Override
            public ApiFuture<Void> cancel(String operationName, ApiCallContext context) {
                return null;
            }
        };
    }
}
