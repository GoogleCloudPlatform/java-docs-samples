package examples;

import com.google.api.core.ApiFuture;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.api.gax.rpc.ApiCallContext;
import com.google.api.gax.rpc.OperationCallable;
import com.google.cloud.managedkafka.v1.OperationMetadata;
import com.google.protobuf.Empty;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

public class MockDeleteOperationFuture {
    public static OperationFuture<Empty, OperationMetadata> getFuture() {
        return new OperationFuture<Empty, OperationMetadata>() {
            @Override
            public String getName() throws InterruptedException, ExecutionException { return null; }

            @Override
            public ApiFuture<OperationSnapshot> getInitialFuture() {
                return null;
            }

            @Override
            public RetryingFuture<OperationSnapshot> getPollingFuture() {
                return null;
            }

            @Override
            public ApiFuture<OperationMetadata> peekMetadata() {
                return null;
            }

            @Override
            public ApiFuture<OperationMetadata> getMetadata() {
                return null;
            }

            @Override
            public void addListener(Runnable listener, Executor executor) {}

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public Empty get() throws InterruptedException, ExecutionException {
                return Empty.newBuilder().build();
            }

            @Override
            public Empty get(long timeout, java.util.concurrent.TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return Empty.newBuilder().build();
            }
        };
    }

    public static <T extends com.google.protobuf.GeneratedMessageV3> OperationCallable<T, Empty, OperationMetadata> getOperableCallable() {
        return new OperationCallable<T, Empty, OperationMetadata>() {
            @Override
            public OperationFuture<Empty, OperationMetadata> futureCall(T request, ApiCallContext context) {
                return getFuture();
            }

            @Override
            public OperationFuture<Empty, OperationMetadata> resumeFutureCall(String operationName, ApiCallContext context) {
                return getFuture();
            }

            @Override
            public ApiFuture<Void> cancel(String operationName, ApiCallContext context) {
                return null;
            }
        };
    }
}
